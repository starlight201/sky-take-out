import re
from typing import Any


TASTE_KEYWORDS = ["微辣", "重辣", "辣", "清淡", "少油", "低脂", "高蛋白", "甜", "酸", "下饭", "家常"]
SCENE_MAP = {
    "健身": ["适合健身", "高蛋白", "低脂", "少油", "清淡"],
    "午餐": ["适合午餐", "下饭", "主食"],
    "晚餐": ["适合晚餐", "清淡"],
}
EXCLUDE_PATTERNS = ["不吃", "不要", "忌口", "过敏", "不能吃"]


def parse_intent(query: str) -> dict[str, Any]:
    intent: dict[str, Any] = {
        "tasteTags": [],
        "sceneTags": [],
        "excludeTags": [],
        "budget": None,
        "preferSetmeal": "套餐" in query,
        "mealType": None,
    }

    budget_match = re.search(r"(\d+(?:\.\d+)?)\s*(?:元|块|以内|以下)", query)
    if budget_match:
        intent["budget"] = float(budget_match.group(1))

    for keyword in TASTE_KEYWORDS:
        if keyword in query:
            intent["tasteTags"].append(keyword)

    for scene, tags in SCENE_MAP.items():
        if scene in query:
            intent["mealType"] = scene
            intent["sceneTags"].extend(tags)

    for pattern in EXCLUDE_PATTERNS:
        if pattern in query:
            tail = query.split(pattern, 1)[1]
            candidates = re.split(r"[，。,、\s有什么推荐吗？?]+", tail)
            for candidate in candidates:
                candidate = candidate.strip()
                if candidate:
                    intent["excludeTags"].append(candidate)
                    intent["excludeTags"].append(f"含{candidate}")

    if "香菜" in query and any(pattern in query for pattern in EXCLUDE_PATTERNS):
        intent["excludeTags"].extend(["香菜", "含香菜"])

    for key in ["tasteTags", "sceneTags", "excludeTags"]:
        intent[key] = list(dict.fromkeys(intent[key]))

    return intent
