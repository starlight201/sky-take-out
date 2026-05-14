import json
import os
import time
from decimal import Decimal
from typing import Any

import requests
from flask import Flask, Response, jsonify, request, stream_with_context

from app.algorithm.intent_parser import parse_intent
from app.algorithm.recommend_filter import filter_items
from app.algorithm.recommend_ranker import rank_items

app = Flask(__name__)

SKY_TAKEOUT_BASE_URL = os.getenv("SKY_TAKEOUT_BASE_URL", "http://127.0.0.1:8080").rstrip("/")
SKY_AGENT_TOKEN = os.getenv("SKY_AGENT_TOKEN")
BAILIAN_API_KEY = os.getenv("BAILIAN_API_KEY")
BAILIAN_BASE_URL = os.getenv("BAILIAN_BASE_URL", "https://dashscope.aliyuncs.com/compatible-mode/v1").rstrip("/")
BAILIAN_MODEL = os.getenv("BAILIAN_MODEL", "qwen-plus")
TOP_N = int(os.getenv("TOP_N", "5"))


def headers() -> dict[str, str]:
    if SKY_AGENT_TOKEN:
        return {"X-Agent-Token": SKY_AGENT_TOKEN}
    return {}


def spring_get(path: str) -> Any:
    response = requests.get(f"{SKY_TAKEOUT_BASE_URL}{path}", headers=headers(), timeout=10)
    response.raise_for_status()
    data = response.json()
    return data.get("data", data)


def spring_post(path: str, payload: dict[str, Any]) -> Any:
    response = requests.post(f"{SKY_TAKEOUT_BASE_URL}{path}", json=payload, headers=headers(), timeout=10)
    response.raise_for_status()
    data = response.json()
    return data.get("data", data)


def build_recommendation(user_id: int, query: str) -> dict[str, Any]:
    intent = parse_intent(query)
    shop_status = spring_get("/internal/agent/shop/status")
    status = shop_status.get("status", 0)
    if status != 1:
        return {
            "userId": user_id,
            "query": query,
            "answer": "店铺当前未营业，暂时不能为你推荐可下单菜品。",
            "intent": intent,
            "items": [],
            "sessionId": None,
        }

    dishes = spring_get("/internal/agent/dishes/available")
    setmeals = spring_get("/internal/agent/setmeals/available")
    preference = spring_get(f"/internal/agent/users/{user_id}/preferences")
    recent_orders = spring_get(f"/internal/agent/users/{user_id}/orders/recent?days=30")
    merge_recent_order_preferences(preference, recent_orders)

    candidates = setmeals if intent.get("preferSetmeal") else dishes + setmeals
    filtered = filter_items(candidates, intent, status)
    ranked = rank_items(filtered, intent, preference, TOP_N)
    items = [convert_item(item) for item in ranked]
    session_id = spring_post(
        "/internal/agent/recommend/log",
        {
            "userId": user_id,
            "query": query,
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
        },
    )

    return {
        "userId": user_id,
        "query": query,
        "answer": build_fallback_answer(items),
        "intent": intent,
        "items": items,
        "sessionId": session_id,
    }


def merge_recent_order_preferences(preference: dict[str, Any], recent_orders: list[dict[str, Any]]) -> None:
    likes = list(preference.get("likes") or [])
    for order in recent_orders[:5]:
        for tag in order.get("tags") or []:
            if tag not in likes:
                likes.append(tag)
    preference["likes"] = likes


def convert_item(item: dict[str, Any]) -> dict[str, Any]:
    converted = {
        "dishId": item.get("dishId"),
        "setmealId": item.get("setmealId"),
        "itemType": item.get("itemType"),
        "name": item.get("name"),
        "price": item.get("price"),
        "score": item.get("score"),
        "reason": item.get("reason"),
        "tags": item.get("tags") or [],
        "salesCount": item.get("salesCount") or 0,
        "image": item.get("image"),
    }
    for key in ["price", "score"]:
        if isinstance(converted[key], Decimal):
            converted[key] = str(converted[key])
    return converted


def build_fallback_answer(items: list[dict[str, Any]]) -> str:
    if not items:
        return "我没有找到完全符合条件的可售菜品，可以放宽预算或换个口味再试试。"
    names = "、".join(item["name"] for item in items[:3])
    return f"为你优先推荐：{names}。这些选项已按口味、预算、销量和个人偏好综合排序。"


def build_llm_prompt(payload: dict[str, Any]) -> str:
    item_lines = []
    for index, item in enumerate(payload["items"], start=1):
        item_lines.append(
            f"{index}. {item['name']}，价格{item['price']}元，评分{item['score']}，"
            f"销量{item['salesCount']}，标签{','.join(item['tags'])}，推荐依据：{item['reason']}"
        )
    item_text = "\n".join(item_lines) if item_lines else "没有符合条件的可推荐菜品。"
    return f"""用户ID：{payload['userId']}
用户需求：{payload['query']}
解析意图：{payload['intent']}
候选推荐结果：
{item_text}

请基于候选结果生成简洁自然的外卖推荐回复。不能编造候选结果之外的菜品、套餐、价格、销量和库存。"""


def generate_llm_answer(payload: dict[str, Any]) -> str:
    if not BAILIAN_API_KEY:
        return payload["answer"]
    response = requests.post(
        f"{BAILIAN_BASE_URL}/chat/completions",
        headers={"Authorization": f"Bearer {BAILIAN_API_KEY}", "Content-Type": "application/json"},
        json={
            "model": BAILIAN_MODEL,
            "messages": [
                {"role": "system", "content": "你是一个智能餐饮推荐助手。必须基于真实候选结果回答，不要提及库存数量。"},
                {"role": "user", "content": build_llm_prompt(payload)},
            ],
            "temperature": 0.4,
        },
        timeout=30,
    )
    response.raise_for_status()
    data = response.json()
    return data["choices"][0]["message"]["content"] or payload["answer"]


def stream_llm_answer(payload: dict[str, Any]):
    if not BAILIAN_API_KEY:
        yield payload["answer"]
        return
    with requests.post(
        f"{BAILIAN_BASE_URL}/chat/completions",
        headers={"Authorization": f"Bearer {BAILIAN_API_KEY}", "Content-Type": "application/json"},
        json={
            "model": BAILIAN_MODEL,
            "messages": [
                {"role": "system", "content": "你是一个智能餐饮推荐助手。必须基于真实候选结果回答，不要提及库存数量。"},
                {"role": "user", "content": build_llm_prompt(payload)},
            ],
            "temperature": 0.4,
            "stream": True,
        },
        timeout=60,
        stream=True,
    ) as response:
        response.raise_for_status()
        for line in response.iter_lines(decode_unicode=True):
            if not line or not line.startswith("data:"):
                continue
            data = line[len("data:"):].strip()
            if data == "[DONE]":
                break
            chunk = json.loads(data)
            delta = chunk["choices"][0].get("delta", {})
            content = delta.get("content")
            if content:
                yield content


@app.get("/health")
def health():
    return jsonify({"status": "ok", "runtime": "flask"})


@app.post("/api/recommend")
def recommend():
    body = request.get_json(force=True)
    payload = build_recommendation(int(body["userId"]), body["query"])
    payload["answer"] = generate_llm_answer(payload)
    return jsonify(payload)


@app.post("/api/recommend/stream")
def recommend_stream():
    body = request.get_json(force=True)

    @stream_with_context
    def generate():
        payload = build_recommendation(int(body["userId"]), body["query"])
        yield f"event: metadata\ndata: {json.dumps(payload, ensure_ascii=False)}\n\n"
        for token in stream_llm_answer(payload):
            yield f"event: message\ndata: {json.dumps({'content': token}, ensure_ascii=False)}\n\n"
            time.sleep(0.01)
        yield "event: done\ndata: {}\n\n"

    return Response(generate(), mimetype="text/event-stream")


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=8000, threaded=True)
