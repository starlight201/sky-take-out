# 智能菜品推荐 Agent 二次开发落地说明

## 文档审核结论

原开发方案的主线成立：Spring Boot 保持业务系统职责，Python FastAPI/LangChain 负责 Agent 编排、推荐过滤、打分排序和自然语言解释。二开边界需要明确为：

1. 不新增 `dish_stock`，不判断实时库存、剩余份数、出餐份数。
2. 是否可推荐只依据店铺营业状态、菜品/套餐启售状态、分类状态、预算、标签和用户偏好。
3. Python Agent 不直连数据库，只通过 Spring Boot 内部接口获取真实业务数据。
4. 第一阶段先跑通确定性推荐逻辑，再接 LangChain 和大模型，避免一开始就被 API Key、模型稳定性和流式输出阻塞。

## 已补齐的落地细节

### 套餐标签

原文只设计了 `dish_tag`，没有单独设计套餐标签。当前实现不新增 `setmeal_tag`，套餐标签从套餐包含的菜品标签聚合得到：

```text
setmeal -> setmeal_dish -> dish_tag
```

这样可以保持数据结构简单，且符合原项目“套餐由菜品组成”的业务模型。

### 套餐销量

原文只设计了 `dish_sales_stat`。当前实现中：

- 菜品销量优先读取 `dish_sales_stat` 最近 30 天统计。
- 套餐销量从 `orders + order_detail` 最近 30 天已完成订单聚合。

后续如果需要日报性能优化，可以再增加 `setmeal_sales_stat`，但第一版不强行扩表。

### 内部接口返回格式

Spring Boot 项目已有统一响应体 `Result<T>`，因此内部接口实际返回：

```json
{
  "code": 1,
  "data": {}
}
```

Python Agent 客户端会自动取 `data` 字段。

## 当前开发阶段

已完成阶段：

1. 扩展推荐数据基础：新增 SQL 文件 `sql/recommend_agent_schema.sql`。
2. Spring Boot 内部 Agent 接口：
   - `GET /internal/agent/shop/status`
   - `GET /internal/agent/dishes/available`
   - `GET /internal/agent/setmeals/available`
   - `GET /internal/agent/users/{userId}/preferences`
   - `GET /internal/agent/users/{userId}/orders/recent?days=30`
   - `POST /internal/agent/recommend/log`
3. Spring Boot 用户推荐入口：
   - `POST /user/recommend-agent`
4. Python FastAPI 确定性推荐服务：
   - `POST /api/recommend`
   - 规则解析 intent
   - 过滤店铺未营业、停售、分类异常、超预算、忌口标签
   - 按标签、偏好、销量、价格、场景打分
   - 保存推荐会话、结果和工具调用日志
5. LangChain + 阿里云百炼接入：
   - 使用 `langchain-openai` 走百炼 OpenAI 兼容接口
   - `BAILIAN_BASE_URL` 默认 `https://dashscope.aliyuncs.com/compatible-mode/v1`
   - `BAILIAN_MODEL` 默认 `qwen-plus`
   - `BAILIAN_API_KEY` 从环境变量读取，不写入代码仓库
   - 未配置 Key 时自动降级为确定性推荐文案
6. SSE 流式输出：
   - Python：`POST /api/recommend/stream`
   - Spring Boot：`GET /user/recommend-agent/stream?query=...`
   - 事件类型：`metadata`、`message`、`done`
7. 后台管理接口：
   - `POST /admin/agent/dish-tags`
   - `PUT /admin/agent/dish-tags`
   - `DELETE /admin/agent/dish-tags/{id}`
   - `GET /admin/agent/dish-tags/page`
   - `GET /admin/agent/recommend-logs/page`
   - `GET /admin/agent/recommend-logs/{sessionId}`

## 下一阶段建议

1. 给 `dish_tag` 和 `user_food_preference` 增加后台管理页面。
2. 接入 LangChain，将 Spring Boot 内部接口封装为 Tools。
3. 增加 SSE：
   - Python `StreamingResponse`
   - Spring Boot 转发流
   - Vue 使用 `EventSource` 展示打字机效果
4. 为 `/internal/agent/**` 增加内部调用鉴权，例如 `X-Agent-Token`。

当前代码已支持可选内部鉴权：

- Spring Boot 配置：`sky.agent.internal-token`
- Python 环境变量：`SKY_AGENT_TOKEN`
- 请求头：`X-Agent-Token`

不配置 token 时方便本地开发；生产环境建议配置强随机 token。

## 验证方式

### Python Agent

```bash
cd food-agent-service
pip install -r requirements.txt
set BAILIAN_API_KEY=你的百炼APIKey
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

普通推荐：

```http
POST http://127.0.0.1:8000/api/recommend
Content-Type: application/json

{
  "userId": 1,
  "query": "我今天想吃辣的，预算30元以内"
}
```

流式推荐：

```http
POST http://127.0.0.1:8000/api/recommend/stream
Content-Type: application/json

{
  "userId": 1,
  "query": "我今天想吃辣的，预算30元以内"
}
```

### Spring Boot

普通推荐：

```http
POST http://127.0.0.1:8080/user/recommend-agent
authentication: 用户JWT
Content-Type: application/json

{
  "query": "我今天想吃辣的，预算30元以内"
}
```

流式推荐：

```http
GET http://127.0.0.1:8080/user/recommend-agent/stream?query=我今天想吃辣的，预算30元以内
authentication: 用户JWT
Accept: text/event-stream
```

后台标签分页：

```http
GET http://127.0.0.1:8080/admin/agent/dish-tags/page?page=1&pageSize=10
token: 管理端JWT
```

后台推荐日志：

```http
GET http://127.0.0.1:8080/admin/agent/recommend-logs/page?page=1&pageSize=10
token: 管理端JWT
```
