from typing import Optional
from langchain_openai import ChatOpenAI
from langchain_core.language_models import BaseChatModel
from pycra import settings
from pycra.utils import logger

class LLMFactory:
    """
    Factory class to create LLM instances based on configuration.
    """
    @staticmethod
    def create_llm(temperature: Optional[float] = None) -> BaseChatModel:
        """
        Create an LLM instance.
        
        Args:
            provider: The LLM provider (e.g., "openai", "azure").
            model_name: specific model name (overrides config).
            temperature: sampling temperature (overrides config).
            
        Returns:
            BaseChatModel: A configured LangChain chat model.
        """
        if not settings:
            logger.error("Configuration not loaded, cannot create LLM.")
            raise RuntimeError("Configuration not loaded")

        llm_config = settings.llm
        
        # Determine model parameters
        final_model = llm_config.model_name
        final_temp = temperature if temperature is not None else llm_config.temperature
        provider = llm_config.providers
        logger.info(f"Initializing LLM: Provider={provider}, Model={final_model}, Temp={final_temp}")

        if provider == "openai":
            provider_settings = llm_config.providers.get("openai")
            api_key = provider_settings.api_key if provider_settings else llm_config.api_key
            base_url = provider_settings.base_url if provider_settings else llm_config.base_url
            
            return ChatOpenAI(
                model=final_model,
                temperature=final_temp,
                api_key=api_key,
                base_url=base_url
            )
            
        elif provider == "azure":
            provider_settings = llm_config.providers.get("azure")
            if not provider_settings:
                raise ValueError("Azure settings not found in configuration")
                
            # Note: LangChain Azure OpenAI integration might require different parameters
            # This is a simplified example
            return ChatOpenAI(
                model=final_model,
                temperature=final_temp,
                api_key=provider_settings.api_key,
                base_url=provider_settings.endpoint, # Often azure uses specific endpoint structure
                # additional azure params...
            )
            
        else:
            raise ValueError(f"Unsupported LLM provider: {provider}")

    @staticmethod
    def create_embedding_model():
        """
        Create an embedding model instance.
        """
        from langchain_openai import OpenAIEmbeddings
        
        if not settings:
            raise RuntimeError("Configuration not loaded")
            
        emb_config = settings.embeddings
        
        if emb_config.provider == "openai":
            # Assuming shared API key or from env
            return OpenAIEmbeddings(
                model=emb_config.model_name
            )
        else:
            raise ValueError(f"Unsupported embedding provider: {emb_config.provider}")
