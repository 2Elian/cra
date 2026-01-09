from typing import List, Optional
from langchain_core.documents import Document
from langchain_core.vectorstores import VectorStore
from langchain_community.vectorstores import Milvus, FAISS
from pycra.core.llm_factory import LLMFactory
from pycra import settings
from pycra.utils import logger

class VectorStoreFactory:
    """
    Factory to create Vector Store instances.
    """
    
    @staticmethod
    def create_vector_store() -> VectorStore:
        """
        Initialize the vector store based on configuration.
        """
        if not settings:
            raise RuntimeError("Configuration not loaded")
            
        embeddings = LLMFactory.create_embedding_model()
        vs_config = settings.vector_store
        
        if vs_config.type == "milvus":
            if not vs_config.milvus:
                raise ValueError("Milvus configuration missing")
                
            return Milvus(
                embedding_function=embeddings,
                connection_args={
                    "host": vs_config.milvus.host,
                    "port": vs_config.milvus.port
                },
                collection_name=vs_config.milvus.collection_name,
                auto_id=True
            )
            
        elif vs_config.type == "faiss":
            # FAISS is usually in-memory or loaded from disk
            # Here we just initialize a new in-memory one for simplicity if path doesn't exist
            # In production, you'd load_local
            logger.warning("FAISS support is basic (in-memory). Persistence requires explicit save/load.")
            return FAISS.from_texts([""], embeddings) # Dummy init
            
        else:
            raise ValueError(f"Unsupported vector store type: {vs_config.type}")

    @staticmethod
    def get_retriever(vector_store: VectorStore, k: int = None):
        search_k = k or (settings.rag.search_k if settings else 5)
        return vector_store.as_retriever(search_kwargs={"k": search_k})
