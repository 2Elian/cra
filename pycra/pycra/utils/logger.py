import logging
import sys
import os
from logging.handlers import TimedRotatingFileHandler
from datetime import datetime
from pycra import settings

def setup_logger(name: str) -> logging.Logger:
    """
    Setup a logger with the specified name and configuration.
    
    Args:
        name: The name of the logger (usually __name__).
        
    Returns:
        logging.Logger: Configured logger instance.
    """
    logger = logging.getLogger(name)
    
    # If logger already has handlers, assume it's already configured
    if logger.handlers:
        return logger
        
    # Set log level
    level_str = settings.logging.level if settings else "INFO"
    level = getattr(logging, level_str.upper(), logging.INFO)
    logger.setLevel(level)
    
    # Create formatter
    format_str = settings.logging.format if settings else "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
    formatter = logging.Formatter(format_str)
    
    # Console Handler
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setFormatter(formatter)
    logger.addHandler(console_handler)
    
    # File Handler
    if settings and settings.logging.file_path:
        log_dir = settings.logging.file_path
        os.makedirs(log_dir, exist_ok=True)
        today = datetime.now().strftime("%Y-%m-%d")
        log_file = os.path.join(log_dir, f"{name}-{today}.log")

        file_handler = TimedRotatingFileHandler(
            filename=log_file,
            when=settings.logging.when,
            interval=settings.logging.interval,
            backupCount=settings.logging.save_days,
            encoding="utf-8",
            utc=False
        )
        file_handler.maxBytes = settings.logging.file_handler_maxBytes * 1024 * 1024
        file_handler.backupCount = settings.logging.file_handler_backupCount

        file_handler.setFormatter(formatter)
        logger.addHandler(file_handler)

    return logger


# Create a default logger for the package
logger = setup_logger("pycra")
