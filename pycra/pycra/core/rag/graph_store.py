from typing import List, Dict, Any, Optional
from langchain_community.graphs import Neo4jGraph
from pycra import settings
from pycra.utils import logger

class GraphStore:
    """
    Wrapper for Neo4j Graph Database.
    """
    
    def __init__(self):
        if not settings or not settings.graph_store.neo4j:
            logger.warning("Graph store configuration missing or disabled.")
            self.graph = None
            return

        neo4j_config = settings.graph_store.neo4j
        try:
            self.graph = Neo4jGraph(
                url=neo4j_config.uri,
                username=neo4j_config.username,
                password=neo4j_config.password
            )
            logger.info("Connected to Neo4j Graph Database.")
        except Exception as e:
            logger.error(f"Failed to connect to Neo4j: {e}")
            self.graph = None

    def query(self, query: str, params: Dict[str, Any] = None) -> List[Dict[str, Any]]:
        """
        Execute a Cypher query.
        """
        if not self.graph:
            logger.warning("Graph store not initialized, returning empty result.")
            return []
            
        return self.graph.query(query, params=params)

    def refresh_schema(self):
        if self.graph:
            self.graph.refresh_schema()
