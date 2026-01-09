import os
import pandas as pd
import tiktoken
from typing import Dict, Any, List, Optional
from pycra import settings
from pycra.core.llm_factory import LLMFactory
from pycra.utils import logger

# Try importing GraphRAG components; handle missing dependencies gracefully
try:
    from graphrag.query.indexer_adapters import (
        read_indexer_entities,
        read_indexer_relationships,
        read_indexer_reports,
        read_indexer_text_units,
        read_indexer_covariates,
    )
    from graphrag.query.context_builder.entity_extraction import EntityVectorStoreKey
    from graphrag.query.structured_search.local_search.mixed_context import (
        LocalSearchMixedContext,
    )
    from graphrag.query.structured_search.local_search.search import LocalSearch
    from graphrag.query.structured_search.global_search.search import GlobalSearch
    from graphrag.query.structured_search.global_search.community_context import (
        GlobalCommunityContext,
    )
    from graphrag.vector_stores.lancedb import LanceDBVectorStore
    HAS_GRAPHRAG = True
except ImportError:
    logger.warning("Microsoft GraphRAG library not found. GraphRAG features will be disabled.")
    HAS_GRAPHRAG = False

class GraphRAGEngine:
    """
    Integration with Microsoft GraphRAG framework.
    Loads indexed artifacts (Parquet files) and provides Local/Global search capabilities.
    """

    def __init__(self):
        self.config = settings.rag.graphrag if settings and settings.rag.graphrag else None
        self.enabled = HAS_GRAPHRAG and self.config and self.config.enabled
        self.search_engine = None
        
        if self.enabled:
            try:
                self._initialize_engine()
            except Exception as e:
                logger.error(f"Failed to initialize GraphRAG engine: {e}")
                self.enabled = False

    def _initialize_engine(self):
        """
        Load Parquet files and setup the search engine.
        """
        input_dir = self.config.index_path
        if not os.path.exists(input_dir):
            raise FileNotFoundError(f"GraphRAG index directory not found: {input_dir}")

        logger.info(f"Loading GraphRAG artifacts from {input_dir}...")
        
        # 1. Load Dataframes
        # Note: Adjust filenames based on your specific GraphRAG version output
        entities_df = pd.read_parquet(os.path.join(input_dir, "create_final_entities.parquet"))
        reports_df = pd.read_parquet(os.path.join(input_dir, "create_final_community_reports.parquet"))
        text_units_df = pd.read_parquet(os.path.join(input_dir, "create_final_text_units.parquet"))
        relationships_df = pd.read_parquet(os.path.join(input_dir, "create_final_relationships.parquet"))
        
        # Optional: Covariates
        covariates_df = None
        cov_path = os.path.join(input_dir, "create_final_covariates.parquet")
        if os.path.exists(cov_path):
             covariates_df = pd.read_parquet(cov_path)

        # 2. Setup LLM & Embeddings
        # GraphRAG expects its own LLM wrappers or compatible interfaces
        # We wrap our LangChain LLM or use OpenAI directly if config allows
        llm_model = settings.llm.model_name
        embedding_model = settings.embeddings.model_name
        api_key = settings.llm.api_key
        
        # Using GraphRAG's internal ChatOpenAI/OpenAIEmbedding wrappers is standard
        from graphrag.llm.openai.chat_openai import ChatOpenAI as GraphRAGChatOpenAI
        from graphrag.llm.openai.openai_embeddings import OpenAIEmbeddings as GraphRAGEmbeddings
        
        llm = GraphRAGChatOpenAI(
            api_key=api_key,
            model=llm_model,
            api_type=graphrag.llm.openai.OpenAIClientTypes.OpenAI, # or Azure
        )
        
        text_embedder = GraphRAGEmbeddings(
            api_key=api_key,
            model=embedding_model,
            api_type=graphrag.llm.openai.OpenAIClientTypes.OpenAI,
        )

        token_encoder = tiktoken.get_encoding("cl100k_base")

        # 3. Setup Search Engine
        if self.config.search_type == "local":
            self._setup_local_search(
                llm, text_embedder, token_encoder, 
                entities_df, reports_df, text_units_df, relationships_df, covariates_df
            )
        else:
            self._setup_global_search(
                llm, token_encoder, reports_df, entities_df
            )

    def _setup_local_search(self, llm, text_embedder, token_encoder, 
                           entities, reports, text_units, relationships, covariates):
        
        # We need a vector store for entities description embeddings
        # Assuming description_embedding exists in entities_df
        # If using LanceDB (default in GraphRAG), we might connect to it.
        # For simplicity here, we assume in-memory vector search provided by LocalSearch's context builder
        # or we rely on the pre-computed embeddings in the parquet.
        
        context_builder = LocalSearchMixedContext(
            all_community_reports=reports,
            all_text_units=text_units,
            all_entities=entities,
            all_relationships=relationships,
            all_covariates=covariates,
            text_embedder=text_embedder,
            token_encoder=token_encoder,
        )

        self.search_engine = LocalSearch(
            llm=llm,
            context_builder=context_builder,
            token_encoder=token_encoder,
            llm_params={
                "max_tokens": 2000,
                "temperature": 0.0,
            },
            context_builder_params={
                "text_unit_prop": 0.5,
                "community_prop": 0.1,
                "conversation_history_max_turns": 5,
                "conversation_history_user_turns_only": True,
                "top_k_mapped_entities": 10,
                "top_k_relationships": 10,
                "include_entity_rank": True,
                "include_relationship_weight": True,
                "include_community_rank": False,
                "return_candidate_context": False,
                "max_tokens": 12000,
            },
            response_type=self.config.response_type,
        )
        logger.info("GraphRAG Local Search initialized.")

    def _setup_global_search(self, llm, token_encoder, reports, entities):
        
        context_builder = GlobalCommunityContext(
            community_reports=reports,
            entities=entities,
            token_encoder=token_encoder,
        )

        self.search_engine = GlobalSearch(
            llm=llm,
            context_builder=context_builder,
            token_encoder=token_encoder,
            max_data_tokens=12000,
            map_llm_params={
                "max_tokens": 1000,
                "temperature": 0.0,
                "response_format": {"type": "json_object"},
            },
            reduce_llm_params={
                "max_tokens": 2000,
                "temperature": 0.0,
            },
            allow_general_knowledge=False,
            json_mode=True,
            context_builder_params={
                "use_community_summary": False,
                "shuffle_data": True,
                "include_community_rank": True,
                "min_community_rank": 0,
                "community_level": self.config.community_level,
                "max_tokens": 12000,
                "context_name": "Reports",
            },
            response_type=self.config.response_type,
        )
        logger.info("GraphRAG Global Search initialized.")

    def query(self, query: str) -> Dict[str, Any]:
        """
        Execute search query against the GraphRAG engine.
        """
        if not self.enabled or not self.search_engine:
            logger.warning("GraphRAG is disabled or not initialized.")
            return {"response": "", "context_data": []}

        try:
            logger.info(f"Executing GraphRAG search: {query}")
            result = self.search_engine.search(query)
            
            # Result is typically a SearchResult object with .response and .context_data
            return {
                "response": result.response,
                "context_data": result.context_data
            }
        except Exception as e:
            logger.error(f"GraphRAG search failed: {e}")
            return {"response": "", "error": str(e)}

    def build_context(self, key_entities: Dict[str, Any]) -> Dict[str, Any]:
        """
        Adapter method for ContractReviewAgent.
        Constructs a query based on extracted entities and returns the result.
        """
        # Construct a natural language query from entities
        parties = key_entities.get("Parties", "the parties")
        date = key_entities.get("Effective Date", "the date")
        
        query_text = f"What are the compliance risks and regulations concerning {parties} and contracts starting on {date}? " \
                     f"Provide details on liability, termination, and data privacy."

        result = self.query(query_text)
        
        return {
            "regulations": [], # We might parse context_data if we want raw records
            "regulations_summary": result.get("response", ""),
            "raw_context": result.get("context_data", [])
        }
