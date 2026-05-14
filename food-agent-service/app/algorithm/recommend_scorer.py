from decimal import Decimal, ROUND_HALF_UP
from typing import Any


def calculate_score(item: dict[str, Any], intent: dict[str, Any], preference: dict[str, Any], max_sales: int) -> Decimal:
    tag_score = _tag_score(item, intent)
    preference_score = _preference_score(item, preference)
    sales_score = _sales_score(item, max_sales)
    price_score = _price_score(item, intent.get("budget") or preference.get("defaultBudget"))
    scene_score = _scene_score(item, intent)

    final = (
        tag_score * Decimal("0.35")
        + preference_score * Decimal("0.25")
        + sales_score * Decimal("0.20")
        + price_score * Decimal("0.10")
        + scene_score * Decimal("0.10")
    )
    return final.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)


def build_reason(item: dict[str, Any], intent: dict[str, Any], preference: dict[str, Any]) -> str:
    tags = set(item.get("tags") or [])
    reasons: list[str] = []
    matched_tastes = tags.intersection(intent.get("tasteTags") or [])
    matched_scene = tags.intersection(intent.get("sceneTags") or [])
    matched_likes = tags.intersection(preference.get("likes") or [])

    if matched_tastes:
        reasons.append("符合" + "、".join(matched_tastes) + "口味")
    if matched_scene:
        reasons.append("适合" + "、".join(matched_scene))
    if matched_likes:
        reasons.append("贴合你的历史偏好")
    if intent.get("budget") is not None:
        reasons.append(f"价格在{intent['budget']}元预算内")
    if item.get("salesCount", 0) > 0:
        reasons.append(f"近期开售热度较高，销量{item.get('salesCount')}份")

    if not reasons:
        reasons.append("当前可售且综合评分靠前")
    return "，".join(reasons)


def _tag_score(item: dict[str, Any], intent: dict[str, Any]) -> Decimal:
    required = set((intent.get("tasteTags") or []) + (intent.get("sceneTags") or []))
    if not required:
        return Decimal("70")
    tags = set(item.get("tags") or [])
    return Decimal(len(tags.intersection(required))) / Decimal(len(required)) * Decimal("100")


def _preference_score(item: dict[str, Any], preference: dict[str, Any]) -> Decimal:
    tags = set(item.get("tags") or [])
    likes = set(preference.get("likes") or [])
    dislikes = set(preference.get("dislikes") or [])
    allergies = set(preference.get("allergies") or [])
    if tags.intersection(dislikes) or tags.intersection(allergies):
        return Decimal("0")
    if likes and tags.intersection(likes):
        return Decimal("100")
    return Decimal("60")


def _sales_score(item: dict[str, Any], max_sales: int) -> Decimal:
    if max_sales <= 0:
        return Decimal("60")
    return Decimal(str(item.get("salesCount") or 0)) / Decimal(max_sales) * Decimal("100")


def _price_score(item: dict[str, Any], budget: Any) -> Decimal:
    if budget is None:
        return Decimal("70")
    price = Decimal(str(item.get("price", 0)))
    budget_value = Decimal(str(budget))
    if price > budget_value:
        return Decimal("0")
    return (Decimal("1") - abs(budget_value - price) / budget_value) * Decimal("100")


def _scene_score(item: dict[str, Any], intent: dict[str, Any]) -> Decimal:
    scene_tags = set(intent.get("sceneTags") or [])
    if not scene_tags:
        return Decimal("70")
    tags = set(item.get("tags") or [])
    return Decimal("100") if tags.intersection(scene_tags) else Decimal("40")
