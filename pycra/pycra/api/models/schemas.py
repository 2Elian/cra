from typing import Optional, Dict, Any, List
from pydantic import BaseModel

class ContractReviewRequest(BaseModel):
    file_path: Optional[str] = None
    contract_text: Optional[str] = None
    
class ContractReviewResponse(BaseModel):
    status: str
    report: Optional[str] = None
    risks: List[Dict[str, Any]] = []
    entities: Dict[str, Any] = {}
    error: Optional[str] = None
