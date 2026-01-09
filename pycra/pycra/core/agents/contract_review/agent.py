from langgraph.graph import StateGraph, END
from pycra.core.base_agent import BaseAgent
from .state import ContractReviewState
from .nodes import ReviewNodes

class ContractReviewAgent(BaseAgent):
    """
    Agent responsible for reviewing contracts for compliance and risks.
    """
    def _build_graph(self):
        """
        Build the LangGraph workflow.
        """
        nodes = ReviewNodes()
        workflow = StateGraph(ContractReviewState)
        
        # Add nodes
        workflow.add_node("load_and_split", nodes.load_and_split)
        workflow.add_node("extract_entities", nodes.extract_entities)
        workflow.add_node("retrieve_knowledge", nodes.retrieve_knowledge)
        workflow.add_node("identify_risks", nodes.identify_risks)
        workflow.add_node("generate_report", nodes.generate_report)
        
        # Define edges
        workflow.set_entry_point("load_and_split")
        
        workflow.add_edge("load_and_split", "extract_entities")
        workflow.add_edge("extract_entities", "retrieve_knowledge")
        workflow.add_edge("retrieve_knowledge", "identify_risks")
        workflow.add_edge("identify_risks", "generate_report")
        workflow.add_edge("generate_report", END)
        
        self.graph = workflow.compile()
