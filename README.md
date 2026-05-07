# 校园综合生活服务平台

## 1. 项目简介

校园综合生活服务平台是一个基于 Spring Cloud Alibaba 的微服务课程项目，面向校园生活场景，提供用户认证、二手交易、跑腿互助、活动社团、消息通知等功能。

项目采用每个服务独立 Schema 的方式隔离数据，各业务模块以独立微服务运行，通过网关统一入口访问，通过 Nacos 实现服务注册发现，通过 OpenFeign 完成必要的服务间调用。

## 2. 技术栈

### 后端技术

| 技术 | 版本/说明 |
|---|---|
| JDK | 17 |
| Spring Boot | 3.3.5 |
| Spring Cloud | 2023.0.3 |
| Spring Cloud Alibaba | 2023.0.1.2 |
| Nacos | 2.3.2 服务注册与发现 |
| Spring Cloud Gateway | API 网关 |
| OpenFeign | 服务间调用 |
| Spring Cloud LoadBalancer | 负载均衡 |
| MyBatis-Plus | 3.5.9，数据访问与分页 |
| MySQL | 8.4.0 驱动 |
| Knife4j / Springdoc OpenAPI | 接口文档与调试 |
| Lombok | 简化实体类和 DTO 代码 |
| Hutool | 常用工具类 |
| JWT | 用户登录认证 |

后续可扩展：

- Redis：Token 黑名单、验证码、热点数据缓存。
- RabbitMQ：任务接单、活动报名、商品状态变更后的异步通知。
- Nacos Config：统一配置中心。

## 3. 项目结构

```text
campus-life-service-platform
├── campus-common
├── campus-gateway
├── campus-user-auth-service
├── campus-market-service
├── campus-task-service
├── campus-activity-service
├── campus-notification-service
├── campus-file-service
└── pom.xml
```

### 3.1 campus-common

公共基础模块，不作为独立服务启动。

主要内容：

- 统一响应结果：`Result`、`ResultCode`
- 分页结果：`PageResult`
- 全局异常处理：`GlobalExceptionHandler`
- 业务异常：`BusinessException`
- JWT 工具：`JwtUtil`
- Token 工具：`TokenUtil`
- 用户上下文：`UserContext`
- 用户信息拦截器：从网关 Header 中读取用户信息
- OpenFeign 公共配置：请求 Header 透传
- OpenFeign 用户客户端：`UserFeignClient`
- 公共 DTO：`LoginUserDTO`、`UserSimpleDTO`、`NotificationMessageDTO`
- 公共枚举：用户状态、用户角色、商品状态、任务状态、活动状态、通知类型
- 公共常量：鉴权 Header、Redis Key、MQ Key 等

### 3.2 campus-gateway

网关服务，端口：`9000`。

职责：

- 统一系统入口
- 路由转发到各业务服务
- JWT 登录校验
- 白名单接口放行
- 将登录用户信息写入 Header 后转发给下游服务

外部请求统一通过网关访问，例如：

```text
http://localhost:9000/api/auth/login
http://localhost:9000/api/market/items
http://localhost:9000/api/tasks
```

### 3.3 campus-user-auth-service

用户中心与认证服务，端口：`9010`。

职责：

- 用户注册
- 用户登录
- JWT Token 生成
- 查询当前用户信息
- 修改当前用户信息
- 查询用户简要信息
- 为其他服务提供用户信息 Feign 接口

### 3.4 campus-market-service

校园二手交易服务，端口：`9020`。

职责：

- 发布二手商品
- 查询商品列表
- 查询商品详情
- 编辑商品
- 下架商品
- 标记已售出
- 查询我的发布
- 通过 OpenFeign 查询商品发布者用户名

### 3.5 campus-task-service

校园跑腿/互助任务服务，端口：`9030`。

职责：

- 发布跑腿任务
- 查询任务大厅
- 查看任务详情
- 接单
- 完成任务
- 取消任务
- 查询我发布的任务
- 查询我接单的任务
- 通过 OpenFeign 查询发布者、接单者用户名

### 3.6 campus-activity-service

校园活动与社团服务，端口：`9040`。

职责：

- 创建社团
- 查询社团列表
- 查询社团详情
- 修改社团信息
- 查询我管理的社团
- 发布活动
- 查询活动列表
- 查询活动详情
- 活动报名
- 取消报名
- 取消活动
- 查询我的报名
- 查询我发布的活动
- 通过 OpenFeign 查询社团管理员、活动发布者用户名

### 3.7 campus-notification-service

消息中心与系统通知服务，端口：`9050`。

职责：

- 创建通知
- 查询我的通知
- 查询未读通知数量
- 标记单条通知已读
- 标记全部通知已读
- 删除通知

当前版本未引入消息队列，通知创建接口可由前端或其他服务同步调用。后续可扩展为 RabbitMQ 异步通知。

### 3.8 campus-file-service

文件上传服务，端口：`9060`。

职责：

- 接收前端上传的图片文件
- 上传至阿里云 OSS
- 返回可访问的 OSS URL
- 支持单张和批量上传

该服务无数据库依赖，文件按日期分目录存储在 OSS，访问 URL 由前端获取后再填入其他业务接口的 image 字段。

## 4. 数据库说明

项目采用**每个服务独立 Schema** 的方式，通过数据库隔离保证微服务数据私有。

| 服务 | Schema | 建表 SQL |
|---|---|---|
| campus-user-auth-service | `campus_user` | `sql/schema-user.sql` |
| campus-market-service | `campus_market` | `sql/schema-market.sql` |
| campus-task-service | `campus_task` | `sql/schema-task.sql` |
| campus-activity-service | `campus_activity` | `sql/schema-activity.sql` |
| campus-notification-service | `campus_notification` | `sql/schema-notification.sql` |

已使用的数据表：

| 表名 | 说明 | Schema |
|---|---|---|
| `sys_user` | 用户表 | `campus_user` |
| `market_item` | 二手商品表 | `campus_market` |
| `help_task` | 跑腿任务表 | `campus_task` |
| `club` | 社团表 | `campus_activity` |
| `activity` | 活动表 | `campus_activity` |
| `activity_registration` | 活动报名表 | `campus_activity` |
| `notification` | 通知表 | `campus_notification` |

敏感配置（数据库密码、JWT 密钥）存放在 profile 文件中，不随 `application.yml` 提交：

- `application-local.yml` — 本地开发环境，直接写值，已 `.gitignore`
- `application-dev.yml` — 部署环境，通过环境变量注入，已 `.gitignore`
- `application-*.yml.example` — 模板文件，可安全提交到 Git

使用方式：复制 `.example` 模板为对应 profile 文件，填入真实密码。例如：

```bash
cp campus-user-auth-service/src/main/resources/application-local.yml.example \
   campus-user-auth-service/src/main/resources/application-local.yml
# 编辑 application-local.yml，将 "your_db_password" 替换为实际密码
```

## 5. 服务端口

| 服务 | 端口 | 说明 |
|---|---:|---|
| `campus-gateway` | 9000 | 网关统一入口 |
| `campus-user-auth-service` | 9010 | 用户认证服务 |
| `campus-market-service` | 9020 | 二手交易服务 |
| `campus-task-service` | 9030 | 跑腿任务服务 |
| `campus-activity-service` | 9040 | 活动社团服务 |
| `campus-notification-service` | 9050 | 消息通知服务 |
| `campus-file-service` | 9060 | 文件上传服务 |
| Nacos | 8848 | 服务注册发现 |
| MySQL | 3306 | 数据库 |

## 6. 接口说明

> 面向前端开发的完整接口文档（含字段定义、校验规则、状态流转）见 [`docs/product-prototype.md`](docs/product-prototype.md)。
> 系统设计文档（架构、数据库、安全、设计决策）见 [`docs/design-doc.md`](docs/design-doc.md)。

### 6.1 用户认证服务接口

网关访问路径前缀：`/api`

| 方法 | 网关路径 | 服务内部路径 | 说明 | 是否需要登录 |
|---|---|---|---|---|
| POST | `/api/auth/register` | `/auth/register` | 用户注册 | 否 |
| POST | `/api/auth/login` | `/auth/login` | 用户登录 | 否 |
| POST | `/api/auth/logout` | `/auth/logout` | 用户登出 | 是 |
| GET | `/api/users/me` | `/users/me` | 查询当前用户信息 | 是 |
| PUT | `/api/users/me` | `/users/me` | 修改当前用户信息 | 是 |
| GET | `/api/users/{id}` | `/users/{id}` | 查询用户资料 | 是 |
| GET | `/api/users/simple/{id}` | `/users/simple/{id}` | 查询用户简要信息 | 是 |
| POST | `/api/users/simple/batch` | `/users/simple/batch` | 批量查询用户简要信息 | 是 |

登录请求示例：

```json
{
  "account": "20260001",
  "password": "123456"
}
```

登录成功后会返回：

```json
{
  "token": "Bearer xxxxxx",
  "expireSeconds": 86400,
  "user": {
    "userId": 1,
    "studentNo": "20260001",
    "username": "张三",
    "role": "STUDENT",
    "status": 1
  }
}
```

### 6.2 二手交易服务接口

| 方法 | 网关路径 | 服务内部路径 | 说明 | 是否需要登录 |
|---|---|---|---|---|
| POST | `/api/market/items` | `/market/items` | 发布商品 | 是 |
| GET | `/api/market/items` | `/market/items` | 商品列表 | 是 |
| GET | `/api/market/items/{id}` | `/market/items/{id}` | 商品详情 | 是 |
| PUT | `/api/market/items/{id}` | `/market/items/{id}` | 编辑商品 | 是 |
| PUT | `/api/market/items/{id}/off-shelf` | `/market/items/{id}/off-shelf` | 下架商品 | 是 |
| PUT | `/api/market/items/{id}/sold` | `/market/items/{id}/sold` | 标记已售出 | 是 |
| GET | `/api/market/items/my` | `/market/items/my` | 我的发布 | 是 |

发布商品示例：

```json
{
  "title": "二手自行车",
  "description": "9成新山地车，骑行流畅",
  "price": 299.00,
  "category": "交通工具",
  "coverImage": "https://example.com/bike.jpg",
  "images": "https://example.com/1.jpg,https://example.com/2.jpg",
  "contactInfo": "微信：bike123"
}
```

### 6.3 跑腿任务服务接口

| 方法 | 网关路径 | 服务内部路径 | 说明 | 是否需要登录 |
|---|---|---|---|---|
| POST | `/api/tasks` | `/tasks` | 发布任务 | 是 |
| GET | `/api/tasks` | `/tasks` | 任务大厅 | 是 |
| GET | `/api/tasks/{id}` | `/tasks/{id}` | 任务详情 | 是 |
| PUT | `/api/tasks/{id}/accept` | `/tasks/{id}/accept` | 接单 | 是 |
| PUT | `/api/tasks/{id}/complete` | `/tasks/{id}/complete` | 完成任务 | 是 |
| PUT | `/api/tasks/{id}/cancel` | `/tasks/{id}/cancel` | 取消任务 | 是 |
| GET | `/api/tasks/my-published` | `/tasks/my-published` | 我发布的任务 | 是 |
| GET | `/api/tasks/my-accepted` | `/tasks/my-accepted` | 我接的任务 | 是 |

发布任务示例：

```json
{
  "title": "帮忙取快递",
  "description": "菜鸟驿站有一个快递，请帮忙取到宿舍楼下",
  "taskType": "取快递",
  "reward": 5.00,
  "pickupAddress": "菜鸟驿站",
  "deliveryAddress": "3号宿舍楼201",
  "deadline": "2026-05-15T18:00:00",
  "contactInfo": "微信：abc123"
}
```

### 6.4 活动与社团服务接口

#### 社团接口

| 方法 | 网关路径 | 服务内部路径 | 说明 | 是否需要登录 |
|---|---|---|---|---|
| POST | `/api/clubs` | `/clubs` | 创建社团 | 是 |
| GET | `/api/clubs` | `/clubs` | 社团列表 | 是 |
| GET | `/api/clubs/{id}` | `/clubs/{id}` | 社团详情 | 是 |
| PUT | `/api/clubs/{id}` | `/clubs/{id}` | 修改社团 | 是 |
| GET | `/api/clubs/my` | `/clubs/my` | 我管理的社团 | 是 |

创建社团示例：

```json
{
  "name": "计算机协会",
  "description": "致力于推广计算机技术和校园技术交流",
  "logo": "https://example.com/logo.png",
  "contactInfo": "QQ群：123456"
}
```

#### 活动接口

| 方法 | 网关路径 | 服务内部路径 | 说明 | 是否需要登录 |
|---|---|---|---|---|
| POST | `/api/activities` | `/activities` | 发布活动 | 是 |
| GET | `/api/activities` | `/activities` | 活动列表 | 是 |
| GET | `/api/activities/{id}` | `/activities/{id}` | 活动详情 | 是 |
| POST | `/api/activities/{id}/register` | `/activities/{id}/register` | 报名活动 | 是 |
| DELETE | `/api/activities/{id}/register` | `/activities/{id}/register` | 取消报名 | 是 |
| PUT | `/api/activities/{id}/cancel` | `/activities/{id}/cancel` | 取消活动 | 是 |
| GET | `/api/activities/my-registered` | `/activities/my-registered` | 我的报名 | 是 |
| GET | `/api/activities/my-published` | `/activities/my-published` | 我发布的活动 | 是 |

发布活动示例：

```json
{
  "clubId": 1,
  "title": "编程马拉松",
  "description": "24小时编程挑战赛",
  "location": "图书馆报告厅",
  "coverImage": "https://example.com/activity.png",
  "startTime": "2026-05-15T09:00:00",
  "endTime": "2026-05-16T09:00:00",
  "signupDeadline": "2026-05-14T23:59:59",
  "maxParticipants": 50
}
```

### 6.5 消息通知服务接口

| 方法 | 网关路径 | 服务内部路径 | 说明 | 是否需要登录 |
|---|---|---|---|---|
| POST | `/api/notifications` | `/notifications` | 创建通知 | 是 |
| GET | `/api/notifications` | `/notifications` | 我的通知 | 是 |
| GET | `/api/notifications/unread-count` | `/notifications/unread-count` | 未读数量 | 是 |
| PUT | `/api/notifications/{id}/read` | `/notifications/{id}/read` | 标记单条已读 | 是 |
| PUT | `/api/notifications/read-all` | `/notifications/read-all` | 全部已读 | 是 |
| DELETE | `/api/notifications/{id}` | `/notifications/{id}` | 删除通知 | 是 |

创建通知示例：

```json
{
  "receiverId": 1,
  "title": "任务提醒",
  "content": "你的任务已被接单",
  "type": "TASK",
  "businessId": 1,
  "businessType": "TASK"
}
```

## 7. 认证说明

除登录、注册等白名单接口外，请求需要携带 JWT Token。

请求头格式：

```text
Authorization: Bearer xxxxxx
```

注意：`Bearer` 和 Token 之间必须有一个空格。

网关校验 Token 后，会将用户信息写入下游请求 Header：

```text
X-User-Id: 用户ID
X-Username: 用户名
X-User-Role: 用户角色
```

各业务服务通过 `UserContext` 获取当前用户信息。

## 8. OpenFeign 服务调用说明


### 当前 Feign 调用关系

| 调用方 | 被调用方 | 说明 |
|---|---|---|
| `campus-market-service` | `campus-user-auth-service` | 查询商品发布者用户名 |
| `campus-task-service` | `campus-user-auth-service` | 查询任务发布者、接单者用户名 |
| `campus-activity-service` | `campus-user-auth-service` | 查询社团管理员、活动发布者用户名 |

### Feign 公共客户端

公共模块中提供：

```text
com.god.common.client.UserFeignClient
```

主要接口：

```text
GET /users/simple/{id}
POST /users/simple/batch
```

### Feign 容错说明

当前业务中对用户名查询做了兜底处理：

- Feign 调用成功：展示真实用户名。
- Feign 调用失败：展示 `用户{id}`。

这样可以避免用户服务短暂不可用时影响商品、任务、活动详情接口的基本可用性。

## 9. Swagger / Knife4j 文档地址

每个业务服务都提供独立的接口文档。

| 服务 | 文档地址 |
|---|---|
| 用户认证服务 | `http://localhost:9010/doc.html` |
| 二手交易服务 | `http://localhost:9020/doc.html` |
| 跑腿任务服务 | `http://localhost:9030/doc.html` |
| 活动社团服务 | `http://localhost:9040/doc.html` |
| 消息通知服务 | `http://localhost:9050/doc.html` |
| 文件上传服务 | `http://localhost:9060/doc.html` |

在 Knife4j 中测试需要登录的接口时，需要配置请求头：

```text
Authorization: Bearer xxxxxx
```

如果使用全局参数配置，请确认 `Bearer` 后有空格。

## 10. 启动步骤

### 10.1 启动基础服务

1. 启动 MySQL。
2. 执行 `sql/` 目录下所有 `schema-*.sql` 文件创建各服务独立数据库。
3. 启动 Nacos，默认地址：`http://localhost:8848/nacos`。

### 10.2 配置数据库密码

每个服务需要 `application-local.yml`（本地开发）或 `application-dev.yml`（部署环境）来提供数据库密码和 JWT 密钥。

首次使用请从模板复制：

```bash
for module in campus-user-auth-service campus-market-service campus-task-service \
              campus-activity-service campus-notification-service campus-gateway; do
  cp "$module/src/main/resources/application-local.yml.example" \
     "$module/src/main/resources/application-local.yml"
done
```

然后修改各 `application-local.yml` 中的 `your_db_password` 为实际密码，`your-jwt-secret-key` 为实际密钥。

### 10.3 编译项目

在项目根目录执行：

```bash
mvn clean compile -DskipTests
```

### 10.4 启动微服务

推荐启动顺序：

1. `campus-user-auth-service`
2. `campus-market-service`
3. `campus-task-service`
4. `campus-activity-service`
5. `campus-notification-service`
6. `campus-file-service`
7. `campus-gateway`

启动完成后，可在 Nacos 控制台查看服务是否注册成功。

服务名包括：

```text
campus-gateway
campus-user-auth-service
campus-market-service
campus-task-service
campus-activity-service
campus-notification-service
campus-file-service
```

## 11. 推荐测试流程

### 11.1 注册用户

```text
POST http://localhost:9000/api/auth/register
```

```json
{
  "studentNo": "20260001",
  "username": "张三",
  "password": "123456",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "college": "计算机学院",
  "major": "软件工程"
}
```

### 11.2 登录获取 Token

```text
POST http://localhost:9000/api/auth/login
```

```json
{
  "account": "20260001",
  "password": "123456"
}
```

复制返回的 `token` 字段。

### 11.3 使用 Token 请求业务接口

例如发布商品：

```text
POST http://localhost:9000/api/market/items
Authorization: Bearer xxxxxx
Content-Type: application/json
```

```json
{
  "title": "二手自行车",
  "description": "9成新山地车",
  "price": 299.00,
  "category": "交通工具",
  "contactInfo": "微信：bike123"
}
```

### 11.4 测试跑腿任务

```text
POST http://localhost:9000/api/tasks
```

```json
{
  "title": "帮忙取快递",
  "description": "菜鸟驿站取快递送到宿舍楼下",
  "taskType": "取快递",
  "reward": 5.00,
  "pickupAddress": "菜鸟驿站",
  "deliveryAddress": "3号宿舍楼201",
  "contactInfo": "微信：abc123"
}
```

### 11.5 测试活动报名

1. 创建社团。
2. 发布活动。
3. 使用另一个账号登录。
4. 报名活动。
5. 查看我的报名。

### 11.6 测试消息通知

```text
POST http://localhost:9000/api/notifications
```

```json
{
  "receiverId": 1,
  "title": "系统通知",
  "content": "欢迎使用校园综合生活服务平台",
  "type": "SYSTEM",
  "businessId": null,
  "businessType": "SYSTEM"
}
```

然后查询：

```text
GET http://localhost:9000/api/notifications
GET http://localhost:9000/api/notifications/unread-count
```

## 12. 后续优化方向

当前版本以课程项目可运行、可演示为目标。后续可以继续优化：

1. 接入 RabbitMQ，实现异步消息通知。
2. 接入 Redis，实现 Token 黑名单、热点商品缓存、验证码。
3. 接入 Nacos Config，实现统一配置中心。
4. 增加管理员后台接口。
5. 增加数据统计接口，例如商品数量、任务完成率、活动报名人数。
6. 增加接口权限控制，例如管理员、社团管理员、普通学生区分。
7. 增加单元测试和接口测试集合。

## 13. 当前项目状态

当前项目已经完成以下核心能力：

- 用户注册登录
- 网关 JWT 鉴权
- 二手商品管理
- 跑腿任务管理
- 社团与活动管理
- 消息通知管理
- OpenFeign 用户信息查询
- Nacos 服务注册发现
- Knife4j 接口文档
- MySQL 数据持久化
- 文件上传服务，实现商品图片、社团 Logo、活动封面上传。