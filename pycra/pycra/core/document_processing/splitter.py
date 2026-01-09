from typing import List
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_core.documents import Document
from pycra import settings
from pycra.utils import logger

class DocumentSplitter:
    """
    Split documents into chunks for embedding.
    """
    
    def __init__(self, chunk_size: int = None, chunk_overlap: int = None):
        self.chunk_size = chunk_size or (settings.embeddings.chunk_size if settings else 1000)
        self.chunk_overlap = chunk_overlap or (settings.embeddings.chunk_overlap if settings else 200)
        self.splitter = RecursiveCharacterTextSplitter(
            chunk_size=self.chunk_size,
            chunk_overlap=self.chunk_overlap,
            separators=["\n\n", "\n", "。", ".", " ", ""]
        )
        
    def split_documents(self, documents: List[Document]) -> List[Document]:
        """
        Split a list of documents into chunks.
        """
        logger.info(f"Splitting {len(documents)} documents with chunk_size={self.chunk_size}")
        chunks = self.splitter.split_documents(documents)
        logger.info(f"Generated {len(chunks)} chunks")
        return chunks
