# 校园综合生活服务平台 — 设计文档

> 版本 1.0 | 2026-05-07 | 分支 `zsh`

---

## 1. 概述

### 1.1 项目背景

校园综合生活服务平台是一个基于 Spring Cloud Alibaba 的微服务课程项目，覆盖校园生活中的二手交易、跑腿互助、社团活动、消息通知四大场景，并提供统一的用户认证体系。

### 1.2 设计目标

| 目标 | 说明 |
|---|---|
| 微服务化 | 按业务域拆分独立服务，独立部署、独立演进 |
| 数据隔离 | 每个服务独立数据库 Schema，避免跨服务直接访问数据 |
| 统一入口 | 所有外部请求通过 Gateway 统一鉴权和路由 |
| 前后端分离 | 后端纯 RESTful API，前端独立开发 |
| 可扩展 | 预留 Redis、RabbitMQ、Nacos Config 扩展点 |

### 1.3 术语表

| 术语 | 说明 |
|---|---|
| Gateway | Spring Cloud Gateway 网关，系统唯一入口 |
| Nacos | 服务注册与发现中心 |
| Feign | 声明式 HTTP 客户端，用于服务间调用 |
| UserContext | 基于 ThreadLocal 的当前用户上下文 |
| Schema | MySQL 数据库，每个服务独享一个 |
| JWT | JSON Web Token，无状态认证凭据 |

---

## 2. 总体架构

### 2.1 架构图

```
                          ┌─────────────────────┐
                          │   前端 (Mobile/Web)   │
                          └──────────┬──────────┘
                                     │ HTTP
                                     ▼
                          ┌─────────────────────┐
                          │  Gateway :9000       │
                          │  - 路由转发           │
                          │  - JWT 鉴权          │
                          │  - Header 注入       │
                          └──────────┬──────────┘
                                     │
                    ┌────────────────┼────────────────┐
                    ▼                ▼                 ▼
     ┌──────────────────┐  ┌──────────────┐  ┌──────────────────┐
     │ User-Auth :9010  │  │ Market :9020 │  │ Task :9030       │
     │ (用户认证)        │  │ (二手交易)    │  │ (跑腿任务)        │
     └────────┬─────────┘  └──────┬───────┘  └────────┬─────────┘
              │                   │                    │
              │         ┌─────────┼──────────┐         │
              │         │         │          │         │
              ▼         ▼         ▼          ▼         ▼
     ┌──────────────────────────────────────────────────────┐
     │                    Nacos :8848                        │
     │                  (服务注册发现)                         │
     └──────────────────────────────────────────────────────┘

     ┌──────────────────┐  ┌──────────────────┐
     │ Activity :9040   │  │ Notification:9050│
     │ (社团活动)        │  │ (消息通知)        │
     └──────────────────┘  └──────────────────┘
```

### 2.2 技术选型

| 层次 | 技术 | 版本 | 选型理由 |
|---|---|---|---|
| 开发语言 | JDK | 17 | LTS 版本，Spring Boot 3.x 最低要求 |
| 核心框架 | Spring Boot | 3.3.5 | 微服务基础框架，生态成熟 |
| 微服务框架 | Spring Cloud | 2023.0.3 | 完整的微服务解决方案 |
| 服务注册 | Nacos | 2023.0.1.2 | 阿里开源，兼容 Spring Cloud |
| API 网关 | Spring Cloud Gateway | 同 SC | 基于 WebFlux，性能优于 Zuul |
| 服务调用 | OpenFeign | 同 SC | 声明式 HTTP 客户端，集成 LoadBalancer |
| 负载均衡 | Spring Cloud LoadBalancer | 同 SC | 替代 Ribbon，官方推荐 |
| ORM | MyBatis-Plus | 3.5.9 | 简化 CRUD，自带分页插件 |
| 数据库 | MySQL | 8.4.0 驱动 | 主流关系型数据库 |
| 认证 | jjwt | 0.12.6 | 轻量 JWT 库，无 Spring Security 依赖 |
| API 文档 | Knife4j | 4.5.0 | 国产 Swagger 增强，支持 OpenAPI 3.0 |
| 工具 | Lombok | 1.18.30 | 简化 POJO 代码 |
| 工具 | Hutool | 5.8.34 | 通用工具类库 |

### 2.3 服务端口分配

| 服务 | 模块名 | 端口 | Schema |
|---|---|---|---|
| API 网关 | `campus-gateway` | 9000 | — |
| 用户认证 | `campus-user-auth-service` | 9010 | `campus_user` |
| 二手交易 | `campus-market-service` | 9020 | `campus_market` |
| 跑腿任务 | `campus-task-service` | 9030 | `campus_task` |
| 社团活动 | `campus-activity-service` | 9040 | `campus_activity` |
| 消息通知 | `campus-notification-service` | 9050 | `campus_notification` |

---

## 3. 模块设计

### 3.1 campus-common（公共模块）

不作为服务启动，以 JAR 形式被所有业务服务依赖。

```
campus-common/src/main/java/com/god/common/
├── client/
│   └── UserFeignClient.java        # Feign 客户端，调用 user-auth 服务
├── config/
│   ├── CommonWebMvcConfig.java     # 注册 UserInfoInterceptor
│   ├── FeignConfig.java            # Feign 配置 + 请求拦截器
│   └── FeignRequestInterceptor.java # 跨服务透传 X-User-* Header
├── constant/
│   ├── AuthConstant.java           # Header 名称常量
│   ├── MqConstant.java             # RabbitMQ 路由键常量（预留）
│   └── RedisKeyConstant.java       # Redis Key 前缀（预留）
├── context/
│   └── UserContext.java            # ThreadLocal 用户上下文
├── dto/
│   ├── LoginUserDTO.java           # 网关解析后的用户信息
│   ├── UserSimpleDTO.java          # 用户简要信息（Feign 返回）
│   ├── NotificationMessageDTO.java # 通知消息体（预留 MQ）
│   └── PageQueryDTO.java           # 分页请求基类
├── enums/
│   ├── UserRoleEnum.java           # STUDENT / CLUB_ADMIN / ADMIN
│   ├── UserStatusEnum.java         # NORMAL / DISABLED
│   ├── MarketItemStatusEnum.java   # ON_SALE / LOCKED / SOLD / OFF_SHELF
│   ├── TaskStatusEnum.java         # PUBLISHED / ACCEPTED / COMPLETED / CANCELLED
│   ├── ActivityStatusEnum.java     # DRAFT / PUBLISHED / REGISTRATION_CLOSED / FINISHED / CANCELLED
│   └── NotificationTypeEnum.java   # SYSTEM / MARKET / TASK / ACTIVITY
├── exception/
│   ├── BusinessException.java      # 业务异常（code + message）
│   ├── GlobalExceptionHandler.java # 全局异常处理器
│   └── AssertUtil.java             # 断言工具（条件不满足抛异常）
├── interceptor/
│   └── UserInfoInterceptor.java    # 从 Header 读取用户信息写入 UserContext
├── result/
│   ├── Result.java                 # 统一响应 {code, message, data}
│   ├── PageResult.java             # 分页响应 {total, pageNum, pageSize, records}
│   └── ResultCode.java             # 错误码枚举
└── utils/
    ├── JwtUtil.java                # JWT 生成与解析（HMAC-SHA）
    ├── TokenUtil.java              # Authorization Header 解析
    └── DateTimeUtil.java           # 日期格式化工具
```

**关键设计决策：UserContext**

采用 ThreadLocal 而非 Spring Security，原因是课程项目体量小，引入 Security 会显著增加配置复杂度。每个请求在 Gateway 鉴权后，用户信息通过 `X-User-Id`、`X-Username`、`X-User-Role` 三个 Header 向下传递，业务服务通过 `UserInfoInterceptor` 自动提取并写入 `UserContext`。

### 3.2 campus-gateway（API 网关）

**职责：**
- 统一入口，对外暴露 `localhost:9000/api/**`
- JWT 鉴权（白名单除外）
- 将用户信息注入 Header 后转发

**路由规则：**

| 路径前缀 | 目标服务 | StripPrefix |
|---|---|---|
| `/api/auth/**`, `/api/users/**` | `lb://campus-user-auth-service` | 1 |
| `/api/market/**` | `lb://campus-market-service` | 1 |
| `/api/tasks/**` | `lb://campus-task-service` | 1 |
| `/api/activities/**`, `/api/clubs/**` | `lb://campus-activity-service` | 1 |
| `/api/notifications/**` | `lb://campus-notification-service` | 1 |

**白名单（无需 Token）：**
- `/api/auth/login`
- `/api/auth/register`
- `/api/auth/captcha`（预留）
- `/actuator/**`

**鉴权流程：**

```
请求 → JwtAuthenticationFilter (Order = -100)
     → 路径匹配白名单？
         ├── 是 → 直接放行
         └── 否 → 提取 Authorization Header
                  → 解析 JWT（HMAC-SHA 验签）
                      ├── 失败 → 返回 401
                      └── 成功 → 注入 X-User-Id / X-Username / X-User-Role
                               → 放行到下游
```

### 3.3 campus-user-auth-service（用户认证服务）

**职责：** 用户注册、登录登出、个人信息管理、为其他服务提供用户查询 Feign 接口。

**包结构：**

```
com.god.userauth
├── controller/
│   ├── AuthController.java    # /auth — 注册、登录、登出
│   └── UserController.java    # /users — 用户信息 CRUD + Feign 接口
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   └── impl/
│       ├── AuthServiceImpl.java
│       └── UserServiceImpl.java
├── mapper/
│   └── UserMapper.java        # MyBatis-Plus BaseMapper<sys_user>
├── entity/
│   └── User.java
├── dto/
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── UpdateUserRequest.java
└── vo/
    ├── LoginResponse.java
    └── UserProfileVO.java
```

**登录逻辑：**
1. 接收 `account` 字段（支持学号、用户名、手机、邮箱四种方式）
2. 查询 `sys_user` 表，匹配 `student_no OR username OR phone OR email`
3. BCrypt 密码比对
4. 生成 JWT，claims 包含 `userId`、`username`、`role`
5. 返回 token + 用户信息

**核心接口：**
- `POST /auth/register` — 注册（学号唯一性校验）
- `POST /auth/login` — 登录（多字段匹配）
- `POST /auth/logout` — 登出（当前为客户端删除 token，服务端无状态）
- `GET /users/me` — 当前用户信息
- `PUT /users/me` — 更新个人信息
- `GET /users/{id}` — 用户公开信息
- `GET /users/simple/{id}` — 用户简要信息（Feign 专用）
- `POST /users/simple/batch` — 批量查询用户（Feign 专用）

### 3.4 campus-market-service（二手交易服务）

**职责：** 商品发布、列表查询、详情浏览、状态管理。

**实体：** `market_item`（见第 4 节数据模型）

**服务间依赖：** 调用 `UserFeignClient.batchGetUserSimple()` 填充 `sellerName`（已批量化，避免 N+1）

**状态机：**

```
     ┌─────────┐
     │ ON_SALE │ ← 发布
     └────┬────┘
          │ 下架 → OFF_SHELF（终态）
          │ 标记已售 → SOLD（终态）
```

**核心接口：**
- `POST /market/items` — 发布商品
- `GET /market/items` — 分页列表（支持 keyword / category / status 筛选）
- `GET /market/items/{id}` — 详情（自动增加浏览量）
- `PUT /market/items/{id}` — 编辑（仅卖家，SOLD 状态不可编辑）
- `PUT /market/items/{id}/off-shelf` — 下架（仅卖家）
- `PUT /market/items/{id}/sold` — 标记已售（仅卖家）
- `GET /market/items/my` — 我的商品

### 3.5 campus-task-service（跑腿任务服务）

**职责：** 任务发布、任务大厅、接单、完成、取消。

**实体：** `help_task`（见第 4 节数据模型）

**服务间依赖：** 调用 `UserFeignClient.batchGetUserSimple()` 填充 `publisherName` 和 `accepterName`

**状态机：**

```
     ┌───────────┐
     │ PUBLISHED │ ← 发布
     └─────┬─────┘
           │ 接单 → ACCEPTED
           │ 取消 → CANCELLED（终态）
     ┌─────▼─────┐
     │ ACCEPTED  │
     └─────┬─────┘
           │ 完成 → COMPLETED（终态）
```

**核心接口：**
- `POST /tasks` — 发布任务
- `GET /tasks` — 分页列表（支持 keyword / taskType / status 筛选）
- `GET /tasks/{id}` — 详情
- `PUT /tasks/{id}/accept` — 接单（不能接自己的任务，只能接 PUBLISHED 状态）
- `PUT /tasks/{id}/complete` — 完成（仅接单者，只能完成 ACCEPTED 状态）
- `PUT /tasks/{id}/cancel` — 取消（仅发布者，只能取消 PUBLISHED 状态）
- `GET /tasks/my-published` — 我发布的
- `GET /tasks/my-accepted` — 我接取的

### 3.6 campus-activity-service（社团活动服务）

**职责：** 社团管理、活动管理、报名管理。

**实体：** `club`、`activity`、`activity_registration`（见第 4 节数据模型）

**服务间依赖：** 调用 `UserFeignClient.batchGetUserSimple()` 填充 `adminName` 和 `publisherName`

**活动状态机：**

```
     ┌───────────┐
     │ PUBLISHED │ ← 发布
     └─────┬─────┘
           │ 取消 → CANCELLED（终态）
           │ 报名截止 → REGISTRATION_CLOSED
           │ 结束 → FINISHED
```

**核心接口——社团：**
- `POST /clubs` — 创建社团（创建者自动成为管理员）
- `GET /clubs` — 全部社团列表（不分页）
- `GET /clubs/{id}` — 社团详情
- `PUT /clubs/{id}` — 修改（仅管理员）
- `GET /clubs/my` — 我管理的社团

**核心接口——活动：**
- `POST /activities` — 发布活动（可选关联社团）
- `GET /activities` — 分页列表（支持 keyword / clubId / status 筛选）
- `GET /activities/{id}` — 详情（带 token 返回 `isRegistered`）
- `POST /activities/{id}/register` — 报名（校验：状态 / 截止时间 / 满员 / 重复报名）
- `DELETE /activities/{id}/register` — 取消报名
- `PUT /activities/{id}/cancel` — 取消活动（仅发布者）
- `GET /activities/my-registered` — 我报名的
- `GET /activities/my-published` — 我发布的

### 3.7 campus-notification-service（消息通知服务）

**职责：** 通知创建、查询、标记已读、删除。

**实体：** `notification`（见第 4 节数据模型）

**当前实现：** 同步通知，由业务方直接调用 `POST /notifications` 创建。后续可接入 RabbitMQ 异步化。

**核心接口：**
- `POST /notifications` — 创建通知
- `GET /notifications` — 我的通知（分页，支持 type / readStatus 筛选）
- `GET /notifications/unread-count` — 未读数量
- `PUT /notifications/{id}/read` — 标记单条已读
- `PUT /notifications/read-all` — 全部已读
- `DELETE /notifications/{id}` — 删除通知

---

## 4. 数据库设计

### 4.1 Schema 划分

```
MySQL :3306
├── campus_user           # 用户认证服务
│   └── sys_user
├── campus_market         # 二手交易服务
│   └── market_item
├── campus_task           # 跑腿任务服务
│   └── help_task
├── campus_activity       # 社团活动服务
│   ├── club
│   ├── activity
│   └── activity_registration
└── campus_notification   # 消息通知服务
    └── notification
```

**设计原则：**
- 每个微服务独占一个 Schema，不跨 Schema 做 JOIN
- 跨服务数据关联通过服务 ID（Long） + Feign API 实现
- 表名全小写+下划线，字段名全小写+下划线，与 MyBatis-Plus 下划线转驼峰配合

### 4.2 表结构

#### sys_user（用户表）— campus_user

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | ✅ | 用户 ID |
| student_no | VARCHAR(32) UNIQUE | ✅ | 学号 |
| username | VARCHAR(50) | ✅ | 用户名 |
| password | VARCHAR(100) | ✅ | BCrypt 加密 |
| phone | VARCHAR(20) | | 手机号 |
| email | VARCHAR(100) | | 邮箱 |
| avatar | VARCHAR(255) | | 头像 URL |
| college | VARCHAR(100) | | 学院 |
| major | VARCHAR(100) | | 专业 |
| role | VARCHAR(32) | ✅ | 角色，默认 `STUDENT` |
| status | TINYINT | ✅ | 1=正常，0=禁用 |
| create_time | DATETIME | ✅ | 创建时间 |
| update_time | DATETIME | ✅ | 更新时间（自动更新） |

#### market_item（商品表）— campus_market

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | ✅ | 商品 ID |
| seller_id | BIGINT | ✅ | 卖家 ID（关联 sys_user.id） |
| title | VARCHAR(100) | ✅ | 标题 |
| description | TEXT | | 描述 |
| price | DECIMAL(10,2) | ✅ | 价格 |
| category | VARCHAR(50) | | 分类 |
| cover_image | VARCHAR(255) | | 封面图 URL |
| images | TEXT | | 多图，逗号分隔 URL |
| contact_info | VARCHAR(100) | | 联系方式 |
| status | VARCHAR(32) | ✅ | 状态，默认 `ON_SALE` |
| view_count | INT | ✅ | 浏览量，默认 0 |
| create_time | DATETIME | ✅ | |
| update_time | DATETIME | ✅ | |

#### help_task（任务表）— campus_task

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | ✅ | 任务 ID |
| publisher_id | BIGINT | ✅ | 发布者 ID |
| accepter_id | BIGINT | | 接单者 ID |
| title | VARCHAR(100) | ✅ | 标题 |
| description | TEXT | | 描述 |
| task_type | VARCHAR(50) | ✅ | 任务类型 |
| reward | DECIMAL(10,2) | ✅ | 悬赏金额 |
| pickup_address | VARCHAR(255) | | 取货地址 |
| delivery_address | VARCHAR(255) | | 送达地址 |
| deadline | DATETIME | | 截止时间 |
| contact_info | VARCHAR(100) | | 联系方式 |
| status | VARCHAR(32) | ✅ | 状态，默认 `PUBLISHED` |
| create_time | DATETIME | ✅ | |
| update_time | DATETIME | ✅ | |

#### club（社团表）— campus_activity

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | ✅ | 社团 ID |
| name | VARCHAR(100) | ✅ | 社团名称 |
| description | TEXT | | 介绍 |
| logo | VARCHAR(255) | | Logo URL |
| admin_id | BIGINT | ✅ | 社团管理员 ID |
| contact_info | VARCHAR(100) | | 联系方式 |
| create_time | DATETIME | ✅ | |
| update_time | DATETIME | ✅ | |

#### activity（活动表）— campus_activity

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | ✅ | 活动 ID |
| club_id | BIGINT | | 所属社团 ID（可为空，个人活动） |
| publisher_id | BIGINT | ✅ | 发布者 ID |
| title | VARCHAR(100) | ✅ | 活动标题 |
| description | TEXT | | 描述 |
| location | VARCHAR(255) | | 地点 |
| cover_image | VARCHAR(255) | | 封面图 URL |
| start_time | DATETIME | ✅ | 开始时间 |
| end_time | DATETIME | ✅ | 结束时间 |
| signup_deadline | DATETIME | | 报名截止时间 |
| max_participants | INT | | 最大报名人数 |
| current_participants | INT | ✅ | 当前报名人数，默认 0 |
| status | VARCHAR(32) | ✅ | 状态，默认 `PUBLISHED` |
| create_time | DATETIME | ✅ | |
| update_time | DATETIME | ✅ | |

#### activity_registration（报名表）— campus_activity

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | ✅ | |
| activity_id | BIGINT | ✅ | 活动 ID |
| user_id | BIGINT | ✅ | 用户 ID |
| status | VARCHAR(32) | ✅ | 默认 `REGISTERED` |
| create_time | DATETIME | ✅ | |
| update_time | DATETIME | ✅ | |

- 联合唯一索引：`(activity_id, user_id)` — 防重复报名

#### notification（通知表）— campus_notification

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | ✅ | |
| receiver_id | BIGINT | ✅ | 接收人 ID |
| title | VARCHAR(100) | ✅ | 标题 |
| content | VARCHAR(500) | ✅ | 内容 |
| type | VARCHAR(32) | ✅ | 类型：SYSTEM/MARKET/TASK/ACTIVITY |
| business_id | BIGINT | | 关联业务 ID |
| business_type | VARCHAR(32) | | 关联业务类型 |
| read_status | TINYINT | ✅ | 0=未读，1=已读 |
| create_time | DATETIME | ✅ | |
| update_time | DATETIME | ✅ | |

### 4.3 跨服务引用关系

```
market_item.seller_id     ──→ sys_user.id  (Feign)
help_task.publisher_id    ──→ sys_user.id  (Feign)
help_task.accepter_id     ──→ sys_user.id  (Feign)
club.admin_id             ──→ sys_user.id  (Feign)
activity.publisher_id     ──→ sys_user.id  (Feign)
activity.club_id          ──→ club.id      (本地)
activity_registration.activity_id → activity.id (本地)
notification.receiver_id  ──→ sys_user.id  (Feign，预留)
```

所有跨 Schema 的引用均通过 Feign API 在应用层关联，数据库层面不做 JOIN。

---

## 5. 接口设计

### 5.1 设计规范

#### 统一响应结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

#### 分页响应结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "records": []
  }
}
```

#### 错误码体系

| code | 含义 | 触发场景 |
|---|---|---|
| 200 | 成功 | — |
| 400 | 参数错误 | `@Valid` 校验失败 |
| 401 | 未登录 | Token 缺失/无效/过期 |
| 403 | 无权限 | 操作他人数据 |
| 404 | 资源不存在 | ID 查不到数据 |
| 409 | 资源冲突 | 重复注册/报名 |
| 500 | 系统异常 | 未捕获异常 |
| 1001 | 用户名或密码错误 | 登录失败 |
| 1002 | 用户已被禁用 | 登录被拒 |
| 1003 | 用户不存在 | 查询不存在的用户 |
| 1004 | Token 无效 | JWT 解析失败 |
| 2001 | 商品不存在 | |
| 2002 | 商品状态不允许该操作 | 编辑已售商品 |
| 3001 | 任务不存在 | |
| 3002 | 任务状态不允许该操作 | 重复接单 |
| 4001 | 活动不存在 | |
| 4002 | 活动报名已截止 | |
| 4003 | 活动报名人数已满 | |
| 5001 | 通知不存在 | |

#### 分页参数

所有 GET 列表接口统一使用 Query 参数：
- `pageNum`（Long，默认 1）
- `pageSize`（Long，默认 10，上限 100）

#### 认证方式

需认证接口在 Header 中携带：`Authorization: Bearer <token>`

### 5.2 完整接口清单

完整接口文档（含字段定义、校验规则、请求/响应示例）见 [`product-prototype.md`](product-prototype.md)。此处仅列路由速查表。

#### 用户认证（campus-user-auth-service）

| 方法 | 网关路径 | 认证 | 说明 |
|---|---|---|---|
| POST | `/api/auth/register` | ❌ | 注册 |
| POST | `/api/auth/login` | ❌ | 登录 |
| POST | `/api/auth/logout` | ✅ | 登出 |
| GET | `/api/users/me` | ✅ | 当前用户信息 |
| PUT | `/api/users/me` | ✅ | 更新个人信息 |
| GET | `/api/users/{id}` | ❌ | 用户公开信息 |
| GET | `/api/users/simple/{id}` | ✅ | 用户简要信息（Feign） |
| POST | `/api/users/simple/batch` | ✅ | 批量查询（Feign） |

#### 二手交易（campus-market-service）

| 方法 | 网关路径 | 认证 | 说明 |
|---|---|---|---|
| POST | `/api/market/items` | ✅ | 发布商品 |
| GET | `/api/market/items` | ❌ | 商品列表 |
| GET | `/api/market/items/{id}` | ❌ | 商品详情（增加浏览量） |
| PUT | `/api/market/items/{id}` | ✅ | 编辑商品 |
| PUT | `/api/market/items/{id}/off-shelf` | ✅ | 下架 |
| PUT | `/api/market/items/{id}/sold` | ✅ | 标记已售 |
| GET | `/api/market/items/my` | ✅ | 我的商品 |

#### 跑腿任务（campus-task-service）

| 方法 | 网关路径 | 认证 | 说明 |
|---|---|---|---|
| POST | `/api/tasks` | ✅ | 发布任务 |
| GET | `/api/tasks` | ❌ | 任务列表 |
| GET | `/api/tasks/{id}` | ❌ | 任务详情 |
| PUT | `/api/tasks/{id}/accept` | ✅ | 接单 |
| PUT | `/api/tasks/{id}/complete` | ✅ | 完成 |
| PUT | `/api/tasks/{id}/cancel` | ✅ | 取消 |
| GET | `/api/tasks/my-published` | ✅ | 我发布的 |
| GET | `/api/tasks/my-accepted` | ✅ | 我接取的 |

#### 社团活动（campus-activity-service）

| 方法 | 网关路径 | 认证 | 说明 |
|---|---|---|---|
| POST | `/api/clubs` | ✅ | 创建社团 |
| GET | `/api/clubs` | ❌ | 社团列表 |
| GET | `/api/clubs/{id}` | ❌ | 社团详情 |
| PUT | `/api/clubs/{id}` | ✅ | 修改社团 |
| GET | `/api/clubs/my` | ✅ | 我管理的社团 |
| POST | `/api/activities` | ✅ | 发布活动 |
| GET | `/api/activities` | ❌ | 活动列表 |
| GET | `/api/activities/{id}` | 可选 | 活动详情 |
| POST | `/api/activities/{id}/register` | ✅ | 报名 |
| DELETE | `/api/activities/{id}/register` | ✅ | 取消报名 |
| PUT | `/api/activities/{id}/cancel` | ✅ | 取消活动 |
| GET | `/api/activities/my-registered` | ✅ | 我报名的 |
| GET | `/api/activities/my-published` | ✅ | 我发布的 |

#### 消息通知（campus-notification-service）

| 方法 | 网关路径 | 认证 | 说明 |
|---|---|---|---|
| POST | `/api/notifications` | ✅ | 创建通知 |
| GET | `/api/notifications` | ✅ | 我的通知 |
| GET | `/api/notifications/unread-count` | ✅ | 未读数量 |
| PUT | `/api/notifications/{id}/read` | ✅ | 标记已读 |
| PUT | `/api/notifications/read-all` | ✅ | 全部已读 |
| DELETE | `/api/notifications/{id}` | ✅ | 删除 |

---

## 6. 安全设计

### 6.1 认证流程

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  客户端    │     │ Gateway  │     │ UserAuth │     │ 业务服务  │
└────┬─────┘     └────┬─────┘     └────┬─────┘     └────┬─────┘
     │                │               │                  │
     │ POST /login    │               │                  │
     │───────────────→│               │                  │
     │                │──────────────→│                  │
     │                │←──── Token ───│                  │
     │←─── Token ────│               │                  │
     │                │               │                  │
     │ GET /market    │               │                  │
     │ + Bearer Token │               │                  │
     │───────────────→│               │                  │
     │                │ JWT 验签       │                  │
     │                │ 解析 userId    │                  │
     │                │ username role  │                  │
     │                │──────────────────────────────→  │
     │                │    Header: X-User-Id            │
     │                │           X-Username            │
     │                │           X-User-Role           │
     │                │               │                  │
     │                │               │   UserInfoInterceptor
     │                │               │   → UserContext.setUser()
     │                │               │                  │
     │                │←────────────────────────────── │
     │←── Response ──│               │                  │
```

### 6.2 JWT 设计

- **算法：** HMAC-SHA（jjwt 默认）
- **Claims：** `userId`、`username`、`role`、`iat`、`exp`
- **有效期：** 86400 秒（24 小时）
- **密钥：** 配置在 `campus.security.jwt-secret`，local 和 dev 环境各自管理，不提交到 Git
- **无状态：** 当前版本不设 Token 黑名单（登出由客户端删除 token）。后续可引入 Redis 实现黑名单

### 6.3 权限模型

当前为简化 RBAC，仅区分角色：

| 角色 | 权限范围 |
|---|---|
| `STUDENT` | 发布/购买商品、发布/接取任务、报名活动、管理个人信息 |
| `CLUB_ADMIN` | STUDENT 权限 + 创建社团、以社团身份发布活动 |
| `ADMIN` | 预留 |

权限校验在业务层通过 `UserContext.getUserId()` 与资源所有者 ID 比对实现。

### 6.4 配置安全

- 所有密钥（jwt-secret、db password）不在 `application.yml` 中硬编码
- `application.yml`（框架级配置）→ 提交到 Git
- `application-local.yml` / `application-dev.yml`（敏感配置）→ `.gitignore`
- `.example` 模板文件（占位符）→ 可安全提交

---

## 7. 关键设计决策

### 7.1 为什么不用 Spring Security

**决策：** 采用 Gateway Filter + ThreadLocal Interceptor 的自定义认证方案。

**理由：**
- 课程项目规模小，用户角色简单（3 种），不需要 Security 的 ACL/注解授权
- Spring Security 配置样板代码量大，集成 Spring Cloud Gateway 需要额外适配（WebFlux vs Servlet）
- 自定义方案代码量少，调试直观，适合教学场景
- 后续如需接入 Security，可在 Gateway 层替换 Filter，不动业务服务

### 7.2 为什么 Feign N+1 要改成批量

**决策：** 列表接口的 Feign 调用从逐条改为批量。

**原始问题：** 分页返回 10 条商品 → 每条调一次 `getUserSimple(id)` → 10 次 HTTP 调用。

**修复方式：** 先收集当前页所有 userId → Set 去重 → 一次 `batchGetUserSimple(ids)` → 本地 Map 取值。

**细节接口保留逐条：** 详情接口只查一个用户，批量反而浪费。

### 7.3 为什么用独立 Schema 而非独立数据库实例

**决策：** 同一 MySQL 实例下每人独立 Schema（`campus_user` / `campus_market` / ...）。

**理由：**
- 课程项目只需一个 MySQL 实例，降低运维成本
- Schema 隔离满足"微服务数据私有"的底线要求
- 将来升级到独立实例只需改 datasource URL
- 跨服务引用全部走 Feign API，不依赖跨库 JOIN

### 7.4 为什么状态字段用 VARCHAR 而非 INT

**决策：** 所有枚举字段（status、role、type）在数据库存 VARCHAR。

**理由：**
- 数据库直接可读，调试方便
- MyBatis-Plus 不做数值转换，减少出错
- 课程项目数据量小，存储开销可忽略

### 7.5 为什么通知服务是同步调用

**决策：** 当前版本通知通过直接 HTTP 调用创建，不经过消息队列。

**理由：**
- 降低初版架构复杂度
- 业务触发点少（报名、接单等），同步调用延迟可接受
- `campus-common` 已预留 `MqConstant` 和 `NotificationMessageDTO`，后续切换只需改调用方式

---

## 8. 配置管理

### 8.1 Profile 机制

```
application.yml            → 框架级公共配置（提交 Git）
application-local.yml      → 本地开发环境（不提交）
application-dev.yml        → 部署环境，环境变量注入（不提交）
application-*.yml.example  → 模板文件（提交 Git）
```

### 8.2 各 Profile 职责

| 配置项 | application.yml | local | dev |
|---|---|---|---|
| server.port | ✅ | — | — |
| spring.application.name | ✅ | — | — |
| spring.profiles.active | ✅ (local) | — | — |
| mybatis-plus | ✅ | — | — |
| knife4j / springdoc | ✅ | — | — |
| logging | ✅ | — | — |
| gateway routes | ✅ | — | — |
| gateway white-list | ✅ | — | — |
| JWT expire | ✅ | — | — |
| nacos server-addr | — | ✅ | ✅ |
| datasource url/driver/username | — | ✅ | ✅ |
| datasource password | — | ✅（明文） | ✅（`${DB_PASSWORD}`） |
| jwt-secret | — | ✅（明文） | ✅（`${JWT_SECRET}`） |

---

## 9. 部署架构

### 9.1 启动顺序

```
1. MySQL :3306
2. Nacos :8848
3. campus-user-auth-service :9010  ← 其他服务 Feign 依赖
4. campus-market-service :9020
   campus-task-service :9030
   campus-activity-service :9040
   campus-notification-service :9050
   （以上 4 个可并行启动）
5. campus-gateway :9000  ← 最后启动
```

### 9.2 外部依赖

| 组件 | 地址 | 说明 |
|---|---|---|
| MySQL | `localhost:3306` | 5 个独立 Schema |
| Nacos | `localhost:8848` | 服务注册与发现 |

### 9.3 本地开发环境搭建

```bash
# 1. 初始化数据库
for f in sql/schema-*.sql; do
  mysql -u root -p < "$f"
done

# 2. 配置密码（首次）
for module in campus-user-auth-service campus-market-service campus-task-service \
              campus-activity-service campus-notification-service campus-gateway; do
  cp "$module/src/main/resources/application-local.yml.example" \
     "$module/src/main/resources/application-local.yml"
done
# 编辑各 application-local.yml，填入真实密码

# 3. 编译
mvn clean compile -DskipTests

# 4. 按启动顺序启动各服务（IDE 直接运行 *Application.java）
```

---

## 10. 前端设计概要

> 完整前端接口文档见 [`product-prototype.md`](product-prototype.md)。本节仅列架构层面的关键信息。

### 10.1 技术建议

| 方面 | 建议 |
|---|---|
| 框架 | React / Vue / 小程序（Taro/uni-app） |
| 状态管理 | 全局：用户 Token + 用户信息 + 未读通知数；页面级：各列表数据 |
| HTTP 库 | axios（Web）/ 封装 wx.request（小程序） |
| UI 组件 | 移动端 H5 可用 Vant；小程序用各平台原生组件 |
| 路由守卫 | 未登录跳转登录页，Token 过期（401）自动跳转 |

### 10.2 页面结构

```
├── 登录/注册
├── 首页（Tab 导航）
│   ├── 二手交易（列表 / 详情 / 发布 / 我的）
│   ├── 跑腿任务（列表 / 详情 / 发布 / 我的发布 / 我的接取）
│   ├── 社团活动（社团列表 / 活动列表 / 详情 / 创建 / 发布 / 我的）
│   └── 消息（通知列表 / 未读角标）
└── 个人中心（资料 / 修改 / 我管理的社团 / 退出）
```

### 10.3 前端要点

1. **Token 管理：** 登录后存入 `localStorage`，每次请求在 Header 中带 `Authorization: Bearer <token>`
2. **401 处理：** 拦截器统一处理，跳转登录页
3. **用户名展示：** 后端已通过 Feign 填充，前端直接展示 `sellerName` / `publisherName` 等字段
4. **未读角标：** 轮询或 WebSocket 获取 `/api/notifications/unread-count`
5. **图片字段：** `coverImage` / `images` 当前接受 URL 字符串，前端需自行对接图片上传服务（后续可扩展文件上传接口）

---

## 11. 待优化项

参见 `memory/architecture-issues.md`（持久化任务清单），简要列出：

| 优先级 | 事项 | 说明 |
|---|---|---|
| P1 | 引入断路器 | Sentinel 或 Resilience4j，防止级联故障 |
| P3 | 链路追踪 | Micrometer Tracing + Zipkin |
| P4 | 通知权限控制 | 限制通知创建者的 receiverId 范围 |
| P5 | 清理 MQ 死代码 | 移除或落地 MqConstant / NotificationMessageDTO |
| P6 | 清理 Redis 死代码 / 落地 | 用于验证码或 Token 黑名单 |

---

## 附录 A：项目文件清单

| 文件/目录 | 说明 |
|---|---|
| `pom.xml` | 父 POM，定义模块和依赖版本 |
| `campus-common/` | 公共模块（JAR） |
| `campus-gateway/` | 网关服务 |
| `campus-user-auth-service/` | 用户认证服务 |
| `campus-market-service/` | 二手交易服务 |
| `campus-task-service/` | 跑腿任务服务 |
| `campus-activity-service/` | 社团活动服务 |
| `campus-notification-service/` | 消息通知服务 |
| `sql/schema-*.sql` | 各服务建表脚本 |
| `docs/design-doc.md` | 本文档 |
| `docs/product-prototype.md` | 前端接口文档 |
| `README.md` | 项目说明 |
