from decimal import Decimal
from typing import Any, Optional

from pydantic import BaseModel, Field


class RecommendItem(BaseModel):
    dish_id: Optional[int] = Field(default=None, alias="dishId")
    setmeal_id: Optional[int] = Field(default=None, alias="setmealId")
    item_type: str = Field(alias="itemType")
    name: str
    price: Decimal
    score: Decimal
    reason: str
    tags: list[str] = []
    sales_count: int = Field(default=0, alias="salesCount")
    image: Optional[str] = None

    model_config = {"populate_by_name": True}


class RecommendResponse(BaseModel):
    user_id: int = Field(alias="userId")
    query: str
    answer: str
    intent: dict[str, Any]
    items: list[RecommendItem]
    session_id: Optional[int] = Field(default=None, alias="sessionId")

    model_config = {"populate_by_name": True}
