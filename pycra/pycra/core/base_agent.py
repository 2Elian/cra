from abc import ABC, abstractmethod
from typing import Any, Dict, Optional
from langchain_core.runnables import Runnable
from langgraph.graph import StateGraph
from pycra.utils import logger

class BaseAgent(ABC):
    """
    Base class for all Agents in the system.
    Encapsulates common logic for initialization, state management, and execution.
    """
    
    def __init__(self, config: Optional[Dict[str, Any]] = None):
        self.config = config or {}
        self.graph: Optional[Runnable] = None
        self._build_graph()

    @abstractmethod
    def _build_graph(self):
        """
        Build the LangGraph state graph.
        This method must be implemented by subclasses to define the agent's workflow be self.graph.
        """
        raise NotImplementedError

    def run(self, inputs: Dict[str, Any], config: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """
        Execute the agent workflow.
        
        Args:
            inputs: Input dictionary for the workflow.
            config: Runtime configuration (e.g., callbacks).
            
        Returns:
            Dict[str, Any]: The final state of the workflow.
        """
        if not self.graph:
            logger.error("Agent graph not initialized.")
            raise RuntimeError("Agent graph not initialized. Call _build_graph() first.")
            
        logger.info(f"Starting agent execution with inputs keys: {list(inputs.keys())}")
        try:
            result = self.graph.invoke(inputs, config=config)
            logger.info("Agent execution completed successfully.")
            return result
        except Exception as e:
            logger.error(f"Agent execution failed: {e}", exc_info=True)
            raise

    async def arun(self, inputs: Dict[str, Any], config: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """
        Async execution of the agent workflow.
        """
        if not self.graph:
            logger.error("Agent graph not initialized.")
            raise RuntimeError("Agent graph not initialized. Call _build_graph() first.")

        logger.info(f"Starting async agent execution with inputs keys: {list(inputs.keys())}")
        try:
            result = await self.graph.ainvoke(inputs, config=config)
            logger.info("Async agent execution completed successfully.")
            return result
        except Exception as e:
            logger.error(f"Async agent execution failed: {e}", exc_info=True)
            raise
