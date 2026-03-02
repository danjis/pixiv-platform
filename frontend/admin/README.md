# 管理员后台前端

基于 Vue 3 + Element Plus 的管理员后台系统。

## 技术栈

- **Vue 3**: 渐进式 JavaScript 框架
- **Vite**: 下一代前端构建工具
- **Element Plus**: Vue 3 UI 组件库
- **Vue Router**: 官方路由管理器
- **Pinia**: Vue 3 状态管理库
- **Axios**: HTTP 客户端

## 项目结构

```
src/
├── api/              # API 接口模块
│   ├── auth.js       # 认证相关接口
│   ├── artist.js     # 画师审核接口
│   └── artwork.js    # 作品管理接口
├── assets/           # 静态资源
├── components/       # 公共组件
├── router/           # 路由配置
│   └── index.js      # 路由定义和守卫
├── stores/           # Pinia 状态管理
│   └── admin.js      # 管理员状态
├── utils/            # 工具函数
│   └── request.js    # Axios 封装和拦截器
├── views/            # 页面组件
│   ├── LoginView.vue      # 登录页
│   └── DashboardView.vue  # 仪表板
├── App.vue           # 根组件
└── main.js           # 应用入口
```

## 功能特性

### 已实现功能

- ✅ 管理员登录认证
- ✅ JWT Token 管理
- ✅ 路由守卫（自动跳转）
- ✅ Axios 请求/响应拦截器
- ✅ 统一错误处理
- ✅ 响应式侧边栏布局

### 待实现功能

- 画师申请审核页面
- 作品管理页面
- 审计日志页面
- 数据统计仪表板

## 开发指南

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问地址：http://localhost:3001

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 环境配置

项目使用 `.env` 文件配置环境变量：

```env
# API 基础 URL
VITE_API_BASE_URL=http://localhost:8080
```

## API 接口说明

### 认证接口

- `POST /api/admin/auth/login` - 管理员登录
- `POST /api/admin/auth/logout` - 管理员登出
- `GET /api/admin/me` - 获取当前管理员信息

### 画师审核接口

- `GET /api/admin/artist-applications` - 获取待审核申请列表
- `GET /api/admin/artist-applications/:id` - 获取申请详情
- `POST /api/admin/artist-applications/:id/approve` - 批准申请
- `POST /api/admin/artist-applications/:id/reject` - 拒绝申请

### 作品管理接口

- `GET /api/admin/artworks` - 获取作品列表
- `DELETE /api/admin/artworks/:id` - 删除违规作品
- `GET /api/admin/audit-logs` - 获取审计日志

## 注意事项

1. **Token 管理**: JWT Token 存储在 localStorage 中，key 为 `admin_token`
2. **路由守卫**: 所有需要认证的页面都会自动检查 token，未登录会跳转到登录页
3. **错误处理**: 401 错误会自动清除 token 并跳转到登录页
4. **请求拦截**: 所有请求会自动添加 Authorization 头部

## 开发规范

- 使用 Composition API（`<script setup>`）
- 组件命名使用 PascalCase
- 文件命名使用 kebab-case
- API 函数添加 JSDoc 注释
- 使用 Element Plus 组件库

