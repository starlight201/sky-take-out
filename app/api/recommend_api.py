import json

from fastapi import APIRouter
from fastapi.responses import StreamingResponse

from app.agent.llm import stream_answer
from app.agent.recommend_agent import build_recommendation, recommend
from app.schemas.request import RecommendRequest
from app.schemas.response import RecommendResponse

router = APIRouter()


@router.post("/recommend", response_model=RecommendResponse)
async def recommend_api(request: RecommendRequest) -> RecommendResponse:
    return await recommend(request)


@router.post("/recommend/stream")
async def recommend_stream_api(request: RecommendRequest) -> StreamingResponse:
    async def event_generator():
        response, _client = await build_recommendation(request)
        payload = response.model_dump(mode="json", by_alias=True)
        yield f"event: metadata\ndata: {json.dumps(payload, ensure_ascii=False)}\n\n"
        async for token in stream_answer(request.user_id, request.query, response.intent, response.items, response.answer):
            yield f"event: message\ndata: {json.dumps({'content': token}, ensure_ascii=False)}\n\n"
        yield "event: done\ndata: {}\n\n"

    return StreamingResponse(event_generator(), media_type="text/event-stream")
