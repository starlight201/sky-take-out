from typing import Any

from app.algorithm.recommend_scorer import build_reason, calculate_score


def rank_items(items: list[dict[str, Any]], intent: dict[str, Any], preference: dict[str, Any], top_n: int) -> list[dict[str, Any]]:
    max_sales = max([item.get("salesCount") or 0 for item in items], default=0)
    ranked: list[dict[str, Any]] = []
    for item in items:
        enriched = dict(item)
        enriched["score"] = calculate_score(item, intent, preference, max_sales)
        enriched["reason"] = build_reason(item, intent, preference)
        ranked.append(enriched)
    ranked.sort(key=lambda x: x["score"], reverse=True)
    return ranked[:top_n]
