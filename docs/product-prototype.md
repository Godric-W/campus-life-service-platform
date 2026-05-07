# 校园综合生活服务平台 — 产品原型

> 面向前端开发的完整接口文档、数据模型与交互流程

---

## 1. 产品概述

校园综合生活服务平台，包含五大业务模块：二手交易、跑腿任务、社团活动、消息通知、用户认证。

### 1.1 用户角色

| 角色 | 标识 | 权限 |
|---|---|---|
| 普通学生 | `STUDENT` | 发布/购买商品、发布/接取任务、报名活动 |
| 社团管理员 | `CLUB_ADMIN` | 学生权限 + 创建社团、以社团身份发布活动 |
| 系统管理员 | `ADMIN` | 全权限（预留） |

### 1.2 技术约束

- 所有需认证的接口在 Header 中携带：`Authorization: Bearer <token>`
- 统一响应格式：`{ "code": 200, "message": "操作成功", "data": {...} }`
- 分页响应：`{ "code": 200, "message": "操作成功", "data": { "total": 100, "pageNum": 1, "pageSize": 10, "records": [...] } }`
- 基础路径：`http://localhost:9000/api`

---

## 2. 通用规范

### 2.1 统一响应结构 `Result<T>`

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| code | 含义 |
|---|---|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 资源冲突（重复注册/报名等） |
| 500 | 系统异常 |

业务错误码：

| code | 含义 |
|---|---|
| 1001 | 用户名或密码错误 |
| 1002 | 用户已被禁用 |
| 1003 | 用户不存在 |
| 1004 | Token 无效 |
| 2001 | 商品不存在 |
| 2002 | 商品状态不允许该操作 |
| 3001 | 任务不存在 |
| 3002 | 任务状态不允许该操作 |
| 4001 | 活动不存在 |
| 4002 | 活动报名已截止 |
| 4003 | 活动报名人数已满 |
| 5001 | 通知不存在 |

### 2.2 分页响应结构 `PageResult<T>`

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

### 2.3 分页请求参数

所有列表接口统一使用以下 Query 参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|---|---|---|---|---|
| pageNum | Long | 否 | 1 | 页码，>=1 |
| pageSize | Long | 否 | 10 | 每页条数，1-100 |

---

## 3. 用户认证模块

**服务端口：** 9010  
**网关路径前缀：** `/api/auth/**`, `/api/users/**`

### 3.1 数据模型

#### UserProfileVO（用户信息）
```json
{
  "id": 1,
  "studentNo": "20240001",
  "username": "张三",
  "phone": "13800000001",
  "email": "zhangsan@campus.edu",
  "avatar": "https://...",
  "college": "计算机学院",
  "major": "软件工程",
  "role": "STUDENT",
  "status": 1,
  "createTime": "2026-05-07T10:00:00"
}
```

#### UserSimpleDTO（用户简要信息，Feign 用）
```json
{
  "userId": 1,
  "studentNo": "20240001",
  "username": "张三",
  "avatar": "https://...",
  "college": "计算机学院",
  "major": "软件工程",
  "role": "STUDENT",
  "status": 1
}
```

#### LoginResponse（登录响应）
```json
{
  "token": "Bearer eyJhbGci...",
  "expireSeconds": 86400,
  "user": { ...UserProfileVO }
}
```

### 3.2 接口清单

#### POST /api/auth/register — 注册

```
Content-Type: application/json
```

| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| studentNo | String | ✅ | max 32，唯一 |
| username | String | ✅ | max 50 |
| password | String | ✅ | 6-32 位 |
| phone | String | ❌ | 1[3-9]xxxxxxxxx |
| email | String | ❌ | 邮箱格式 |
| college | String | ❌ | |
| major | String | ❌ | |

**响应：** `Result<Void>`

#### POST /api/auth/login — 登录

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| account | String | ✅ | 学号/用户名/手机/邮箱 |
| password | String | ✅ | |

**响应：** `Result<LoginResponse>`

#### POST /api/auth/logout — 登出

```
Authorization: Bearer <token>
```

**响应：** `Result<Void>`

#### GET /api/users/me — 当前用户信息

```
Authorization: Bearer <token>
```

**响应：** `Result<UserProfileVO>`

#### PUT /api/users/me — 更新个人信息

```
Authorization: Bearer <token>
```

| 字段 | 类型 | 必填 |
|---|---|---|
| username | String | ❌ |
| phone | String | ❌ |
| email | String | ❌ |
| avatar | String | ❌ |
| college | String | ❌ |
| major | String | ❌ |

**响应：** `Result<Void>`

#### GET /api/users/{id} — 用户公开信息

无需认证（白名单路径）

**响应：** `Result<UserProfileVO>`

#### GET /api/users/simple/{id} — 用户简要信息（Feign）

**响应：** `Result<UserSimpleDTO>`

#### POST /api/users/simple/batch — 批量查询用户

```
Authorization: Bearer <token>
Body: [1, 2, 3]  (Long 数组)
```

**响应：** `Result<Map<Long, UserSimpleDTO>>`

---

## 4. 二手交易模块

**服务端口：** 9020  
**网关路径前缀：** `/api/market/**`

### 4.1 商品状态枚举

| 状态值 | 含义 | 可执行操作 |
|---|---|---|
| `ON_SALE` | 在售 | 编辑、下架、标记已售 |
| `LOCKED` | 锁定（预留） | - |
| `SOLD` | 已售出 | - |
| `OFF_SHELF` | 已下架 | - |

### 4.2 数据模型

#### MarketItemListVO（列表项）
```json
{
  "id": 1,
  "sellerId": 1,
  "sellerName": "张三",
  "title": "二手 MacBook Pro 2023",
  "price": 8999.00,
  "category": "数码电子",
  "coverImage": "https://...",
  "status": "ON_SALE",
  "viewCount": 128,
  "createTime": "2026-05-07T10:00:00"
}
```

#### MarketItemVO（详情）
```json
{
  "id": 1,
  "sellerId": 1,
  "sellerName": "张三",
  "title": "二手 MacBook Pro 2023",
  "description": "M3 Pro芯片，16GB内存，512GB SSD，使用半年，九成新",
  "price": 8999.00,
  "category": "数码电子",
  "coverImage": "https://...",
  "images": "url1,url2,url3",
  "contactInfo": "微信：zhangsan_001",
  "status": "ON_SALE",
  "viewCount": 128,
  "createTime": "2026-05-07T10:00:00",
  "updateTime": "2026-05-07T10:00:00"
}
```

### 4.3 接口清单

#### POST /api/market/items — 发布商品

```
Authorization: Bearer <token>
```

| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| title | String | ✅ | max 100 |
| description | String | ❌ | max 1000 |
| price | BigDecimal | ✅ | >= 0.01 |
| category | String | ❌ | max 50 |
| coverImage | String | ❌ | |
| images | String | ❌ | 多图，逗号分隔 URL |
| contactInfo | String | ❌ | 联系方式 |

**响应：** `Result<Long>`（返回商品 ID）

#### GET /api/market/items — 商品列表

无需认证

| Query 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| pageNum | Long | ❌ | 默认 1 |
| pageSize | Long | ❌ | 默认 10 |
| keyword | String | ❌ | 搜索标题+描述 |
| category | String | ❌ | 分类筛选 |
| status | String | ❌ | 状态筛选（默认 ON_SALE） |
| sellerId | Long | ❌ | 卖家 ID |

**响应：** `PageResult<MarketItemListVO>`

#### GET /api/market/items/{id} — 商品详情

无需认证。自动增加浏览量。

**响应：** `Result<MarketItemVO>`

#### PUT /api/market/items/{id} — 编辑商品

```
Authorization: Bearer <token>
```
只能操作自己的商品。已售出商品不可编辑。

| 字段 | 类型 | 必填 |
|---|---|---|
| title | String | ❌ |
| description | String | ❌ |
| price | BigDecimal | ❌ |
| category | String | ❌ |
| coverImage | String | ❌ |
| images | String | ❌ |
| contactInfo | String | ❌ |

**响应：** `Result<Void>`

#### PUT /api/market/items/{id}/off-shelf — 下架商品

```
Authorization: Bearer <token>
```
只能操作自己的商品。

**响应：** `Result<Void>`

#### PUT /api/market/items/{id}/sold — 标记已售

```
Authorization: Bearer <token>
```
只能操作自己的商品。

**响应：** `Result<Void>`

#### GET /api/market/items/my — 我的商品

```
Authorization: Bearer <token>
```

参数同列表接口（sellerId 自动填充）。

**响应：** `PageResult<MarketItemListVO>`

---

## 5. 跑腿任务模块

**服务端口：** 9030  
**网关路径前缀：** `/api/tasks/**`

### 5.1 任务状态枚举与流转

```
                  ┌──────────┐
                  │ PUBLISHED │ ← 发布
                  └────┬─────┘
                       │ 接单
                  ┌────▼─────┐
                  │ ACCEPTED  │
                  └────┬─────┘
                       │ 完成
                  ┌────▼─────┐
                  │ COMPLETED │
                  └──────────┘
                  PUBLISHED → CANCELLED (发布者取消)
```

| 状态值 | 含义 |
|---|---|
| `PUBLISHED` | 已发布，待接单 |
| `ACCEPTED` | 已接单，执行中 |
| `COMPLETED` | 已完成 |
| `CANCELLED` | 已取消 |

### 5.2 数据模型

#### HelpTaskListVO（列表项）
```json
{
  "id": 1,
  "publisherId": 1,
  "publisherName": "张三",
  "title": "帮忙取快递",
  "taskType": "取快递",
  "reward": 5.00,
  "pickupAddress": "3号驿站",
  "deliveryAddress": "7号宿舍楼",
  "deadline": "2026-05-09T18:00:00",
  "status": "PUBLISHED",
  "createTime": "2026-05-07T10:00:00"
}
```

#### HelpTaskVO（详情）
```json
{
  "id": 1,
  "publisherId": 1,
  "publisherName": "张三",
  "accepterId": 2,
  "accepterName": "李四",
  "title": "帮忙取快递",
  "description": "中通快递 3号驿站，取货码 1234，送到7号宿舍楼下",
  "taskType": "取快递",
  "reward": 5.00,
  "pickupAddress": "3号驿站",
  "deliveryAddress": "7号宿舍楼",
  "deadline": "2026-05-09T18:00:00",
  "contactInfo": "微信：zhangsan_001",
  "status": "ACCEPTED",
  "createTime": "2026-05-07T10:00:00",
  "updateTime": "2026-05-07T10:05:00"
}
```

### 5.3 接口清单

#### POST /api/tasks — 发布任务

```
Authorization: Bearer <token>
```

| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| title | String | ✅ | max 100 |
| description | String | ❌ | max 1000 |
| taskType | String | ✅ | max 50 |
| reward | BigDecimal | ✅ | >= 0.00 |
| pickupAddress | String | ❌ | max 255 |
| deliveryAddress | String | ❌ | max 255 |
| deadline | DateTime | ❌ | |
| contactInfo | String | ❌ | |

**响应：** `Result<Long>`

#### GET /api/tasks — 任务列表

无需认证

| Query 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| pageNum | Long | ❌ | |
| pageSize | Long | ❌ | |
| keyword | String | ❌ | 搜索标题+描述 |
| taskType | String | ❌ | |
| status | String | ❌ | 默认 PUBLISHED |
| publisherId | Long | ❌ | |
| accepterId | Long | ❌ | |

**响应：** `PageResult<HelpTaskListVO>`

#### GET /api/tasks/{id} — 任务详情

无需认证

**响应：** `Result<HelpTaskVO>`

#### PUT /api/tasks/{id}/accept — 接单

```
Authorization: Bearer <token>
```
不能接自己发布的任务。只能接 PUBLISHED 状态的任务。

**响应：** `Result<Void>`

#### PUT /api/tasks/{id}/complete — 完成任务

```
Authorization: Bearer <token>
```
只有接单者可以完成。只能完成 ACCEPTED 状态的任务。

**响应：** `Result<Void>`

#### PUT /api/tasks/{id}/cancel — 取消任务

```
Authorization: Bearer <token>
```
只有发布者可以取消。只能取消 PUBLISHED 状态的任务。

**响应：** `Result<Void>`

#### GET /api/tasks/my-published — 我发布的

```
Authorization: Bearer <token>
```

**响应：** `PageResult<HelpTaskListVO>`

#### GET /api/tasks/my-accepted — 我接取的

```
Authorization: Bearer <token>
```

**响应：** `PageResult<HelpTaskListVO>`

---

## 6. 社团 & 活动模块

**服务端口：** 9040  
**网关路径前缀：** `/api/activities/**`, `/api/clubs/**`

### 6.1 活动状态枚举

| 状态值 | 含义 |
|---|---|
| `DRAFT` | 草稿 |
| `PUBLISHED` | 已发布 |
| `REGISTRATION_CLOSED` | 报名截止 |
| `FINISHED` | 已结束 |
| `CANCELLED` | 已取消 |

### 6.2 数据模型

#### ClubVO（社团）
```json
{
  "id": 1,
  "name": "编程协会",
  "description": "聚集校园编程爱好者，每周技术分享",
  "logo": "https://...",
  "adminId": 3,
  "adminName": "管理员小王",
  "contactInfo": "微信群：CampusDevGroup",
  "createTime": "2026-05-07T10:00:00",
  "updateTime": "2026-05-07T10:00:00"
}
```

#### ActivityListVO（活动列表项）
```json
{
  "id": 1,
  "clubId": 1,
  "clubName": "编程协会",
  "title": "Spring Boot 入门实战工作坊",
  "location": "教学楼B201",
  "coverImage": "https://...",
  "startTime": "2026-05-20T14:00:00",
  "endTime": "2026-05-20T17:00:00",
  "signupDeadline": "2026-05-18T23:59:59",
  "maxParticipants": 30,
  "currentParticipants": 12,
  "status": "PUBLISHED",
  "createTime": "2026-05-07T10:00:00"
}
```

#### ActivityVO（活动详情）
```json
{
  "id": 1,
  "clubId": 1,
  "clubName": "编程协会",
  "publisherId": 3,
  "publisherName": "管理员小王",
  "title": "Spring Boot 入门实战工作坊",
  "description": "面向新生的Spring Boot入门教学，学会搭建第一个微服务。请自带电脑。",
  "location": "教学楼B201",
  "coverImage": "https://...",
  "startTime": "2026-05-20T14:00:00",
  "endTime": "2026-05-20T17:00:00",
  "signupDeadline": "2026-05-18T23:59:59",
  "maxParticipants": 30,
  "currentParticipants": 12,
  "status": "PUBLISHED",
  "isRegistered": true,
  "createTime": "2026-05-07T10:00:00",
  "updateTime": "2026-05-07T10:00:00"
}
```

### 6.3 社团接口

#### POST /api/clubs — 创建社团

```
Authorization: Bearer <token>
```

| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| name | String | ✅ | max 100 |
| description | String | ❌ | max 1000 |
| logo | String | ❌ | |
| contactInfo | String | ❌ | |

创建者自动成为社团管理员。

**响应：** `Result<Long>`

#### GET /api/clubs — 社团列表

无需认证。返回全部社团。

**响应：** `Result<List<ClubVO>>`

#### GET /api/clubs/{id} — 社团详情

无需认证。

**响应：** `Result<ClubVO>`

#### PUT /api/clubs/{id} — 修改社团

```
Authorization: Bearer <token>
```
只有社团管理员可以修改。

| 字段 | 类型 | 必填 |
|---|---|---|
| name | String | ✅ |
| description | String | ❌ |
| logo | String | ❌ |
| contactInfo | String | ❌ |

**响应：** `Result<Void>`

#### GET /api/clubs/my — 我管理的社团

```
Authorization: Bearer <token>
```

**响应：** `Result<List<ClubVO>>`

### 6.4 活动接口

#### POST /api/activities — 发布活动

```
Authorization: Bearer <token>
```

| 字段 | 类型 | 必填 | 校验 | 说明 |
|---|---|---|---|---|
| clubId | Long | ❌ | | 社团活动需填，且发布者须是社团管理员 |
| title | String | ✅ | max 100 | |
| description | String | ❌ | max 1000 | |
| location | String | ❌ | max 255 | |
| coverImage | String | ❌ | | |
| startTime | DateTime | ✅ | | ISO 格式 |
| endTime | DateTime | ✅ | | |
| signupDeadline | DateTime | ❌ | | 报名截止时间 |
| maxParticipants | Integer | ❌ | >= 1 | |

**响应：** `Result<Long>`

#### GET /api/activities — 活动列表

无需认证

| Query 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| pageNum | Long | ❌ | |
| pageSize | Long | ❌ | |
| keyword | String | ❌ | 搜索标题+描述 |
| clubId | Long | ❌ | |
| status | String | ❌ | 默认 PUBLISHED |
| publisherId | Long | ❌ | |

**响应：** `PageResult<ActivityListVO>`

#### GET /api/activities/{id} — 活动详情

```
Authorization: Bearer <token>（可选）
```
带 token 时会返回 `isRegistered` 字段。

**响应：** `Result<ActivityVO>`

#### POST /api/activities/{id}/register — 报名活动

```
Authorization: Bearer <token>
```
- 活动必须是 PUBLISHED 状态
- 未过报名截止时间
- 未满员
- 不可重复报名

**响应：** `Result<Void>`

#### DELETE /api/activities/{id}/register — 取消报名

```
Authorization: Bearer <token>
```

**响应：** `Result<Void>`

#### PUT /api/activities/{id}/cancel — 取消活动

```
Authorization: Bearer <token>
```
只有发布者可以取消。

**响应：** `Result<Void>`

#### GET /api/activities/my-registered — 我报名的

```
Authorization: Bearer <token>
```

**响应：** `PageResult<ActivityListVO>`

#### GET /api/activities/my-published — 我发布的

```
Authorization: Bearer <token>
```

**响应：** `PageResult<ActivityListVO>`

---

## 7. 消息通知模块

**服务端口：** 9050  
**网关路径前缀：** `/api/notifications/**`

### 7.1 通知类型枚举

| 类型值 | 含义 |
|---|---|
| `SYSTEM` | 系统通知 |
| `MARKET` | 二手交易 |
| `TASK` | 跑腿任务 |
| `ACTIVITY` | 社团活动 |

### 7.2 NotificationVO

```json
{
  "id": 1,
  "receiverId": 1,
  "title": "欢迎加入校园服务平台",
  "content": "欢迎使用校园综合生活服务平台！",
  "type": "SYSTEM",
  "businessId": null,
  "businessType": null,
  "readStatus": 0,
  "createTime": "2026-05-07T10:00:00"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| readStatus | Integer | 0=未读，1=已读 |

### 7.3 接口清单

#### POST /api/notifications — 创建通知

```
Authorization: Bearer <token>
```

| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| receiverId | Long | ✅ | |
| title | String | ✅ | max 100 |
| content | String | ✅ | max 500 |
| type | String | ✅ | 见枚举 |
| businessId | Long | ❌ | 关联业务 ID |
| businessType | String | ❌ | 关联业务类型 |

**响应：** `Result<Void>`

#### GET /api/notifications — 我的通知

```
Authorization: Bearer <token>
```

| Query 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| pageNum | Long | ❌ | |
| pageSize | Long | ❌ | |
| type | String | ❌ | |
| readStatus | Integer | ❌ | 0 或 1 |

**响应：** `PageResult<NotificationVO>`

#### GET /api/notifications/unread-count — 未读数量

```
Authorization: Bearer <token>
```

**响应：** `Result<Integer>`

#### PUT /api/notifications/{id}/read — 标记已读

```
Authorization: Bearer <token>
```
只能操作自己的通知。

**响应：** `Result<Void>`

#### PUT /api/notifications/read-all — 全部已读

```
Authorization: Bearer <token>
```

**响应：** `Result<Void>`

#### DELETE /api/notifications/{id} — 删除通知

```
Authorization: Bearer <token>
```
只能删除自己的通知。

**响应：** `Result<Void>`

---

## 8. 页面结构建议

```
├── 登录/注册
│   ├── 登录页
│   └── 注册页
│
├── 首页（Tab 导航）
│   ├── 二手交易
│   │   ├── 商品列表（搜索/分类筛选）
│   │   ├── 商品详情
│   │   ├── 发布商品
│   │   └── 我的商品
│   │
│   ├── 跑腿任务
│   │   ├── 任务列表（搜索/类型筛选）
│   │   ├── 任务详情
│   │   ├── 发布任务
│   │   ├── 我发布的
│   │   └── 我接取的
│   │
│   ├── 社团活动
│   │   ├── 社团列表
│   │   ├── 社团详情
│   │   ├── 创建社团
│   │   ├── 活动列表（搜索/社团筛选）
│   │   ├── 活动详情
│   │   ├── 发布活动
│   │   ├── 我报名的
│   │   └── 我发布的
│   │
│   └── 消息
│       ├── 通知列表（类型筛选）
│       └── 通知详情
│
└── 个人中心
    ├── 个人信息
    ├── 修改资料
    ├── 我管理的社团
    └── 退出登录
```

---

## 9. 交互状态说明

### 9.1 商品状态流转

```
发布 → ON_SALE ──下架──→ OFF_SHELF
         │
         └──标记已售──→ SOLD
```

### 9.2 任务状态流转

```
发布 → PUBLISHED ──接单──→ ACCEPTED ──完成──→ COMPLETED
          │
          └──取消──→ CANCELLED
```

### 9.3 活动状态与操作

| 状态 | 报名 | 取消报名 | 取消活动 | 查看 |
|---|---|---|---|---|
| PUBLISHED | ✅ | ✅ | ✅（发布者） | ✅ |
| 其他状态 | ❌ | ❌ | ❌ | ✅ |

---

## 10. 前端开发要点

1. **Token 管理**：登录成功后存储 token，每次请求在 Header 带上 `Authorization: Bearer <token>`
2. **用户上下文**：token 含 userId/username/role，解析后全局可用
3. **列表加载**：所有分页列表加 `pageNum`/`pageSize`，支持下拉刷新和上拉加载
4. **错误处理**：401 跳转登录页，403 提示"无权限"，409 提示对应冲突信息
5. **列表中的用户名**：服务端通过 Feign 已填充 `sellerName`/`publisherName` 等，前端直接展示即可
6. **图片上传**：目前接口没有图片上传端点，`coverImage`/`images` 字段接受 URL 字符串
7. **消息通知**：建议在页面顶部或 Tab 上显示未读数量角标
8. **活动报名**：列表的 `currentParticipants` 和 `maxParticipants` 用来展示报名进度条
