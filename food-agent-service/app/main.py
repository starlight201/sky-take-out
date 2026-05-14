from fastapi import FastAPI

from app.api.recommend_api import router as recommend_router

app = FastAPI(title="Sky Take Out Food Agent Service")
app.include_router(recommend_router, prefix="/api")


@app.get("/health")
async def health() -> dict:
    return {"status": "ok"}
