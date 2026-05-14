from decimal import Decimal
from typing import Any


def filter_items(items: list[dict[str, Any]], intent: dict[str, Any], shop_status: int) -> list[dict[str, Any]]:
    if shop_status != 1:
        return []

    result: list[dict[str, Any]] = []
    budget = intent.get("budget")
    exclude_tags = set(intent.get("excludeTags") or [])

    for item in items:
        if item.get("status") != 1:
            continue
        if item.get("categoryStatus") != 1:
            continue
        if budget is not None and Decimal(str(item.get("price", 0))) > Decimal(str(budget)):
            continue
        tags = set(item.get("tags") or [])
        if exclude_tags and tags.intersection(exclude_tags):
            continue
        result.append(item)

    return result
