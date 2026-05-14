from typing import Any

from app.agent.tools import create_client
from app.agent.llm import generate_answer
from app.algorithm.intent_parser import parse_intent
from app.algorithm.recommend_filter import filter_items
from app.algorithm.recommend_ranker import rank_items
from app.config.settings import settings
from app.schemas.request import RecommendRequest
from app.schemas.response import RecommendItem, RecommendResponse


async def recommend(request: RecommendRequest) -> RecommendResponse:
    response, _client = await build_recommendation(request)
    response.answer = await generate_answer(
        request.user_id,
        request.query,
        response.intent,
        response.items,
        response.answer,
    )
    return response


async def build_recommendation(request: RecommendRequest) -> tuple[RecommendResponse, Any]:
    client = create_client()
    intent = parse_intent(request.query)

    shop_status = await client.get_shop_status()
    status = shop_status.get("status", 0)
    if status != 1:
        return RecommendResponse(
            userId=request.user_id,
            query=request.query,
            answer="店铺当前未营业，暂时不能为你推荐可下单菜品。",
            intent=intent,
            items=[],
        ), client

    dishes = await client.get_available_dishes()
    setmeals = await client.get_available_setmeals()
    preference = await client.get_user_preferences(request.user_id)
    recent_orders = await client.get_recent_orders(request.user_id)
    _merge_recent_order_preferences(preference, recent_orders)

    candidates = setmeals if intent.get("preferSetmeal") else dishes + setmeals
    filtered = filter_items(candidates, intent, status)
    ranked = rank_items(filtered, intent, preference, settings.top_n)

    session_id = await client.save_recommend_log(
        {
            "userId": request.user_id,
            "query": request.query,
            "intent": intent,
            "items": [
                {
                    "dishId": item.get("dishId"),
                    "setmealId": item.get("setmealId"),
                    "score": str(item["score"]),
                    "reason": item["reason"],
                }
                for item in ranked
            ],
        }
    )

    response_items = [
        RecommendItem(
            dishId=item.get("dishId"),
            setmealId=item.get("setmealId"),
            itemType=item.get("itemType"),
            name=item.get("name"),
            price=item.get("price"),
            score=item.get("score"),
            reason=item.get("reason"),
            tags=item.get("tags") or [],
            salesCount=item.get("salesCount") or 0,
            image=item.get("image"),
        )
        for item in ranked
    ]

    return RecommendResponse(
        userId=request.user_id,
        query=request.query,
        answer=_build_answer(response_items),
        intent=intent,
        items=response_items,
        sessionId=session_id,
    ), client


def _merge_recent_order_preferences(preference: dict[str, Any], recent_orders: list[dict[str, Any]]) -> None:
    likes = list(preference.get("likes") or [])
    for order in recent_orders[:5]:
        for tag in order.get("tags") or []:
            if tag not in likes:
                likes.append(tag)
    preference["likes"] = likes


def _build_answer(items: list[RecommendItem]) -> str:
    if not items:
        return "我没有找到完全符合条件的可售菜品，可以放宽预算或换个口味再试试。"
    names = "、".join(item.name for item in items[:3])
    return f"为你优先推荐：{names}。这些选项已按口味、预算、销量和个人偏好综合排序。"
