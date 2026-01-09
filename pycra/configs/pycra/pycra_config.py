import os
import yaml
from typing import Dict, Any, Optional
from pydantic import BaseModel, Field
from pydantic_settings import BaseSettings

class AppSettings(BaseModel):
    name: str
    version: str
    env: str
    debug: bool

class ServerSettings(BaseModel):
    host: str
    port: int

class LoggingSettings(BaseModel):
    level: str
    format: str
    file_path: str
    save_days: int
    when: str
    interval: int
    file_handler_maxBytes: int
    file_handler_backupCount: int

class LLMProviderSettings(BaseModel):
    api_key: Optional[str] = None
    base_url: Optional[str] = None
    endpoint: Optional[str] = None
    api_version: Optional[str] = None

class LLMSettings(BaseModel):
    model_name: str
    temperature: float
    api_key: Optional[str] = None
    base_url: Optional[str] = None
    # providers: Dict[str, LLMProviderSettings] = Field(default_factory=dict)
    providers: str

class EmbeddingSettings(BaseModel):
    model_name: str
    provider: str
    chunk_size: int
    chunk_overlap: int

class MilvusSettings(BaseModel):
    host: str
    port: int
    collection_name: str

class FaissSettings(BaseModel):
    index_path: str

class VectorStoreSettings(BaseModel):
    type: str
    milvus: Optional[MilvusSettings] = None
    faiss: Optional[FaissSettings] = None

class Neo4jSettings(BaseModel):
    uri: str
    username: str
    password: Optional[str] = None

class GraphStoreSettings(BaseModel):
    type: str
    neo4j: Optional[Neo4jSettings] = None

class GraphRAGSettings(BaseModel):
    enabled: bool = False
    index_path: str = "./ragtest/output"
    community_level: int = 2
    response_type: str = "multiple paragraphs"
    search_type: str = "local"

class RagSettings(BaseModel):
    search_k: int
    score_threshold: float
    graphrag: Optional[GraphRAGSettings] = None

class ContractReviewAgentSettings(BaseModel):
    max_iterations: int
    timeout: int

class AgentSettings(BaseModel):
    contract_review: ContractReviewAgentSettings

class Config(BaseSettings):
    app: AppSettings
    server: ServerSettings
    logging: LoggingSettings
    # TODO Open the following notes
    # llm: LLMSettings
    # embeddings: EmbeddingSettings
    vector_store: VectorStoreSettings
    graph_store: GraphStoreSettings
    rag: RagSettings
    agents: AgentSettings

    @classmethod
    def load_config(cls, config_path: str = None) -> "Config":
        """
        Load configuration from multiple YAML files if present, else fallback to single config.yaml.
        Files:
          - api.yaml (app, server)
          - logging.yaml (logging)
          - llm.yaml (llm, embeddings)
          - db.yaml (vector_store, graph_store)
          - rag.yaml (rag, agents)
        """
        current_dir = os.path.dirname(os.path.abspath(__file__))
        base = current_dir
        parent_dir = os.path.abspath(os.path.join(base, '..'))

        files = {
            "api": os.path.join(base, "api.yaml"),
            "logging": os.path.join(base, "logging.yaml"),
            # TODO Open the following notes
            # "llm": os.path.join(parent_dir, "llm.yaml"),
            "db": os.path.join(parent_dir, "db.yaml"),
            "rag": os.path.join(base, "rag.yaml"),
        }

        merged: Dict[str, Any] = {}

        def merge_dict(dst: Dict[str, Any], src: Dict[str, Any]):
            for k, v in src.items():
                if isinstance(v, dict) and isinstance(dst.get(k), dict):
                    merge_dict(dst[k], v)
                else:
                    dst[k] = v

        found_any = False
        for name, path in files.items():
            if os.path.exists(path):
                with open(path, "r", encoding="utf-8") as f:
                    data = yaml.safe_load(f) or {}
                    data = cls._expand_env_vars(data)
                    merge_dict(merged, data)
                    found_any = True
            else:
                raise FileNotFoundError(f"The file: {path} is not be Founded")

        if not found_any:
            # Fallback to single config.yaml or provided path
            if config_path is None:
                config_path = os.path.join(base, "config.yaml")
            if not os.path.exists(config_path):
                raise FileNotFoundError(f"Configuration file not found at {config_path}")
            with open(config_path, "r", encoding="utf-8") as f:
                merged = cls._expand_env_vars(yaml.safe_load(f) or {})

        return cls(**merged)

    @staticmethod
    def _expand_env_vars(data: Any) -> Any:
        if isinstance(data, dict):
            return {k: Config._expand_env_vars(v) for k, v in data.items()}
        elif isinstance(data, list):
            return [Config._expand_env_vars(v) for v in data]
        elif isinstance(data, str) and data.startswith("${") and data.endswith("}"):
            env_var = data[2:-1]
            return os.getenv(env_var, data) # Return original if env var not found, or maybe empty string
        return data
