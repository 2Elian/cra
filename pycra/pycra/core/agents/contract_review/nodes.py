from typing import Dict, Any, List
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser
from langchain_core.documents import Document

from pycra.core.llm_factory import LLMFactory
from pycra.core.document_processing import DocumentLoader, DocumentSplitter
from pycra.core.rag import VectorStoreFactory, GraphStore, GraphRAGEngine
from pycra.utils import logger
from .state import ContractReviewState

class ReviewNodes:
    """
    Nodes for the Contract Review Graph.
    """
    
    def __init__(self):
        self.llm = LLMFactory.create_llm(temperature=0.0)
        self.splitter = DocumentSplitter()
        try:
            self.vector_store = VectorStoreFactory.create_vector_store()
            self.retriever = VectorStoreFactory.get_retriever(self.vector_store)
        except Exception as e:
            logger.warning(f"Vector store init failed: {e}")
            self.retriever = None
            
        self.graph_store = GraphStore()

    def load_and_split(self, state: ContractReviewState) -> Dict[str, Any]:
        """
        Node: Load document and split into chunks.
        """
        file_path = state.get("file_path")
        text = state.get("contract_text")
        
        docs = []
        if file_path:
            docs = DocumentLoader.load_document(file_path)
        elif text:
            docs = [Document(page_content=text, metadata={"source": "input_text"})]
            
        chunks = self.splitter.split_documents(docs)
        return {"chunks": chunks}

    def extract_entities(self, state: ContractReviewState) -> Dict[str, Any]:
        """
        Node: Extract key entities (Parties, Dates, Amounts) using LLM.
        """
        logger.info("Extracting entities...")
        # Simplified: using first few chunks for header info
        context = "\n".join([c.page_content for c in state["chunks"][:3]])
        
        prompt = ChatPromptTemplate.from_messages([
            ("system", "You are a legal expert. Extract key entities from the contract header: Parties (Client, Provider), Effective Date, Expiration Date, Total Amount. Return JSON."),
            ("user", "Contract Header:\n{context}")
        ])
        
        chain = prompt | self.llm | JsonOutputParser()
        try:
            entities = chain.invoke({"context": context})
        except Exception as e:
            logger.error(f"Entity extraction failed: {e}")
            entities = {}
            
        return {"key_entities": entities}

    def retrieve_knowledge(self, state: ContractReviewState) -> Dict[str, Any]:
        """
        Node: Retrieve relevant clauses and regulations.
        """
        logger.info("Retrieving knowledge...")
        query = f"Contract for {state.get('key_entities', {}).get('Parties', 'general')}"
        
        relevant_clauses = []
        if self.retriever:
            relevant_clauses = self.retriever.invoke(query)
            
        # GraphRAG integration
        graphrag = GraphRAGEngine()
        gr_ctx = graphrag.build_context(state.get("key_entities", {}))
        relevant_regulations = [r.get("text", "") for r in gr_ctx.get("regulations", [])]
        regulations_summary = gr_ctx.get("regulations_summary", "")
        
        return {
            "relevant_clauses": relevant_clauses,
            "relevant_regulations": relevant_regulations,
            "messages": ["GraphRAG summary:", regulations_summary] if regulations_summary else []
        }

    def identify_risks(self, state: ContractReviewState) -> Dict[str, Any]:
        """
        Node: Analyze chunks for risks using LLM.
        """
        logger.info("Identifying risks...")
        chunks = state["chunks"]
        # Limit processing for demo/performance (e.g. process first 5 chunks or aggregate)
        # In production, map-reduce over all chunks
        
        context = "\n".join([c.page_content for c in chunks[:10]]) # Process first 10 chunks
        
        prompt = ChatPromptTemplate.from_messages([
            ("system", """You are a senior contract compliance auditor. Analyze the following contract text for risks.
            Focus on:
            1. Unbalanced liability
            2. Missing termination clauses
            3. Ambiguous payment terms
            4. Data privacy violations
            
            Return a list of risks in JSON format: [{{"type": "...", "severity": "High/Medium/Low", "description": "...", "clause": "..."}}]"""),
            ("user", "Contract Content:\n{context}")
        ])
        
        chain = prompt | self.llm | JsonOutputParser()
        try:
            risks = chain.invoke({"context": context})
            if not isinstance(risks, list):
                risks = [risks] # Handle single object return
        except Exception as e:
            logger.error(f"Risk ID failed: {e}")
            risks = []
            
        return {"risks": risks}

    def generate_report(self, state: ContractReviewState) -> Dict[str, Any]:
        """
        Node: Generate final markdown report.
        """
        logger.info("Generating report...")
        entities = state.get("key_entities", {})
        risks = state.get("risks", [])
        
        report = f"# Contract Compliance Review Report\n\n"
        report += f"## Key Information\n"
        for k, v in entities.items():
            report += f"- **{k}**: {v}\n"
            
        report += f"\n## Risk Assessment\n"
        if not risks:
            report += "No significant risks identified (or analysis failed).\n"
        else:
            for r in risks:
                report += f"### [{r.get('severity', 'Unknown')}] {r.get('type', 'Risk')}\n"
                report += f"- **Description**: {r.get('description')}\n"
                report += f"- **Clause**: {r.get('clause')}\n\n"
                
        report += "\n---\n*Generated by Contract Compliance Agent*"
        
        return {"final_report": report}
