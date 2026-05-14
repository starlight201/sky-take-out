# 苍穹外卖智能菜品推荐 Agent 二开文档

## 1. 项目定位

本次二开在苍穹外卖原有用户点餐、菜品、购物车和订单能力之上，新增“智能菜品推荐 Agent”。核心不是让大模型直接聊天，而是让后端先基于真实菜品数据完成过滤、打分和排序，再由 Agent 组织推荐理由。

一句话面试表达：

> 我在苍穹外卖上做了智能推荐升级，用户输入自然语言需求后，系统会解析预算、口味、忌口和场景，查询真实菜品、上架状态、标签、销量和用户偏好，经过规则过滤与加权评分返回 Top N 推荐结果，并支持 Agent 包装回答和 SSE 流式输出。

## 2. 已完成的代码改造

新增 POJO：

- `DishTag`：菜品标签，如辣、清淡、低脂、含香菜、适合午餐。
- `UserFoodPreference`：用户饮食偏好，如喜欢辣、不吃香菜。
- `DishSalesStat`：菜品销量统计，用于热度评分。
- `RecommendSession` / `RecommendResult`：记录推荐链路，方便排查和面试讲追踪。
- `AgentCallLog`：记录千问调用的 prompt、回答、耗时和失败原因。
- `RecommendRequest` / `RecommendIntentDTO` / `RecommendDishDTO` / `RecommendResponseVO` / `RecommendAgentResponseVO`。

新增后端接口：

- `POST /user/recommend`：规则推荐接口。
- `POST /user/agent/recommend`：Agent 推荐接口，返回自然语言回答和推荐菜品。
- `GET /user/agent/recommend/stream`：SSE 流式推荐接口。
- `GET /admin/agent/config`：查看千问配置是否生效，不返回明文 API Key。
- `GET /admin/agent/qwen/ping`：测试千问兼容 OpenAI 接口是否可用。
- `POST /admin/agent/recommend/raw`：后台调试规则推荐结果。
- `POST /admin/agent/recommend`：后台调试完整 Agent 推荐链路。
- `GET /admin/agent/logs`：查看最近 Agent 调用日志。
- `POST /user/preference`、`GET /user/preference/{userId}`、`DELETE /user/preference/{id}`：用户偏好管理。
- `POST /admin/dish/tag`、`GET /admin/dish/{dishId}/tags`、`DELETE /admin/dish/tag/{id}`：菜品标签管理。
新增 SQL：

- `sql/recommend_agent_schema.sql`

## 3. 数据库流程

第一步执行 `sql/recommend_agent_schema.sql`，新增五张扩展表。

核心表关系：

- `dish` 是原有菜品主表。
- `dish_tag` 通过 `dish_id` 给菜品补充推荐标签。
- `dish_sales_stat` 通过 `dish_id` 记录销量热度。
- `user_food_preference` 通过 `user_id` 记录用户偏好。
- `recommend_session` 和 `recommend_result` 记录每次推荐的输入、意图和结果。
- `agent_call_log` 记录每次大模型调用，便于排查模型失败、耗时或兜底情况。

面试讲法：

> 我没有侵入原来的菜品主表，而是通过扩展表补充推荐所需的特征数据。这样原有点餐流程稳定不受影响，推荐模块可以独立演进。

## 4. 标签管理流程

后台先给菜品打标签：

```http
POST /admin/dish/tag
```

```json
{
  "dishId": 1,
  "tagName": "辣",
  "tagType": "taste",
  "weight": 1.0
}
```

流程说明：

1. 商家端录入菜品标签。
2. 推荐时查询原有 `dish` 表中的上架菜品。
3. 根据 `dish_tag` 给候选菜品补齐标签。
4. 结合预算、忌口、偏好和销量进行排序。

面试讲法：

> 苍穹外卖原项目没有库存模型，所以我没有额外引入库存复杂度，而是复用原有菜品上架/停售状态作为可推荐的硬约束，重点扩展标签、偏好、销量和推荐日志。

## 5. 用户偏好流程

用户偏好可以来自手动设置、历史订单分析或 Agent 对话提取。当前已实现手动设置入口：

```http
POST /user/preference
```

```json
{
  "userId": 1,
  "preferenceType": "dislike",
  "preferenceValue": "香菜",
  "source": "manual",
  "weight": 1.0
}
```

推荐时会读取 `user_food_preference`：

- `like` / `taste` 命中后加分。
- `dislike` / `allergy` 命中后降分。
- 查询参数里明确的忌口会直接过滤。

面试讲法：

> 用户当次输入是短期意图，用户偏好是长期画像。推荐排序时我把两者结合起来，当次忌口优先级最高，长期偏好用于加权排序。

## 6. 自然语言意图解析流程

入口类：`RecommendIntentParser`

当前规则解析内容：

- 预算：`30元以内`、`30块`。
- 口味：辣、微辣、重辣、清淡、酸甜。
- 忌口：不吃香菜、不吃牛肉、不要花生、不要辣。
- 场景：午餐、晚餐、健身、减脂、下饭、家常。

示例：

```text
我今天想吃辣的，预算30元以内，不要香菜
```

解析结果：

```json
{
  "tasteTags": ["辣"],
  "budget": 30,
  "excludeTags": ["香菜", "含香菜"]
}
```

面试讲法：

> MVP 阶段我先用规则解析保证稳定性，后续可以把意图解析替换成大模型 JSON 输出，但推荐结果仍然由后端算法决定。

## 7. 推荐过滤流程

入口类：`RecommendFilter`

过滤顺序：

1. 只查询原有 `dish.status = 1` 的上架菜品。
2. 过滤价格超过预算的菜品。
3. 过滤命中忌口标签、菜品名或描述的菜品。

面试讲法：

> 过滤是硬约束，比如停售、超预算和忌口；打分是软排序，比如销量高、标签更匹配。

## 8. 推荐打分流程

入口类：`RecommendScorer`

评分公式：

```text
score =
标签匹配分 * 0.35
+ 用户偏好分 * 0.25
+ 销量热度分 * 0.20
+ 价格适配分 * 0.10
+ 场景适配分 * 0.10
```

各项含义：

- 标签匹配分：用户想吃辣，菜品有辣/微辣/重辣标签则得分更高。
- 用户偏好分：长期喜欢的口味加分，长期忌口降分。
- 销量热度分：根据 `dish_sales_stat` 的销量占比归一化。
- 价格适配分：在预算内且价格接近预算区间时得分更高。
- 场景适配分：午餐、健身、减脂等场景标签命中则加分。

面试讲法：

> 我把推荐拆成可解释的多因子打分，而不是黑盒返回，这样可以根据业务反馈调整权重，也便于解释为什么推荐这个菜。

## 9. 推荐主链路流程

入口类：`RecommendCoreServiceImpl`

完整链路：

1. 接收 `userId` 和 `query`。
2. 使用 `RecommendIntentParser` 解析自然语言意图。
3. 查询用户偏好。
4. 查询所有上架候选菜品。
5. 加载菜品标签。
6. 使用 `RecommendFilter` 过滤不可推荐菜品。
7. 使用 `RecommendScorer` 计算分数。
8. 使用 `RecommendReasonBuilder` 生成推荐理由。
9. 按分数倒序取 Top N。
10. 保存 `recommend_session` 和 `recommend_result`。
11. 返回推荐结果。

面试讲法：

> 推荐链路里最关键的是先过滤再打分，过滤保证推荐结果可用，打分保证推荐结果更符合用户需求。

## 10. Agent 流程

入口类：`RecommendAgentServiceImpl`

千问配置：

- `QwenProperties` 读取 `sky.qwen` 配置。
- API Key 只从环境变量 `DASHSCOPE_API_KEY` 读取，不写入代码和配置文件。
- 默认模型：`qwen-plus`。
- 默认地址：`https://dashscope.aliyuncs.com/compatible-mode/v1`。

启动前设置环境变量：

```bash
DASHSCOPE_API_KEY=你的DashScope API Key
```

当前实现：

- 先调用 `RecommendCoreService` 得到真实推荐结果。
- 再把用户问题、解析意图和推荐结果组装成 Prompt。
- 优先调用千问 `qwen-plus` 生成自然语言回答。
- 如果千问调用失败，则回退到本地模板回答。
- 每次调用都会写入 `agent_call_log`。
- Prompt 统一封装在 `RecommendPromptTemplate`，Agent Service 只负责编排流程。

预留扩展：

- `DishRecommendTool` 已封装为工具类。
- 后续接入 Spring AI 时，可以把 `DishRecommendTool.recommend` 注册为 Tool Calling 工具。
- 大模型只负责理解表达和生成话术，不负责编造菜品、价格和上架状态。

面试讲法：

> Agent 的边界是：推荐结果由后端算法和数据库决定，千问负责把真实推荐结果组织成自然语言解释。如果千问不可用，系统会回退到本地模板回答，不影响推荐主流程。

后台调试顺序：

1. `GET /admin/agent/config`：确认 `apiKeyConfigured=true`。
2. `GET /admin/agent/qwen/ping?message=你是谁`：确认千问接口可达。
3. `POST /admin/agent/recommend/raw`：确认规则推荐有结果。
4. `POST /admin/agent/recommend`：确认千问包装回答正常。
5. `GET /admin/agent/logs`：查看 prompt、回答、耗时和失败原因。

## 11. SSE 流式输出流程

接口：

```http
GET /user/agent/recommend/stream?userId=1&query=我想吃辣的，预算30元以内
```

事件：

- `delta`：回答片段，前端可逐段追加实现打字机效果。
- `answer`：自然语言推荐回答。
- `items`：推荐菜品列表。
- `done`：结束标记。

前端可以用 `EventSource` 接收：

```js
const source = new EventSource('/user/agent/recommend/stream?userId=1&query=我想吃辣的')
source.addEventListener('answer', e => console.log(e.data))
source.addEventListener('items', e => console.log(JSON.parse(e.data)))
source.addEventListener('done', () => source.close())
```

面试讲法：

> SSE 比普通 HTTP 更适合智能助手回答展示，前端可以先展示回答，再同步渲染推荐菜品卡片。

## 12. 推荐日志流程

每次推荐都会写入：

- `recommend_session`：用户原始问题和解析意图。
- `recommend_result`：每个推荐菜品的分数、理由和排名。

作用：

- 排查为什么某个菜被推荐。
- 复盘某次推荐的输入和结果。
- 为后续反馈学习、AB 测试和权重调整留数据。

面试讲法：

> 我设计了推荐链路日志，不只是返回结果，还能追踪意图解析、排序分数和最终排名，方便定位推荐不准的问题。

## 13. 调试建议

1. 先启动原苍穹外卖，确认菜品接口正常。
2. 执行 `sql/recommend_agent_schema.sql`。
3. 给几个上架菜品录入标签和销量统计数据。
4. 插入一些销量统计数据。
5. 调用 `POST /user/recommend` 验证规则推荐。
6. 调用 `POST /user/agent/recommend` 验证 Agent 包装回答。
7. 调用 SSE 接口验证流式输出。

## 14. 简历写法

项目描述：

> 基于苍穹外卖系统进行智能推荐二次开发，设计并实现智能菜品推荐 Agent。用户可通过自然语言输入口味、预算、忌口和用餐场景，系统结合菜品标签、上架状态、销量、用户偏好和价格进行规则过滤与加权排序，返回 Top N 推荐结果，并支持 Agent 自然语言解释和 SSE 流式输出。

技术亮点：

- 通过 `dish_tag`、`user_food_preference`、`dish_sales_stat` 扩展原有业务数据，支撑个性化推荐。
- 使用规则解析抽取预算、口味、忌口和场景，实现稳定可控的 MVP。
- 基于过滤 + 多因子加权评分实现推荐排序，兼顾业务可用性和个性化。
- 封装 `DishRecommendTool`，为 Spring AI Tool Calling 接入预留扩展点。
- 记录推荐会话和推荐结果，实现推荐链路可追踪。

## 15. 面试追问回答

问：为什么不直接让大模型推荐？

答：因为大模型可能编造菜品、价格和上架状态。我的方案是由后端查询真实业务数据并完成过滤打分，大模型只负责理解自然语言和生成解释，保证推荐结果可信。

问：用户说“不吃香菜”怎么处理？

答：当次输入解析出忌口标签后，过滤阶段会直接排除菜品标签、名称或描述中命中香菜的菜品。如果这是长期偏好，还可以写入 `user_food_preference`，后续推荐持续生效。

问：推荐不准怎么排查？

答：查看 `recommend_session` 里的解析意图，再看 `recommend_result` 的分数、理由和排名，判断是意图解析错、标签数据缺失，还是权重设置不合理。
