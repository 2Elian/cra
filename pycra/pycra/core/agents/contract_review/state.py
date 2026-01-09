from typing import TypedDict, List, Dict, Any, Optional, Annotated
import operator

class ContractReviewState(TypedDict):
    """
    State definition for the Contract Review Agent workflow.
    """
    # Input
    contract_text: str
    file_path: str
    
    # Processing
    chunks: List[Any] # List[Document]
    key_entities: Dict[str, Any]
    
    # Retrieval
    relevant_clauses: Annotated[List[Any], operator.add]
    relevant_regulations: Annotated[List[str], operator.add]
    
    # Analysis
    risks: List[Dict[str, Any]]
    compliance_issues: List[Dict[str, Any]]
    
    # Output
    final_report: Optional[str]
    messages: Annotated[List[str], operator.add]
