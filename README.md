# 餐饮服务平台开发 - 后端开发

> 基于 Spring Boot + MyBatis-Plus 构建的高性能餐饮外卖平台后端服务

## 📖 项目简介

本项目是一个面向餐饮行业的综合性服务平台后端系统，提供完整的商家管理、用户端、订单处理、营销活动及数据统计等功能。后端采用微服务架构思想，模块化设计，确保高并发场景下的稳定性和可扩展性。

原项目代号“苍穹外卖”，现正式更名为**餐饮服务平台开发-后端开发**，专注打造餐饮数字化基础设施。

## ✨ 核心功能

### 商家端（管理后台）
- 菜品管理：分类、菜品、口味、套餐的增删改查及上下架
- 订单管理：订单查询、状态跟踪、催单处理、批量接单/拒单
- 员工管理：账号 CRUD、角色权限分配
- 数据统计：营业额、用户量、订单量、热门菜品等运营报表
- 营销工具：满减活动、新用户折扣、优惠券发放

### 用户端（小程序/App API）
- 用户登录授权（微信小程序 + 手机号）
- 地址簿管理（增删改查、默认地址）
- 购物车操作（添加、减少、清空、复购）
- 订单流程：下单、支付模拟、取消、再来一单
- 历史订单查询与评价

### 通用模块
- 统一 JWT 鉴权
- 全局异常处理与统一响应格式
- 阿里云 OSS 图片上传
- 微信支付（模拟/真实）
- 定时任务（订单超时自动取消）

## 🛠️ 技术栈

| 类别       | 技术选型                                      |
|----------|-------------------------------------------|
| 核心框架    | Spring Boot 2.7.x, Spring MVC            |
| ORM      | MyBatis-Plus 3.5.x                        |
| 数据库     | MySQL 8.0 + 主从复制（读写分离可选）               |
| 缓存       | Redis 7.0（购物车、验证码、token 黑名单）           |
| 消息队列    | RabbitMQ（订单延迟取消通知）                    |
| 任务调度    | Spring Scheduled + Quartz               |
| 接口文档    | Swagger 2 / Knife4j                       |
| 对象存储    | 阿里云 OSS                                  |
| 日志       | SLF4J + Logback（按天滚动，ELK 可选）            |
| 工具库     | Lombok, Hutool, Fastjson, Guava          |
| 部署       | Docker + Docker Compose + Nginx          |

## 📂 项目结构

```
sky-take-out/          # 后端根目录
├── sky-common        # 公共模块（工具类、常量、异常、枚举）
├── sky-pojo          # 实体类、DTO、VO
├── sky-mapper        # MyBatis-Plus Mapper 层
├── sky-service       # 业务逻辑层（接口+实现）
├── sky-controller    # 控制器层（REST API）
├── sky-interceptor   # 拦截器、切面（JWT 校验、日志）
├── sky-task          # 定时任务模块
└── sky-admin         # 商家端启动模块（配置、启动类）
```

## 🚀 快速开始

### 环境要求
- JDK 1.8 或 11
- Maven 3.6+
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.12+（可选，超时订单需要）
- Git

### 1. 克隆代码
```bash
git clone https://github.com/your-group/sky-backend.git
cd sky-backend
```

### 2. 数据库初始化
创建数据库 `sky_take_out`，导入 `docs/sky.sql` 脚本（表结构 + 初始数据）。

修改 `application-dev.yml` 中的 MySQL、Redis、OSS 等配置。

### 3. 启动依赖服务
```bash
# 使用 Docker Compose 快速启动 Redis + MySQL + RabbitMQ
cd docker
docker-compose up -d
```

### 4. 编译运行
```bash
mvn clean package
java -jar sky-admin/target/sky-admin.jar --spring.profiles.active=dev
```

启动后访问：
- 商家端接口：`http://localhost:8080/admin/...`
- 用户端接口：`http://localhost:8080/user/...`
- Swagger 文档：`http://localhost:8080/doc.html`

## 📡 API 文档示例

| 模块     | 方法 | 端点                         | 说明          |
|--------|-----|----------------------------|-------------|
| 员工管理   | POST | `/admin/employee/login`    | 登录（返回 JWT）  |
| 菜品管理   | GET  | `/admin/dish/page`          | 分页查询菜品      |
| 用户端购物车 | POST | `/user/shoppingCart/add`    | 添加菜品至购物车    |
| 订单模块   | POST | `/user/order/submit`        | 提交订单        |
| 统计报表   | GET  | `/admin/report/turnover`    | 营业额统计       |

详细接口请查看 Knife4j 文档。

## ⚙️ 配置说明

主要配置文件：
- `application.yml`：通用配置
- `application-dev.yml`：开发环境（数据库、Redis、微信等）
- `application-prod.yml`：生产环境（使用环境变量注入敏感信息）

关键配置项：
```yaml
sky:
  jwt:
    admin-secret-key: your_jwt_secret
    admin-ttl: 7200000   # token 有效期（ms）
  alioss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    bucket-name: your-bucket
  wechat:
    appid: wx...
    secret: ...
```

## 🧪 测试与部署

### 单元测试
```bash
mvn test
```

### 构建 Docker 镜像
```bash
docker build -t sky-backend:latest .
```

### 生产部署（Docker Compose）
```bash
docker-compose -f docker-compose-prod.yml up -d
```

## 📊 性能与监控

- 使用 Redis 缓存热点数据（菜品分类、优惠券模板）
- 接口响应时间 < 200ms（P99）
- 支持 QPS 1000+（单机，4C8G 配置）
- 集成 Actuator + Micrometer 提供 `/actuator/metrics`，可接入 Prometheus

## 🤝 贡献指南

1. Fork 本仓库
2. 新建 `feature/xxx` 分支
3. 提交代码前执行 `mvn spotless:apply` 格式化
4. 发起 Pull Request 到 `dev` 分支

## 📄 许可证

[MIT](LICENSE)

## 📧 联系方式

项目维护者：开发团队  
问题反馈：https://github.com/starlight201/sky-take-out/issues  

**注**：本项目为学习/生产级代码，默认集成了模拟支付。实际部署请替换真实微信支付配置，并开启 HTTPS。
