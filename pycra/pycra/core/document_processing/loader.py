import os
from typing import List, Optional
from langchain_community.document_loaders import PyPDFLoader, TextLoader, UnstructuredWordDocumentLoader
from langchain_core.documents import Document
from pycra.utils import logger

class DocumentLoader:
    """
    Unified document loader for various file formats.
    """
    
    @staticmethod
    def load_document(file_path: str) -> List[Document]:
        """
        Load a document from the file path.
        Supports .pdf, .txt, .docx
        """
        if not os.path.exists(file_path):
            logger.error(f"File not found: {file_path}")
            raise FileNotFoundError(f"File not found: {file_path}")
            
        ext = os.path.splitext(file_path)[1].lower()
        
        try:
            if ext == ".pdf":
                loader = PyPDFLoader(file_path)
            elif ext == ".txt":
                loader = TextLoader(file_path, encoding="utf-8")
            elif ext in [".docx", ".doc"]:
                loader = UnstructuredWordDocumentLoader(file_path)
            else:
                raise ValueError(f"Unsupported file extension: {ext}")
                
            logger.info(f"Loading document: {file_path}")
            documents = loader.load()
            logger.info(f"Loaded {len(documents)} pages/sections from {file_path}")
            return documents
            
        except Exception as e:
            logger.error(f"Failed to load document {file_path}: {e}")
            raise
