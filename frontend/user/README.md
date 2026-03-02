# Pixiv 平台 - 用户端前端

这是 Pixiv 艺术作品分享和约稿平台的用户端前端应用。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP 客户端**: Axios

## 项目结构

```
src/
├── api/              # API 接口模块
│   ├── auth.js       # 认证相关接口
│   ├── user.js       # 用户相关接口
│   └── artwork.js    # 作品相关接口
├── assets/           # 静态资源
├── components/       # 公共组件
├── router/           # 路由配置
│   └── index.js      # 路由定义和守卫
├── stores/           # Pinia 状态管理
│   └── user.js       # 用户状态
├── utils/            # 工具函数
│   ├── request.js    # Axios 配置和拦截器
│   ├── format.js     # 格式化工具
│   └── validate.js   # 验证工具
├── views/            # 页面组件
│   ├── auth/         # 认证相关页面
│   ├── artwork/      # 作品相关页面
│   └── user/         # 用户相关页面
├── App.vue           # 根组件
└── main.js           # 应用入口
```

## 开发指南

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

应用将在 http://localhost:3000 启动

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 环境配置

在项目根目录创建 `.env` 文件：

```env
# API 基础 URL
VITE_API_BASE_URL=http://localhost:8080
```

## 核心功能

### 已实现

- ✅ Vue 3 项目基础架构
- ✅ Element Plus UI 组件库集成
- ✅ Pinia 状态管理配置
- ✅ Vue Router 路由配置
- ✅ Axios HTTP 客户端配置
- ✅ JWT 令牌认证拦截器
- ✅ 用户登录/注册页面
- ✅ 路由守卫（认证检查）

### 待实现

- ⏳ 作品浏览和搜索
- ⏳ 作品详情页面
- ⏳ 用户个人主页
- ⏳ 画师申请功能
- ⏳ 约稿功能
- ⏳ 企划功能
- ⏳ 社交互动（点赞、收藏、评价、关注）

## API 接口

所有 API 请求都会自动添加 JWT 令牌到请求头：

```javascript
Authorization: Bearer <token>
```

### 认证接口

- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/refresh` - 刷新令牌
- `POST /api/auth/logout` - 用户登出

### 用户接口

- `GET /api/users/me` - 获取当前用户信息
- `PUT /api/users/me` - 更新用户信息
- `GET /api/users/{userId}` - 获取用户主页

## 注意事项

1. 确保后端服务已启动（默认端口 8080）
2. 首次运行需要安装依赖：`npm install`
3. 开发环境使用 Vite 代理转发 API 请求
4. 生产环境需要配置 Nginx 反向代理

## 开发规范

- 使用 Vue 3 Composition API
- 组件使用 `<script setup>` 语法
- 状态管理使用 Pinia
- 样式使用 scoped CSS
- 代码注释使用中文
