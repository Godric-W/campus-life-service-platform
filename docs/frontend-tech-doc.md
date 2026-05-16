# 校园综合生活服务平台 - 前端技术文档

## 1. 项目概述

本文档为校园综合生活服务平台的前端技术设计文档，基于后端微服务架构设计对应的前端界面和交互逻辑。

### 1.1 后端服务概述

| 服务 | 端口 | 说明 |
|------|------|------|
| campus-gateway | 9000 | 网关统一入口 |
| campus-user-auth-service | 9010 | 用户认证服务 |
| campus-market-service | 9020 | 二手交易服务 |
| campus-task-service | 9030 | 跑腿任务服务 |
| campus-activity-service | 9040 | 活动社团服务 |
| campus-notification-service | 9050 | 消息通知服务 |
| campus-file-service | 9060 | 文件上传服务 |

---

## 2. 技术栈选择

### 2.1 核心框架

| 分类 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 框架 | Vue | 3.4 | 渐进式JavaScript框架 |
| 语言 | TypeScript | 5.3 | 类型安全的JavaScript超集 |
| 构建工具 | Vite | 5 | 下一代前端构建工具 |
| 状态管理 | Pinia | 2.1 | Vue官方状态管理库 |
| UI组件库 | Element Plus | 2.6 | Vue 3组件库 |
| 路由 | Vue Router | 4.3 | Vue官方路由管理器 |
| HTTP客户端 | Axios | 1.6 | Promise-based HTTP客户端 |
| 样式 | SCSS | 1.70 | CSS预处理器 |

### 2.2 技术选型理由

- **Vue 3 + TypeScript**：提供类型安全、Composition API、更好的性能和开发体验
- **Vite**：快速冷启动、即时热更新、优化的构建输出
- **Element Plus**：成熟稳定、组件丰富、支持Vue 3、中文文档完善
- **Pinia**：Vue官方推荐、支持TypeScript、轻量易用
- **Axios**：广泛使用、支持拦截器、取消请求等功能

---

## 3. 架构设计

### 3.1 架构原则

- **组件化**：UI拆分为可复用组件
- **模块化**：按业务功能划分模块
- **单一职责**：每个组件/模块职责清晰
- **状态管理**：全局状态集中管理，局部状态组件自治
- **类型安全**：TypeScript严格模式

### 3.2 目录结构

```
frontend/
├── src/
│   ├── components/          # 公共组件
│   │   ├── Layout/          # 布局组件
│   │   ├── Common/          # 通用组件
│   │   └── Business/        # 业务组件
│   ├── views/               # 页面视图
│   │   ├── Auth/            # 认证模块
│   │   ├── Market/          # 二手交易模块
│   │   ├── Task/            # 跑腿任务模块
│   │   ├── Activity/        # 活动社团模块
│   │   └── Notification/    # 消息通知模块
│   ├── stores/              # Pinia状态管理
│   ├── api/                 # API接口定义
│   ├── types/               # TypeScript类型定义
│   ├── utils/               # 工具函数
│   ├── router/              # 路由配置
│   ├── styles/              # 全局样式
│   └── App.vue
├── public/                  # 静态资源
├── index.html               # HTML入口
├── package.json
├── vite.config.ts           # Vite配置
├── tsconfig.json            # TypeScript配置
└── tailwind.config.js       # Tailwind配置（如使用）
```

### 3.3 模块划分

| 模块 | 说明 | 页面/组件 |
|------|------|-----------|
| Auth | 用户认证 | 登录、注册、个人中心 |
| Market | 二手交易 | 商品列表、商品详情、发布商品 |
| Task | 跑腿任务 | 任务大厅、任务详情、发布任务 |
| Activity | 活动社团 | 社团列表、活动列表、活动详情 |
| Notification | 消息通知 | 通知列表 |

---

## 4. 组件设计规范

### 4.1 组件分类

1. **布局组件**：Header、Sidebar、Footer、Layout
2. **通用组件**：Button、Input、Modal、Card、Table、Pagination
3. **业务组件**：商品卡片、任务卡片、活动卡片、通知卡片

### 4.2 命名规范

- **组件文件**：大驼峰命名，如 `UserCard.vue`
- **组件名称**：大驼峰命名，与文件名一致
- **props**：小驼峰命名，如 `userId`
- **事件**：短横线分隔，如 `update:visible`

### 4.3 组件设计原则

- **可复用性**：组件设计考虑通用性
- **可配置性**：通过props灵活配置
- **事件驱动**：通过emit向外传递事件
- **样式隔离**：使用scoped样式

---

## 5. 接口调用说明

### 5.1 基础配置

**API基础地址**：`http://localhost:9000/api`

**请求头配置**：
```typescript
{
  'Content-Type': 'application/json',
  'Authorization': 'Bearer ' + token
}
```

### 5.2 认证模块接口

| 方法 | 路径 | 说明 | 是否需要登录 |
|------|------|------|-------------|
| POST | `/auth/register` | 用户注册 | 否 |
| POST | `/auth/login` | 用户登录 | 否 |
| POST | `/auth/logout` | 用户登出 | 是 |
| GET | `/users/me` | 查询当前用户信息 | 是 |
| PUT | `/users/me` | 修改当前用户信息 | 是 |
| GET | `/users/{id}` | 查询用户资料 | 是 |

**类型定义**：
```typescript
interface LoginRequest {
  account: string;      // 账号（学号或手机号）
  password: string;     // 密码
}

interface RegisterRequest {
  studentNo: string;    // 学号
  username: string;     // 用户名
  password: string;     // 密码
  phone: string;        // 手机号
  email: string;        // 邮箱
  college: string;      // 学院
  major: string;        // 专业
}

interface LoginResponse {
  token: string;        // JWT Token
  expireSeconds: number; // 过期时间（秒）
  user: {
    userId: number;
    studentNo: string;
    username: string;
    role: string;
    status: number;
  };
}

interface UserProfile {
  userId: number;
  studentNo: string;
  username: string;
  phone: string;
  email: string;
  college: string;
  major: string;
  avatar: string;
  role: string;
  status: number;
  createdAt: string;
  updatedAt: string;
}
```

### 5.3 二手交易模块接口

| 方法 | 路径 | 说明 | 是否需要登录 |
|------|------|------|-------------|
| POST | `/market/items` | 发布商品 | 是 |
| GET | `/market/items` | 商品列表 | 是 |
| GET | `/market/items/{id}` | 商品详情 | 是 |
| PUT | `/market/items/{id}` | 编辑商品 | 是 |
| PUT | `/market/items/{id}/off-shelf` | 下架商品 | 是 |
| PUT | `/market/items/{id}/sold` | 标记已售出 | 是 |
| GET | `/market/items/my` | 我的发布 | 是 |

**类型定义**：
```typescript
interface MarketItem {
  id: number;
  title: string;
  description: string;
  price: number;
  category: string;
  coverImage: string;
  images: string;       // 逗号分隔的图片URL
  contactInfo: string;
  status: string;       // 'ON_SHELF', 'OFF_SHELF', 'SOLD'
  publisherId: number;
  publisherName: string;
  createdAt: string;
  updatedAt: string;
}

interface PublishItemRequest {
  title: string;
  description: string;
  price: number;
  category: string;
  coverImage?: string;
  images?: string;
  contactInfo: string;
}

interface MarketItemQuery {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  category?: string;
  status?: string;
}
```

### 5.4 跑腿任务模块接口

| 方法 | 路径 | 说明 | 是否需要登录 |
|------|------|------|-------------|
| POST | `/tasks` | 发布任务 | 是 |
| GET | `/tasks` | 任务大厅 | 是 |
| GET | `/tasks/{id}` | 任务详情 | 是 |
| PUT | `/tasks/{id}/accept` | 接单 | 是 |
| PUT | `/tasks/{id}/complete` | 完成任务 | 是 |
| PUT | `/tasks/{id}/cancel` | 取消任务 | 是 |
| GET | `/tasks/my-published` | 我发布的任务 | 是 |
| GET | `/tasks/my-accepted` | 我接的任务 | 是 |

**类型定义**：
```typescript
interface HelpTask {
  id: number;
  title: string;
  description: string;
  taskType: string;
  reward: number;
  pickupAddress: string;
  deliveryAddress: string;
  deadline: string;
  contactInfo: string;
  status: string;       // 'PENDING', 'ACCEPTED', 'COMPLETED', 'CANCELLED'
  publisherId: number;
  publisherName: string;
  acceptorId?: number;
  acceptorName?: string;
  createdAt: string;
  updatedAt: string;
}

interface PublishTaskRequest {
  title: string;
  description: string;
  taskType: string;
  reward: number;
  pickupAddress: string;
  deliveryAddress: string;
  deadline: string;
  contactInfo: string;
}
```

### 5.5 活动社团模块接口

#### 社团接口

| 方法 | 路径 | 说明 | 是否需要登录 |
|------|------|------|-------------|
| POST | `/clubs` | 创建社团 | 是 |
| GET | `/clubs` | 社团列表 | 是 |
| GET | `/clubs/{id}` | 社团详情 | 是 |
| PUT | `/clubs/{id}` | 修改社团 | 是 |
| GET | `/clubs/my` | 我管理的社团 | 是 |

#### 活动接口

| 方法 | 路径 | 说明 | 是否需要登录 |
|------|------|------|-------------|
| POST | `/activities` | 发布活动 | 是 |
| GET | `/activities` | 活动列表 | 是 |
| GET | `/activities/{id}` | 活动详情 | 是 |
| POST | `/activities/{id}/register` | 报名活动 | 是 |
| DELETE | `/activities/{id}/register` | 取消报名 | 是 |
| PUT | `/activities/{id}/cancel` | 取消活动 | 是 |
| GET | `/activities/my-registered` | 我的报名 | 是 |
| GET | `/activities/my-published` | 我发布的活动 | 是 |

**类型定义**：
```typescript
interface Club {
  id: number;
  name: string;
  description: string;
  logo: string;
  contactInfo: string;
  managerId: number;
  managerName: string;
  memberCount: number;
  createdAt: string;
  updatedAt: string;
}

interface Activity {
  id: number;
  clubId: number;
  clubName: string;
  title: string;
  description: string;
  location: string;
  coverImage: string;
  startTime: string;
  endTime: string;
  signupDeadline: string;
  maxParticipants: number;
  participantCount: number;
  status: string;       // 'PENDING', 'ONGOING', 'ENDED', 'CANCELLED'
  publisherId: number;
  publisherName: string;
  createdAt: string;
  updatedAt: string;
}
```

### 5.6 消息通知模块接口

| 方法 | 路径 | 说明 | 是否需要登录 |
|------|------|------|-------------|
| GET | `/notifications` | 我的通知 | 是 |
| GET | `/notifications/unread-count` | 未读数量 | 是 |
| PUT | `/notifications/{id}/read` | 标记单条已读 | 是 |
| PUT | `/notifications/read-all` | 全部已读 | 是 |
| DELETE | `/notifications/{id}` | 删除通知 | 是 |

**类型定义**：
```typescript
interface Notification {
  id: number;
  receiverId: number;
  title: string;
  content: string;
  type: string;         // 'SYSTEM', 'TASK', 'ACTIVITY', 'MARKET'
  businessId?: number;
  businessType?: string;
  read: boolean;
  createdAt: string;
}
```

---

## 6. 状态管理设计

### 6.1 用户状态

```typescript
// stores/user.ts
interface UserState {
  isLoggedIn: boolean;
  token: string | null;
  userInfo: UserProfile | null;
}
```

### 6.2 通知状态

```typescript
// stores/notification.ts
interface NotificationState {
  unreadCount: number;
  notifications: Notification[];
}
```

---

## 7. 路由设计

### 7.1 路由配置

```typescript
const routes = [
  // 公共路由
  { path: '/login', component: LoginView, meta: { requiresAuth: false } },
  { path: '/register', component: RegisterView, meta: { requiresAuth: false } },
  
  // 需要登录的路由
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      { path: '', component: HomeView },
      // 二手交易
      { path: '/market', component: MarketListView },
      { path: '/market/:id', component: MarketDetailView },
      { path: '/market/publish', component: MarketPublishView },
      // 跑腿任务
      { path: '/tasks', component: TaskListView },
      { path: '/tasks/:id', component: TaskDetailView },
      { path: '/tasks/publish', component: TaskPublishView },
      // 活动社团
      { path: '/clubs', component: ClubListView },
      { path: '/clubs/:id', component: ClubDetailView },
      { path: '/activities', component: ActivityListView },
      { path: '/activities/:id', component: ActivityDetailView },
      // 消息通知
      { path: '/notifications', component: NotificationView },
      // 个人中心
      { path: '/profile', component: ProfileView },
    ]
  }
];
```

### 7.2 路由守卫

- **全局守卫**：检查登录状态，未登录跳转到登录页
- **权限守卫**：根据用户角色限制访问

---

## 8. 开发环境配置

### 8.1 环境变量

创建 `.env.development` 和 `.env.production` 文件：

```env
# .env.development
VITE_API_BASE_URL=http://localhost:9000/api
VITE_APP_NAME=校园生活服务平台
```

### 8.2 依赖安装

```bash
# 初始化项目
npm create vite@5 . -- --template vue-ts

# 安装依赖
npm install

# 安装Element Plus
npm install element-plus

# 安装Pinia
npm install pinia

# 安装Vue Router
npm install vue-router

# 安装Axios
npm install axios

# 安装SCSS支持
npm install -D sass
```

### 8.3 运行命令

```bash
# 开发模式
npm run dev

# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

---

## 9. 代码风格要求

### 9.1 ESLint配置

```json
{
  "extends": [
    "eslint:recommended",
    "plugin:vue/vue3-recommended",
    "@typescript-eslint/recommended"
  ],
  "rules": {
    "vue/multi-word-component-names": "off",
    "@typescript-eslint/no-explicit-any": "warn"
  }
}
```

### 9.2 代码规范

- **缩进**：使用2个空格
- **引号**：单引号优先
- **分号**：语句结尾必须加分号
- **变量命名**：小驼峰命名
- **函数命名**：小驼峰命名
- **组件命名**：大驼峰命名
- **文件命名**：组件文件大驼峰，其他文件小驼峰

### 9.3 注释规范

- **组件注释**：说明组件功能、props、events
- **函数注释**：说明功能、参数、返回值
- **复杂逻辑注释**：说明实现思路

---

## 10. 浏览器兼容性

| 浏览器 | 版本要求 |
|--------|----------|
| Chrome | >= 90 |
| Firefox | >= 88 |
| Safari | >= 14 |
| Edge | >= 90 |

---

## 11. 性能优化

### 11.1 代码层面

- **懒加载路由**：按需加载组件
- **虚拟滚动**：大数据列表使用虚拟滚动
- **图片懒加载**：使用Intersection Observer
- **防抖节流**：搜索、滚动等事件

### 11.2 构建层面

- **Tree Shaking**：去除未使用的代码
- **代码分割**：按模块分割打包
- **资源压缩**：JS/CSS/图片压缩

---

## 12. 安全考虑

- **XSS防护**：使用v-html时进行安全处理
- **CSRF防护**：后端配置Token验证
- **请求拦截**：统一处理401、403错误
- **敏感信息**：不存储密码等敏感数据到本地存储

---

## 附录：接口响应格式

```typescript
interface ApiResponse<T = any> {
  code: number;        // 状态码，200 表示成功
  message: string;     // 提示信息
  data: T;             // 数据
}

interface PageResponse<T = any> {
  code: number;
  message: string;
  data: {
    records: T[];
    total: number;
    pageNum: number;
    pageSize: number;
  };
}
```
