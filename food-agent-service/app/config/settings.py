from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import Optional


class Settings(BaseSettings):
    sky_takeout_base_url: str = "http://127.0.0.1:8080"
    sky_agent_token: Optional[str] = None
    bailian_api_key: Optional[str] = None
    bailian_base_url: str = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    bailian_model: str = "qwen-plus"
    request_timeout: float = 10.0
    top_n: int = 5

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()
