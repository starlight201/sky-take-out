from pydantic import BaseModel, Field


class RecommendRequest(BaseModel):
    user_id: int = Field(alias="userId")
    query: str

    model_config = {"populate_by_name": True}
