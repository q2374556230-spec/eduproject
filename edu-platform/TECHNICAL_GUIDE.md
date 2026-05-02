# 在线教育平台课设版技术说明

本文档面向项目组成员和 GitHub 读者，重点说明本项目涉及的技术栈、模块职责、本地运行方式，以及当前课设版重点实现的“课程库 + AI 课程推荐 + 前端展示”最小业务闭环。

## 1. 项目定位

本项目原始设计是一个在线教育微服务平台，包含用户、课程、订单、通知、网关、前端、MCP 工具服务、Docker 编排和 CI/CD 配置等内容。

当前课设版本已经将目标收缩为一个可演示、可部署、可解释的核心闭环：

```text
前端输入学习兴趣、当前水平、学习目标、推荐数量
        ↓
调用后端 AI 推荐接口
        ↓
后端查询已有课程库
        ↓
后端调用 Claude API，或在无 API Key 时使用本地 mock 推荐逻辑
        ↓
返回推荐课程、推荐理由、匹配度
        ↓
前端展示推荐结果
```

因此，课程服务、网关、前端、MySQL、Redis、Nacos 是当前演示的关键部分。订单、通知、RabbitMQ、Zipkin、MCP、Jenkins 等模块保留在项目中，但在课设演示中属于 optional 模块。

## 2. 总体技术栈

### 后端

| 技术 | 版本/说明 | 项目用途 |
|---|---|---|
| Java | JDK 17 | 后端开发语言 |
| Spring Boot | 3.2.4 | 微服务应用基础框架 |
| Spring Cloud | 2023.0.1 | 服务治理、网关、OpenFeign 等 |
| Spring Cloud Alibaba | 2023.0.1.0 | Nacos 服务注册与配置 |
| Spring Cloud Gateway | WebFlux 网关 | 统一入口、路由转发、JWT 过滤 |
| MyBatis-Plus | Boot 3 starter | ORM、分页、CRUD 简化 |
| MySQL | 8.0 | 存储课程、用户、订单、通知等业务数据 |
| Redis | 7 | 课程分类/详情缓存、JWT 黑名单等 |
| Spring Security | Spring Boot 集成 | 用户服务鉴权 |
| JWT | jjwt 0.12.5 | 登录态 token |
| OpenFeign | Spring Cloud OpenFeign | 服务间调用，当前课设主流程依赖较弱 |
| WebClient | Spring WebFlux | 调用 Claude API |
| Docker Compose | 本地编排 | 一键启动本地依赖和服务 |

### 前端

| 技术 | 说明 | 项目用途 |
|---|---|---|
| Vue 3 | 前端框架 | 页面与组件开发 |
| Vite | 构建工具 | 本地开发和生产构建 |
| Pinia | 状态管理 | 用户、课程等状态管理 |
| Vue Router | 路由 | 页面跳转 |
| Element Plus | UI 组件库 | 表单、按钮、卡片、表格等 |
| Axios | HTTP 客户端 | 调用后端 API |
| Nginx | 前端容器运行 | Docker 中托管静态页面并代理 API |

### AI 推荐

| 技术/方式 | 说明 |
|---|---|
| Anthropic Claude API | 有真实 `ANTHROPIC_API_KEY` 时可调用大模型生成推荐 |
| 本地 mock 推荐逻辑 | 无 API Key 或调用失败时使用，保证课设演示稳定 |
| 课程库匹配 | 基于课程标题、分类、标签、难度、热度进行推荐排序 |

## 3. 项目目录结构

```text
edu-platform/
├── common/
│   ├── common-core/              # 通用返回结果、异常处理、MyBatis/Redis 通用配置
│   └── common-security/          # JWT 工具、登录用户模型、用户上下文
├── gateway-service/              # API 网关，统一转发 /api/**
├── user-service/                 # 用户注册、登录、个人信息、JWT
├── course-service/               # 课程库、分类、AI 课程推荐，当前核心服务
├── order-service/                # 订单模块，课设演示中 optional
├── notification-service/         # 通知模块，课设演示中 optional
├── frontend/                     # Vue 3 前端
├── mcp-server/                   # MCP 工具服务，课设演示中 optional
├── sql/init.sql                  # MySQL 初始化脚本
├── docker-compose.yml            # 本地 Docker 编排
├── .env.example                  # 环境变量示例
└── README.md                     # 项目基础说明
```

## 4. 核心模块说明

### 4.1 course-service

课程服务是当前课设版最核心的后端服务，主要能力包括：

- 查询课程列表：`GET /api/course/list`
- 查询课程详情：`GET /api/course/{id}`
- 查询课程分类：`GET /api/course/category/list`
- AI 课程推荐：`GET /api/course/recommend`

AI 推荐接口支持以下参数：

| 参数 | 说明 | 示例 |
|---|---|---|
| `interest` | 学习兴趣 | `Java`、`前端开发`、`数据分析` |
| `level` | 当前水平 | `beginner`、`intermediate`、`advanced` |
| `goal` | 学习目标 | `完成课设`、`准备实习` |
| `limit` | 推荐数量 | `3` |

示例：

```http
GET http://localhost:18080/api/course/recommend?interest=Java&level=beginner&goal=完成课设&limit=3
```

返回结果包含：

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "title": "课程标题",
      "level": "beginner",
      "recommendReason": "推荐理由",
      "matchScore": 88
    }
  ]
}
```

### 4.2 gateway-service

网关负责统一入口和路由转发。本地 Docker 映射端口是：

```text
http://localhost:18080
```

核心路由：

| 外部路径 | 转发服务 |
|---|---|
| `/api/course/**` | `course-service` |
| `/api/user/**` | `user-service` |
| `/api/order/**` | `order-service` |
| `/api/notify/**` | `notification-service` |

为方便课设演示，以下接口已加入白名单，无需登录：

- `/api/user/register`
- `/api/user/login`
- `/api/course/list`
- `/api/course/category/list`
- `/api/course/recommend`

### 4.3 frontend

前端首页当前围绕课设主流程设计，主要区域包括：

- 课程搜索
- AI 推荐表单
- 推荐课程展示
- 课程分类
- 热门课程

AI 推荐表单字段：

- 学习兴趣
- 当前水平
- 学习目标
- 推荐数量

点击“生成推荐”后，前端调用：

```js
GET /api/course/recommend
```

然后展示课程卡片，每张卡片包含：

- 课程封面
- 课程标题
- 课程难度
- 推荐理由
- 匹配度
- 讲师
- 学习人数
- 价格

## 5. AI 推荐实现过程

### 5.1 数据来源

推荐系统不是凭空生成课程，而是先从本地课程库中查询已有课程。

后端逻辑位于：

```text
course-service/src/main/java/com/edu/course/service/impl/CourseServiceImpl.java
```

主要步骤：

1. 构造课程查询条件。
2. 按热度查询课程库中的课程。
3. 获取课程标题、分类、难度、标签、价格等摘要信息。
4. 判断是否有可用 Claude API Key。
5. 有 API Key 时调用 Claude。
6. 无 API Key 或调用失败时使用本地 mock 推荐。
7. 返回课程对象，并补充 `recommendReason` 和 `matchScore`。

### 5.2 Claude API 调用

Claude API 客户端位于：

```text
course-service/src/main/java/com/edu/course/client/ClaudeAiClient.java
```

实现方式：

- 使用 Spring WebClient 发起 HTTP 请求。
- 请求地址为 Anthropic Messages API。
- 请求头包含：
  - `x-api-key`
  - `anthropic-version`
  - `Content-Type`
- 请求体包含：
  - model
  - max_tokens
  - messages

AI 被要求只返回 JSON，例如：

```json
[
  {
    "id": 1,
    "reason": "适合 Java 入门并能支撑课程项目",
    "matchScore": 95
  }
]
```

后端再根据 `id` 匹配本地课程实体，避免 AI 推荐不存在的课程。

### 5.3 Mock 推荐降级

课设演示最怕外部 API key、网络或额度问题导致推荐功能无法展示。因此当前实现了本地 mock 推荐逻辑。

当满足以下情况时，会走 mock：

- `ANTHROPIC_API_KEY` 为空
- `ANTHROPIC_API_KEY` 是 placeholder
- Claude API 调用失败
- Claude 返回内容解析失败

mock 推荐会根据以下因素计算匹配度：

- 用户兴趣是否出现在课程标题、描述、分类或标签中
- 用户选择的难度是否和课程难度一致
- 学习目标是否能匹配课程文本
- 课程学习人数热度

最终返回：

- 推荐课程
- 推荐理由
- 匹配度分数

这样即使没有真实 AI key，项目仍然可以稳定演示完整业务流程。

## 6. 本地运行方式

### 6.1 环境要求

推荐环境：

- JDK 17
- Maven 3.8+
- Node.js 18+
- Docker Desktop
- Git

### 6.2 配置环境变量

复制环境变量示例：

```powershell
copy .env.example .env
```

关键变量：

```env
MYSQL_PASSWORD=edu123456
JWT_SECRET=edu-platform-jwt-secret-key-2024-very-long-secret
ANTHROPIC_API_KEY=sk-placeholder
NACOS_NAMESPACE=
```

如果没有 Claude API Key，保持 `sk-placeholder` 即可，系统会自动使用 mock 推荐。

### 6.3 构建后端

完整构建：

```powershell
mvn -DskipTests package
```

如果只演示课设核心流程，可以构建关键服务：

```powershell
mvn -DskipTests package -pl course-service,gateway-service -am
```

### 6.4 启动 Docker

完整启动：

```powershell
docker compose up -d --build
```

只重启课设主流程相关服务：

```powershell
docker compose up -d --build course-service gateway-service frontend
```

查看容器状态：

```powershell
docker compose ps
```

核心访问地址：

| 服务 | 地址 |
|---|---|
| 前端 | `http://localhost` |
| 网关 | `http://localhost:18080` |
| 课程服务直连 | `http://localhost:8082` |
| MySQL | `localhost:13306` |
| Nacos | `http://localhost:8848/nacos` |

## 7. 核心接口测试

### 7.1 课程列表

```powershell
Invoke-RestMethod -Uri http://localhost:18080/api/course/list
```

预期：

```text
code = 200
data.total > 0
```

### 7.2 课程分类

```powershell
Invoke-RestMethod -Uri http://localhost:18080/api/course/category/list
```

预期：

```text
code = 200
data 中有分类列表
```

### 7.3 AI 推荐

```powershell
Invoke-RestMethod -Uri "http://localhost:18080/api/course/recommend?interest=Java&level=beginner&goal=完成课设&limit=3"
```

预期：

```text
code = 200
data 数量 = 3
每个推荐结果包含 recommendReason 和 matchScore
```

### 7.4 前端演示

浏览器打开：

```text
http://localhost
```

操作流程：

1. 在首页 AI 推荐区域输入学习兴趣。
2. 选择当前水平。
3. 输入学习目标。
4. 设置推荐数量。
5. 点击“生成推荐”。
6. 查看推荐课程、推荐理由和匹配度。

## 8. 已修复或适配的关键问题

为了让本地 Docker 演示可用，项目做过以下重要适配：

- 增加 MySQL Docker 容器，并挂载 `sql/init.sql`。
- 修正 MySQL、Redis、RabbitMQ、Nacos 等环境变量。
- 将 Nacos 默认 namespace 改为 public，即 `NACOS_NAMESPACE=`。
- 增加 `bootstrap.yml`，让 Nacos 配置在 bootstrap 阶段可读取。
- 修复 Spring Boot 3 与 MyBatis-Plus 的兼容问题，改用 Boot 3 starter。
- 给 gateway、course、order 显式增加 loadbalancer 依赖，保证 `lb://service-name` 可解析。
- 将课程推荐接口加入网关白名单，便于无需登录演示。
- 修复课程服务 YAML 中的损坏配置。
- 前端 API facade 已对课程路径进行适配。
- 前端首页增加 AI 推荐输入表单和推荐结果展示。
- 后端增加无 API Key 时的 mock 推荐降级。

## 9. Optional 模块说明

以下模块在完整项目中存在，但当前课设版不是主线。

| 模块 | 当前状态 |
|---|---|
| order-service | 可启动，但课设主流程不依赖 |
| notification-service | 可启动，但通知列表等接口不是当前重点 |
| RabbitMQ | 订单通知链路使用，课设 AI 推荐不依赖 |
| Zipkin | 链路追踪，课设演示不依赖 |
| MCP Server | Claude Desktop 工具调用，课设演示不依赖 |
| Jenkinsfile | CI/CD 示例，课设演示不依赖 |
| Resilience4j | 熔断降级配置，当前不作为主要展示点 |

如果这些模块出现问题，优先判断是否影响以下主流程：

```text
前端推荐表单 → /api/course/recommend → course-service → 课程库 → AI/mock 推荐 → 前端展示
```

如果不影响，则记录为 optional issue，不建议优先投入时间修复。

## 10. GitHub 上传建议

### 10.1 上传前检查

建议上传前执行：

```powershell
git status
mvn -DskipTests package -pl course-service,gateway-service -am
cd frontend
npm run build
```

确认：

- 不上传真实 API Key。
- `.env` 不应提交。
- `.env.example` 可以提交。
- `target/`、`node_modules/`、`dist/` 通常不提交。
- `frontend/package-lock.json` 可以提交，用于锁定依赖。
- `mcp-server/package-lock.json` 如果保留 MCP 模块，也可以提交。

### 10.2 推荐 README 重点

GitHub README 中建议突出：

- 项目是在线教育平台课设版。
- 当前核心功能是 AI 课程推荐。
- 支持无 API Key 的 mock 推荐演示。
- 提供 Docker Compose 本地启动方式。
- 说明 optional 模块不属于当前主演示链路。

### 10.3 建议的 `.gitignore`

如果仓库还没有 `.gitignore`，建议至少包含：

```gitignore
.env
target/
node_modules/
dist/
.idea/
.vscode/
*.log
logs/
```

如果希望保留 VS Code 配置给组员，可以不要忽略 `.vscode/`，但不要提交个人机器路径、插件缓存或敏感配置。

## 11. 组员分工建议

可以按以下方向分工：

| 成员 | 建议负责内容 |
|---|---|
| 后端同学 A | 课程列表、分类、课程详情接口说明 |
| 后端同学 B | AI 推荐接口、Claude/mock 推荐逻辑说明 |
| 前端同学 | 首页推荐表单、课程卡片、推荐结果展示 |
| 部署同学 | Docker Compose、本地启动、环境变量 |
| 文档同学 | README、演示流程、接口测试截图 |

演示时建议只讲主流程，避免被旁支模块带偏：

1. 课程库数据来自 MySQL。
2. 前端输入推荐条件。
3. 网关转发到课程服务。
4. 课程服务查询课程库。
5. 有 AI Key 就调用 Claude，没有就使用 mock。
6. 返回课程、理由、匹配度。
7. 前端卡片展示结果。

## 12. 常见问题

### Q1：没有 Claude API Key 能演示吗？

可以。`.env` 中 `ANTHROPIC_API_KEY=sk-placeholder` 时，后端会自动使用本地 mock 推荐逻辑。

### Q2：为什么网关端口是 18080？

为了避免和本机其他服务的 8080 冲突，Docker Compose 将 gateway 的容器端口 8080 映射到了宿主机 18080。

### Q3：为什么前端地址是 localhost？

前端容器使用 Nginx 暴露 80 端口，因此浏览器直接访问：

```text
http://localhost
```

### Q4：订单、通知、MCP 需要演示吗？

当前课设目标下不需要。它们是保留模块或扩展模块，不影响 AI 推荐主流程。

### Q5：推荐理由是 AI 生成的吗？

如果配置了真实 Claude API Key，并且外网和额度正常，则推荐理由来自 Claude。否则来自本地 mock 推荐逻辑。两种情况返回结构一致，前端展示方式一致。

