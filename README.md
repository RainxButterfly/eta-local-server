<div align="center">

# 🖥️ ETA 本地后端 · `eta-backend`

**面向 ETA 桌面应用的本地离线后端**

单体 Spring Boot 服务，零外部依赖、即启即用——随 Electron 桌面应用一同打包并自动拉起，
让**任务管理 · 简历 · 错题本 · 笔记 · 统计**全部在本地 H2 数据库中闭环运行，
数据留在用户本机，离线依然完整可用。

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-007396.svg" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F.svg" alt="Spring Boot 4.1" />
  <img src="https://img.shields.io/badge/JPA-Hibernate-59666C.svg" alt="Spring Data JPA" />
  <img src="https://img.shields.io/badge/Auth-JWT-blue.svg" alt="JWT" />
  <img src="https://img.shields.io/badge/Database-H2-2AA9E0.svg" alt="H2" />
  <img src="https://img.shields.io/badge/License-AGPL--3.0-orange.svg" alt="License" />
</p>

**简体中文** · [接口文档](../ETA-api.yapi) · [协议](LICENSE)

</div>

---

## ✨ 为什么单独一个本地后端？

ETA 采用「本地优先、云端可选」的双后端架构。**本仓库是其中负责本地闭环的那一套**：

| | 本地后端（本仓库） | 云端后端（规划） |
|:---|:---|:---|
| 定位 | 随 Electron 打包、离线自用 | 多云同步、多设备协同 |
| 形态 | 单体 Spring Boot + H2 内存库 | 独立微服务（含 Python AI） |
| 数据 | 存于用户本机 | 存于云端 |
| 耦合 | 与应用打包，互不依赖 | 与应用解耦部署 |

> 本仓库**刻意不做微服务拆分**——单机离线场景下，一个简单可靠的单体即是正确架构。

## 🎯 核心设计

| | |
|:---|:---|
| 📦 **零外部依赖** | 内嵌 H2 内存数据库 + 内嵌 JWT 认证，`java -jar` 一条命令跑起来，无需安装数据库 |
| 🔐 **无状态认证** | 全部接口走 JWT Bearer，无 Session、无服务端状态，天然适配前后端分离与桌面端调用 |
| 🧱 **按业务分包** | Package-by-feature：`auth/`、`task/`、`resume/`… 业务内聚，边界清晰，未来平滑迁移云端微服务 |
| 🔑 **UUID 主键** | 用户与资源统一使用 UUID 主键，本地生成、全局唯一，为未来本地↔云端同步免去 ID 翻译 |
| 📋 **统一响应** | 所有接口返回 `{ code, message, data }`，`code = 200` 表示成功，前后端契约稳定 |
| 🔍 **可观测** | 暴露 `/actuator/health`，Electron 主进程据此探测后端是否就绪 |

## 🗺️ 架构概览

```
┌───────────────────────── Desktop App (Electron) ─────────────────────────┐
│  front (React + TS) ──── HTTP /api/v1 ───►  launcher 自动拉起并探测就绪   │
└──────────────────────────────────────┬───────────────────────────────────┘
                                       │
┌──────────────────────────────────────┴───────────────────────────────────┐
│  eta-backend  (Spring Boot, 端口 8080, 前缀 /api/v1)                       │
│                                                                          │
│   Controller ──► Service ──► Repository ──► H2 (内存)                    │
│        │            │                                                    │
│        └── JWT Filter（无状态认证，除 auth/health/下载外均需令牌）         │
│                                                                          │
│   Bcrypt 密码哈希  ·  UUID 主键  ·  统一 Result 响应                       │
└──────────────────────────────────────────────────────────────────────────┘
```

## 🧰 技术栈

- **Java 21** — 现代 LTS，`record`、模式匹配等特性让 DTO/校验代码更简洁
- **Spring Boot 4.1** — 应用骨架、依赖注入、AOT 友好的自动配置
- **Spring Data JPA (Hibernate)** — ORM 数据访问，派生查询，无需手写 SQL（替代 MyBatis）
- **Spring Security + JWT (jjwt 0.12)** — 无状态认证，BCrypt 密码存储，方法级鉴权
- **H2** — 内嵌内存数据库，MySQL 兼容模式，启动即建表
- **Lombok** — 消除样板代码
- **Actuator** — 健康检查 / info 端点，供 Electron 探测

## 📁 项目结构

```
back/
├── pom.xml                      # Maven 构建，finalName=eta-backend
├── LICENSE                      # AGPL-3.0
└── src/main/java/cn/eta/team/eta/
    ├── EtaApplication.java      # 启动入口
    ├── auth/                    # 用户与认证（按业务模块分包）
    ├── task/                    # 任务 + 任务分类
    ├── resume/                  # 简历 + 简历模板
    ├── module/                  # 接口存根：错题/笔记/统计/设置/备份/插件/搜索/同步/反馈/OCR
    ├── common/                  # 统一响应、异常、错误码、工具集
    ├── config/                  # 种子数据初始化
    ├── security/                # JWT 过滤器 / 安全配置
    └── download/                # 文件下载（导出资源）
```

## 🚀 快速开始

### 环境要求

- [JDK 21](https://adoptium.net/) 或更高
- （可选）已安装 Maven 3.9+；否则使用项目自带的 `mvnw` wrapper

### 本地运行

```bash
# 使用 Maven wrapper 启动（首次会自动下载依赖）
./mvnw spring-boot:run
```

或打包后运行：

```bash
# 构建可执行 jar
./mvnw clean package

# 运行（产物在 target/eta-backend.jar）
java -jar target/eta-backend.jar
```

启动后在 `http://127.0.0.1:8080/api/v1` 提供服务，H2 控制台位于 `http://127.0.0.1:8080/api/v1/h2-console`。

### 演示账号

首次启动会写入一条演示数据：

```
邮箱：demo@eta.cn
密码：123456
```

## 🔐 认证与请求示例

认证采用 **JWT Bearer**：注册 / 登录获取令牌，后续请求在 `Authorization` 头携带。

```bash
# 1. 登录，获取 token
curl -X POST http://127.0.0.1:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@eta.cn","password":"123456"}'
# → { "code":200, "message":"success", "data": { "token":"...", "user": {...} } }

# 2. 携带 token 访问受保护资源
curl http://127.0.0.1:8080/api/v1/tasks \
  -H "Authorization: Bearer <你的token>"
```

**放行规则**（`SecurityConfig`）：

| 路径 | 说明 |
|:---|:---|
| `/auth/**` | 注册 / 登录等认证接口，公开 |
| `/actuator/health` · `/actuator/info` | 健康检查，公开 |
| `/download/**` | 文件下载，公开 |
| `/h2-console/**` | H2 控制台（仅开发） |
| 其余 | 需携带有效 Bearer 令牌 |

## 📡 接口总览（共 60 个）

> 详细字段、必填标注与示例见 [接口文档](../ETA-api.yapi)（YApi 格式，可直接导入 Apifox）。

| 模块 | 端点 | 说明 |
|:---|:---|:---|
| 认证 `auth` | `POST /auth/register` · `/auth/login` · `/auth/logout` · `GET /auth/me` · `PUT /auth/me` · `PUT /auth/password` | 注册 / 登录 / 个人资料 |
| 任务 `task` | `GET|POST /tasks` · `GET|PUT|DELETE /tasks/{id}` · `PATCH /tasks/{id}/status` · `GET|POST /tasks/categories` | 任务与分类全生命周期 |
| 简历 `resume` | `GET /resumes/templates` · `GET|POST /resumes` · `GET|PUT|DELETE /resumes/{id}` · `POST /resumes/{id}/export` | 多套模板 + 导出 |
| 错题本 `errorbook` | `GET|POST /errors` · `GET|PUT|DELETE /errors/{id}` · `/errors/categories` · `/errors/review` | 错题录入与复习计划 |
| 笔记 `note` | `GET|POST /notes` · `GET|PUT|DELETE /notes/{id}` · `GET|POST /notes/tags` · `DELETE /notes/tags/{id}` | 笔记与标签 |
| 全局搜索 `search` | `GET /search?keyword=` | 跨任务 / 笔记 / 错题检索 |
| 统计 `stats` | `GET /stats/overview` · `/stats/activity` · `/stats/subject-distribution` · `/stats/heatmap` | 学习数据看板 |
| 设置 `settings` | `GET|PUT /settings` | 应用偏好 |
| 备份 `backup` | `POST /backup` · `GET /backup/list` · `POST /backup/restore|export|import` | 数据导入导出 |
| OCR `ocr` | `POST /ocr` | 图片文字识别（错题录入辅助） |
| 反馈 `feedback` | `GET /feedback/faq` · `POST /feedback` | 常见问题与建议 |
| 插件 `plugin` | `GET /plugins` · `GET /plugins/market` · `POST /plugins/{id}/toggle` | 插件管理 |
| 同步 `sync` | `POST /sync` · `GET /sync/devices` · `DELETE /sync/devices/{id}` · `GET /sync/status` · `PUT /sync/conflict` | 多端同步规划 |

## ⚙️ 常用配置

见 `src/main/resources/application.yml`，生产环境可覆盖关键项：

| 配置项 | 默认值 | 说明 |
|:---|:---|:---|
| `server.port` | `8080` | 服务端口（前端约定） |
| `server.servlet.context-path` | `/api/v1` | 接口前缀（前端约定） |
| `eta.jwt.secret` | 内置开发密钥 | **生产请用环境变量覆盖**，HS256 要求 ≥ 32 字节 |
| `eta.jwt.expiration-ms` | `86400000` | 令牌有效期（24h） |
| `spring.jpa.hibernate.ddl-auto` | `create` | 开发期自动建表；生产可改 `validate` |

> H2 为内存库，进程退出数据即清零，`ddl-auto: create` 保证每次全新启动时自动初始化演示数据。计划接入生产持久化时再引入文件型存储。

## 🤝 贡献

- 项目结构采用 **Package-by-feature**：新业务模块请自建 `cn.eta.team.eta.<module>` 包，内含 `Entity / Repository / Service / DTO / Controller`，保持内聚。
- 资源主键统一使用 **UUID 字符串**，与你对外契约保持一致。
- 新增接口后请同步更新根目录 [ETA-api.yapi](../ETA-api.yapi) 接口文档。
- 保持统一响应结构 `{ code, message, data }`。

## 🛤️ Roadmap

- [x] 单体本地后端骨架 & 认证链路
- [x] 任务 / 简历模块完整落地
- [ ] 错题 / 笔记 / 统计等模块从接口存根走向真实实现
- [ ] 本地数据文件型持久化（替代纯内存）
- [ ] 云端微服务后端（含 Python AI）——与本地两套解耦、独立演进
- [ ] 多设备数据同步与冲突合并

## 📄 许可证

[AGPL-3.0](LICENSE) © 2026 ETA 开发团队