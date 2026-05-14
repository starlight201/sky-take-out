# food-agent-service

智能菜品推荐 Agent 服务。当前版本先实现确定性推荐闭环：规则解析用户意图，调用苍穹外卖 Spring Boot 内部接口获取真实数据，完成过滤、打分、排序和日志保存。

## 运行

```bash
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

如果本机暂时无法安装 FastAPI 依赖，可以使用免安装应急版（依赖当前环境已有的 Flask 和 requests）：

```bash
python flask_app.py
```

环境变量：

```text
SKY_TAKEOUT_BASE_URL=http://127.0.0.1:8080
BAILIAN_API_KEY=你的阿里云百炼API Key
BAILIAN_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
BAILIAN_MODEL=qwen-plus
SKY_AGENT_TOKEN=和 Spring Boot sky.agent.internal-token 保持一致，可选
```

## 接口

```http
POST /api/recommend
POST /api/recommend/stream
```

请求：

```json
{
  "userId": 1,
  "query": "我今天想吃辣的，预算30元以内"
}
```
