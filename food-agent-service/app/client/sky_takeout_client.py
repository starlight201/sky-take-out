import time
from typing import Any

import httpx

from app.config.settings import settings


class SkyTakeoutClient:
    def __init__(self) -> None:
        self.base_url = settings.sky_takeout_base_url.rstrip("/")
        self.tool_calls: list[dict[str, Any]] = []
        self.headers = {"X-Agent-Token": settings.sky_agent_token} if settings.sky_agent_token else {}

    async def get_shop_status(self) -> dict[str, Any]:
        return await self._get("get_shop_status", "/internal/agent/shop/status")

    async def get_available_dishes(self) -> list[dict[str, Any]]:
        return await self._get("get_available_dishes", "/internal/agent/dishes/available")

    async def get_available_setmeals(self) -> list[dict[str, Any]]:
        return await self._get("get_available_setmeals", "/internal/agent/setmeals/available")

    async def get_user_preferences(self, user_id: int) -> dict[str, Any]:
        return await self._get("get_user_preferences", f"/internal/agent/users/{user_id}/preferences")

    async def get_recent_orders(self, user_id: int, days: int = 30) -> list[dict[str, Any]]:
        return await self._get("get_recent_orders", f"/internal/agent/users/{user_id}/orders/recent?days={days}")

    async def save_recommend_log(self, payload: dict[str, Any]) -> None:
        await self._post("save_recommend_log", "/internal/agent/recommend/log", payload)

    async def _get(self, tool_name: str, path: str) -> Any:
        started = time.perf_counter()
        try:
            async with httpx.AsyncClient(timeout=settings.request_timeout) as client:
                response = await client.get(f"{self.base_url}{path}", headers=self.headers)
                response.raise_for_status()
                data = response.json()
            result = data.get("data") if isinstance(data, dict) and "data" in data else data
            self._record(tool_name, path, result, True, started)
            return result
        except Exception as exc:
            self._record(tool_name, path, str(exc), False, started)
            raise

    async def _post(self, tool_name: str, path: str, payload: dict[str, Any]) -> Any:
        started = time.perf_counter()
        try:
            body = dict(payload)
            body["toolCalls"] = self.tool_calls
            async with httpx.AsyncClient(timeout=settings.request_timeout) as client:
                response = await client.post(f"{self.base_url}{path}", json=body, headers=self.headers)
                response.raise_for_status()
                data = response.json()
            result = data.get("data") if isinstance(data, dict) and "data" in data else data
            self._record(tool_name, path, result, True, started)
            return result
        except Exception as exc:
            self._record(tool_name, path, str(exc), False, started)
            raise

    def _record(self, tool_name: str, request_params: Any, response: Any, success: bool, started: float) -> None:
        summary = str(response)
        if len(summary) > 800:
            summary = summary[:800] + "..."
        self.tool_calls.append(
            {
                "toolName": tool_name,
                "requestParams": str(request_params),
                "responseSummary": summary,
                "success": 1 if success else 0,
                "costMs": int((time.perf_counter() - started) * 1000),
            }
        )
