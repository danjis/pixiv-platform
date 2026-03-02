# 类 Pixiv 艺术作品分享和约稿平台

## 项目简介

本系统是一个类似 Pixiv 的艺术作品分享和约稿平台，采用前后端分离的微服务架构。系统支持三种角色（普通用户、画师、管理员），提供作品发布、浏览、搜索、约稿、企划等核心功能，并集成智能打标服务作为核心特色功能。

## 核心特性

- **多角色权限管理**: 用户、画师、管理员三种角色，权限分离
- **智能打标**: 基于深度学习的图像识别，自动为作品生成标签
- **约稿系统**: 完整的约稿流程，从发起到完成的全生命周期管理
- **社交互动**: 点赞、收藏、评价、关注等社交功能
- **企划系统**: 用户可发起创作主题，画师参与投稿
- **前后端分离**: RESTful API 设计，支持多端访问

## 技术栈

### 后端技术栈

- **开发语言**: Java 21（最新 LTS 版本）
- **Web 框架**: Spring Boot 3.x
- **安全框架**: Spring Security + JWT
- **数据访问**: Spring Data JPA + Hibernate
- **数据库**: MySQL 8.0（主数据库）+ Redis（缓存）
- **文件存储**: 阿里云 OSS（对象存储服务）
- **消息队列**: RabbitMQ（异步任务处理）
- **API 文档**: Swagger（Springdoc OpenAPI）
- **构建工具**: Maven

### 前端技术栈

- **开发语言**: JavaScript（ES6+）
- **UI 框架**: React 18 + React Router
- **状态管理**: Redux Toolkit
- **UI 组件库**: Ant Design
- **HTTP 客户端**: Axios
- **构建工具**: Vite

### 智能打标服务

- **实现方式**: Python 微服务 + DeepDanbooru 预训练模型
- **Web 框架**: FastAPI（高性能 Python Web 框架）
- **模型选择**: DeepDanbooru（专门为二次元/动漫图片训练的开源模型）

## 项目结构

```
pixiv-platform/
├── backend/                    # Java 后端项目
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/pixiv/platform/
│   │       │       ├── controller/      # 控制器层
│   │       │       ├── service/         # 业务逻辑层
│   │       │       ├── repository/      # 数据访问层
│   │       │       ├── entity/          # 实体类
│   │       │       ├── dto/             # 数据传输对象
│   │       │       ├── config/          # 配置类
│   │       │       ├── exception/       # 异常处理
│   │       │       └── PixivPlatformApplication.java
│   │       └── resources/
│   │           ├── application.yml
│   │           └── application-dev.yml
│   ├── pom.xml
│   └── README.md
│
├── frontend/                   # React 前端项目
│   ├── user/                   # 用户端前端
│   │   ├── src/
│   │   │   ├── components/     # 可复用组件
│   │   │   ├── pages/          # 页面组件
│   │   │   ├── services/       # API 服务
│   │   │   ├── store/          # Redux 状态管理
│   │   │   ├── utils/          # 工具函数
│   │   │   ├── App.jsx
│   │   │   └── main.jsx
│   │   ├── package.json
│   │   ├── vite.config.js
│   │   └── .env
│   │
│   └── admin/                  # 管理员后台前端
│       ├── src/
│       │   ├── components/
│       │   ├── pages/
│       │   ├── services/
│       │   ├── utils/
│       │   ├── App.jsx
│       │   └── main.jsx
│       ├── package.json
│       ├── vite.config.js
│       └── .env
│
├── ai-service/                 # Python AI 智能打标服务
│   ├── main.py                 # FastAPI 应用入口
│   ├── deepdanbooru.py         # 模型包装类
│   ├── requirements.txt        # Python 依赖
│   └── models/                 # DeepDanbooru 模型文件
│
└── README.md                   # 项目总体说明
```

## 开发架构

### 本地开发 + 远程服务混合架构

- **本地运行**：
  - Java 后端（端口 8080）
  - Python AI 服务（端口 8000）
  - React 用户前端（端口 3000）
  - React 管理员后台（端口 3001）

- **远程服务器**（47.120.61.152）：
  - MySQL 8.0（端口 3306）
  - Redis 7（端口 6379）
  - RabbitMQ 3（端口 5672/15672）
  - Nacos 2.3.0（端口 8848）

## 快速开始

### 前置要求

- Java 21 或更高版本
- Node.js 18 或更高版本
- Python 3.10 或更高版本
- Maven 3.8 或更高版本
- Git

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 前端启动

**用户端前端：**
```bash
cd frontend/user
npm install
npm run dev
```

用户端前端将在 http://localhost:3000 启动

**管理员后台：**
```bash
cd frontend/admin
npm install
npm run dev
```

管理员后台将在 http://localhost:3001 启动

### AI 服务启动

```bash
cd ai-service
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

AI 服务将在 http://localhost:8000 启动

## 远程服务连接信息

- **服务器 IP**: 47.120.61.152
- **MySQL**: 端口 3306
  - 用户: pixiv_user
  - 密码: pixiv123456
  - 数据库: pixiv_platform
- **Redis**: 端口 6379
  - 密码: redis123456
- **RabbitMQ**: 端口 5672/15672
  - 用户: admin
  - 密码: rabbitmq123456
- **Nacos**: 端口 8848
  - 命名空间: dev
  - 用户: nacos
  - 密码: nacos

## API 文档

启动后端服务后，访问以下地址查看 API 文档：

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 开发指南

详细的开发指南请参考各子项目的 README.md 文件：

- [后端开发指南](backend/README.md)
- [前端开发指南](frontend/user/README.md)
- [管理员后台开发指南](frontend/admin/README.md)
- [AI 服务开发指南](ai-service/README.md)

## 功能模块

### 用户功能
- 用户注册和登录
- 个人信息管理
- 作品浏览和搜索
- 作品点赞、收藏、评价
- 关注画师
- 发起约稿
- 发布企划

### 画师功能
- 画师申请
- 作品发布和管理
- 智能打标（自动生成标签）
- 接受约稿订单
- 参与企划

### 管理员功能
- 画师申请审核
- 内容管理
- 违规作品删除
- 审计日志查看

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue
- 发送邮件至项目维护者

## 致谢

- [DeepDanbooru](https://github.com/KichangKim/DeepDanbooru) - 智能打标模型
- [Spring Boot](https://spring.io/projects/spring-boot) - 后端框架
- [React](https://reactjs.org/) - 前端框架
- [Ant Design](https://ant.design/) - UI 组件库
