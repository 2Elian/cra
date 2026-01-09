try:
    from contextlib import asynccontextmanager
except ImportError:
    from contextlib2 import asynccontextmanager # type: ignore

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from pycra.api.middleware import register_exception_handlers
from pycra import settings
from pycra.utils import setup_logger
import uvicorn

api_logger = setup_logger(name="pycra-api")

@asynccontextmanager
async def lifespan(fastapi_app: FastAPI):
    api_logger.info("Starting the pycra API service...")
    await get_factory()
    api_logger.info("pycra API service started successfully")
    try:
        yield
    finally:
        await close_factory()
        api_logger.info("The pycra API service is shut down")

def create_app() -> FastAPI:
    app = FastAPI(
        title="pycra API",
        description="Welcome to pycra: a Contract Review Agent",
        version="1.0.0",
        lifespan=lifespan,
        docs_url="/docs",
        redoc_url="/redoc",
        openapi_url="/openapi.json"
    )
    config = get_config()
    app.add_middleware(
        CORSMiddleware,
        allow_origins="*",
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )
    register_exception_handlers(app)
    app.include_router(smw_router, prefix=config.get("api_prefix"))

    @app.get("/", tags=["Root"])
    async def root():
        return {
            "message": "pycra API",
            "version": "1.0.0",
            "status": "running",
            "docs": "/docs"
        }

    @app.get("/health", tags=["Health"])
    async def health_check():
        config = get_config()
        return {
            "status": "healthy",
            "timestamp": datetime.now().isoformat(),
            "config_valid": config.validate()
        }

    return app


app = create_app()

@app.get("/health")
async def health_check():
    return {"status": "ok", "version": settings.app.version}

if __name__ == "__main__":
    uvicorn.run(
        "pycra.api.main:app", 
        host=settings.server.host, 
        port=settings.server.port, 
        reload=settings.app.debug
    )
