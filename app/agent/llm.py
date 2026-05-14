from collections.abc import AsyncIterator
from typing import Any

from langchain_core.messages import HumanMessage, SystemMessage
from langchain_openai import ChatOpenAI

from app.agent.prompts import RECOMMEND_PROMPT
from app.config.settings import settings


def llm_enabled() -> bool:
    return bool(settings.bailian_api_key)


def _create_llm(streaming: bool = False) -> ChatOpenAI:
    return ChatOpenAI(
        api_key=settings.bailian_api_key,
        base_url=settings.bailian_base_url,
        model=settings.bailian_model,
        temperature=0.4,
        streaming=streaming,
    )


def build_llm_prompt(user_id: int, query: str, intent: dict[str, Any], items: list[Any]) -> str:
    item_lines = []
    for index, item in enumerate(items, start=1):
        item_lines.append(
            f"{index}. {item.name}，价格{item.price}元，评分{item.score}，"
            f"销量{item.sales_count}，标签{','.join(item.tags)}，推荐依据：{item.reason}"
        )
    item_text = "\n".join(item_lines) if item_lines else "没有符合条件的可推荐菜品。"
    return f"""用户ID：{user_id}
用户需求：{query}
解析意图：{intent}
候选推荐结果：
{item_text}

请基于候选结果生成简洁自然的外卖推荐回复。不能编造候选结果之外的菜品、套餐、价格、销量和库存。"""


async def generate_answer(user_id: int, query: str, intent: dict[str, Any], items: list[Any], fallback: str) -> str:
    if not llm_enabled():
        return fallback
    llm = _create_llm()
    message = await llm.ainvoke(
        [
            SystemMessage(content=RECOMMEND_PROMPT),
            HumanMessage(content=build_llm_prompt(user_id, query, intent, items)),
        ]
    )
    return message.content or fallback


async def stream_answer(user_id: int, query: str, intent: dict[str, Any], items: list[Any], fallback: str) -> AsyncIterator[str]:
    if not llm_enabled():
        yield fallback
        return
    llm = _create_llm(streaming=True)
    async for chunk in llm.astream(
        [
            SystemMessage(content=RECOMMEND_PROMPT),
            HumanMessage(content=build_llm_prompt(user_id, query, intent, items)),
        ]
    ):
        if chunk.content:
            yield str(chunk.content)
