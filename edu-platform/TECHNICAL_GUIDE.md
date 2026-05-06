# 在线教育平台技术说明

本文档用于说明本项目的总体架构、模块职责、当前完成度和后续待补齐内容。项目整体按照“用户访问层 -> 网关层 -> 业务服务层 -> 基础设施层”的微服务架构组织。

## 1. 总体架构

```text
用户访问层
  前端 / 浏览器

网关层
  Spring Cloud Gateway

业务服务层
  User Service / Course Service / Order Service / Notification Service

基础设施层
  Nacos / MySQL / Redis / RabbitMQ / Zipkin / Kubernetes / Prometheus / Grafana
```

当前工程已经落地了前端、网关、用户服务、课程服务、订单服务、通知服务，以及 Docker Compose 版本的基础设施。Kubernetes、Prometheus、Grafana 更偏向部署与可观测扩展，目前主要作为架构规划内容保留。

## 2. 架构分层说明

### 2.1 用户访问层

用户访问层由 `frontend` 提供，技术栈为 Vue 3、Vite、Element Plus、Pinia、Vue Router 和 Axios。

主要职责：

- 提供课程列表、课程详情、登录注册、个人中心、订单列表、通知抽屉等页面。
- 通过统一的前端 API 封装访问后端接口。
- 在 Docker 环境中由 Nginx 托管静态资源，并代理 API 请求到网关。

### 2.2 网关层

网关层由 `gateway-service` 提供，基于 Spring Cloud Gateway。

主要职责：

- 统一暴露 `/api/**` 入口。
- 根据路径转发到 user、course、order、notification 等业务服务。
- 进行 JWT 校验，并将用户信息写入 `X-User-Id`、`X-User-Name`、`X-User-Role` 请求头传递给下游服务。
- 配置跨域、限流、Swagger 聚合和链路追踪。

核心路由：

| 外部路径 | 下游服务 |
|---|---|
| `/api/user/**` | `user-service` |
| `/api/course/**` | `course-service` |
| `/api/order/**` | `order-service` |
| `/api/notify/**` | `notification-service` |

### 2.3 业务服务层

业务服务层按领域拆分为 4 个服务。

| 服务 | 端口 | 当前状态 | 职责 |
|---|---:|---|---|
| `user-service` | 8081 | 基本完成 | 用户注册、登录、JWT 签发、用户管理、个人资料维护 |
| `course-service` | 8082 | 基本完成 | 课程列表、课程详情、分类管理、课程管理、AI/mock 推荐 |
| `order-service` | 8083 | 部分完成 | 订单创建、订单查询、模拟支付、取消订单、订单统计、支付事件发布 |
| `notification-service` | 8084 | 部分完成 | 站内通知查询、未读数、标记已读、消费订单支付事件生成通知 |

当前主业务闭环可以概括为：

```text
用户登录
  -> 浏览课程
  -> 查看课程详情
  -> 创建课程订单
  -> 模拟支付订单
  -> RabbitMQ 发布订单支付事件
  -> 通知服务消费事件
  -> 生成站内通知
  -> 前端通知抽屉展示
```

其中 user、course 是较完整的核心服务；order、notification 已有可运行骨架和部分接口，但仍需要补齐真实业务规则、权限控制和异常保障。

### 2.4 基础设施层

当前 `docker-compose.yml` 已提供本地开发和演示所需的基础设施。

| 组件 | 当前状态 | 用途 |
|---|---|---|
| Nacos | 已接入 | 服务注册与配置中心 |
| MySQL | 已接入 | 存储用户、课程、订单、通知等业务数据 |
| Redis | 已接入 | 网关限流、课程缓存等 |
| RabbitMQ | 已接入 | 订单支付事件通知 |
| Zipkin | 已接入 | 分布式链路追踪 |
| Kubernetes | 规划中 | 生产级容器编排 |
| Prometheus | 规划中 | 指标采集 |
| Grafana | 规划中 | 指标看板与告警展示 |

## 3. 当前完成度

### 已完成或基本可演示

- 前端页面和主要路由已具备。
- 网关路由、跨域、JWT 过滤和请求头透传已具备。
- 用户注册、登录、资料管理和用户管理已具备。
- 课程查询、课程详情、分类、后台课程管理和 AI/mock 推荐已具备。
- Docker Compose 可启动 MySQL、Redis、RabbitMQ、Nacos、Zipkin、各业务服务和前端。
- 后端 Maven 构建可通过。
- 前端 Vite 构建可通过。

### 尚未完全完成

`order-service` 和 `notification-service` 目前属于“已有基础实现，但业务完整性不足”的状态。

## 4. Order Service 当前状态

### 已有能力

- `POST /api/order/create`：创建课程订单。
- `GET /api/order/list`：查询当前用户订单。
- `GET /api/order/{orderId}`：查询订单详情。
- `POST /api/order/{orderId}/pay`：模拟支付。
- `POST /api/order/{orderId}/cancel`：取消订单。
- `GET /api/order/admin/list`：管理员订单列表。
- `GET /api/order/admin/stats`：订单统计。
- 通过 Feign 调用课程服务获取课程快照信息。
- 支付成功后向 RabbitMQ 发布订单支付事件。

### 待完成能力

- 真实支付链路：支付宝、微信或第三方支付回调校验。
- 订单状态机：待支付、已支付、已取消、已退款等状态需要统一约束。
- 支付幂等：重复点击支付、重复回调、重复消息需要保证不会重复处理。
- 超时取消：待支付订单超过指定时间自动关闭。
- 退款流程：退款申请、退款审核、退款完成、课程学习权限回收。
- 权限校验：管理员接口需要明确校验角色，普通用户只能操作自己的订单。
- 异常补偿：课程服务调用失败、RabbitMQ 发送失败时需要补偿或重试机制。
- 测试覆盖：创建订单、重复购买、支付、取消、统计等核心路径应补单元测试和集成测试。

## 5. Notification Service 当前状态

### 已有能力

- `GET /api/notify/my`：查询当前用户通知。
- `GET /api/notify/list`：按用户查询通知。
- `GET /api/notify/unread-count`：查询当前用户未读通知数。
- `PUT /api/notify/{id}/read`：标记单条通知已读。
- `PUT /api/notify/read-all`：标记全部通知已读。
- 监听 RabbitMQ 订单支付队列。
- 支付事件消费成功后写入站内通知表。

### 待完成能力

- 通知服务层：目前控制器直接使用 Mapper，建议补 `NotificationService` 统一承载业务逻辑。
- 权限校验：标记已读时应校验通知是否属于当前用户；管理员查询接口应校验角色。
- 消息可靠性：需要补死信队列、重试策略、消费幂等和重复消息处理。
- 通知类型扩展：系统通知、课程通知、订单通知、营销通知等类型可统一枚举。
- 实时推送：如需更完整体验，可增加 WebSocket 或 SSE。
- 多渠道通知：站内信之外可扩展邮件、短信或企业微信。
- 前端刷新机制：通知抽屉已能展示数据，但未读数刷新和已读状态同步还可完善。
- 测试覆盖：消费订单事件、查询通知、标记已读、权限隔离等路径应补测试。

## 6. 推荐后续开发顺序

1. 先补 order、notification 的权限校验，避免用户越权访问订单或通知。
2. 为通知模块增加 `NotificationService`，把控制器中的查询和更新逻辑下沉。
3. 为订单支付事件增加幂等处理，避免重复消费生成多条通知。
4. 增加 RabbitMQ 死信队列和失败日志，保证消息异常可追踪。
5. 补订单状态流转和超时取消。
6. 最后再接入真实支付、WebSocket、Prometheus、Grafana、Kubernetes 等扩展能力。

## 7. 本地验证命令

后端构建：

```powershell
mvn -DskipTests package
```

前端构建：

```powershell
cd frontend
npm run build
```

Docker 启动：

```powershell
docker compose up -d --build
```

服务状态：

```powershell
docker compose ps
```

主要访问地址：

| 入口 | 地址 |
|---|---|
| 前端 | `http://localhost` |
| API 网关 | `http://localhost:18080` |
| Nacos | `http://localhost:8848/nacos` |
| RabbitMQ 管理台 | `http://localhost:15672` |
| Zipkin | `http://localhost:19411` |

## 8. 结论

本项目整体已经符合预期的四层微服务架构：前端负责用户访问，网关负责统一入口和认证转发，业务服务层按 user、course、order、notification 拆分，基础设施层提供注册配置、数据库、缓存、消息队列和链路追踪。

需要特别说明的是：`order-service` 和 `notification-service` 不是完全空缺，而是处于“基础闭环可运行、生产级业务能力待完善”的阶段。后续重点应放在权限、幂等、状态机、消息可靠性和测试覆盖上。
