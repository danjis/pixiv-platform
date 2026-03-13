# Pixiv Platform — 类 Pixiv 艺术作品分享与约稿平台

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=springboot" alt="Spring Boot 3.2"/>
  <img src="https://img.shields.io/badge/Spring%20Cloud-2023.0-blue?logo=spring" alt="Spring Cloud"/>
  <img src="https://img.shields.io/badge/Vue-3-green?logo=vuedotjs" alt="Vue 3"/>
  <img src="https://img.shields.io/badge/Python-3.10-blue?logo=python" alt="Python 3.10"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql" alt="MySQL 8.0"/>
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker" alt="Docker"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="MIT License"/>
</p>

一个基于微服务架构的二次元艺术作品分享与约稿平台，支持作品发布、智能打标、社交互动、约稿交易、企划比赛等功能。

## 预览

| 用户首页 | 管理后台 |
|---------|---------|
| ![用户端](https://img.shields.io/badge/用户端-Vue3%20%2B%20Element%20Plus-42b883) | ![管理端](https://img.shields.io/badge/管理端-Vue3%20%2B%20ECharts-409EFF) |

## 项目概述

本系统是一个类似 Pixiv 的艺术作品分享和约稿平台，采用 **前后端分离 + 微服务架构**。系统支持三种角色（**普通用户、画师、管理员**），提供完整的作品发布浏览、智能 AI 打标、约稿交易、企划比赛等功能。

### 核心特性

- 🎨 **作品系统** — 发布、浏览、搜索、点赞、收藏、评论，支持多图上传
- 🤖 **AI 智能打标** — 基于 DeepDanbooru 深度学习模型，自动识别二次元图片内容并生成标签
- 📝 **约稿系统** — 完整的约稿生命周期管理，从发起、报价、支付到交付
- 🏆 **企划比赛** — 用户可发起创作主题，画师参与投稿和投票
- 💬 **即时通讯** — 基于 WebSocket（STOMP + SockJS）实现私信实时推送与系统通知
- 💰 **支付系统** — 支付宝沙箱支付、钱包管理、提现、优惠券
- 👥 **社交互动** — 关注动态、排行榜、浏览历史
- 🎫 **会员体系** — VIP 会员、签到积分、月度优惠券

## 技术栈

### 后端

| 技术 | 说明 |
|------|------|
| Java 21 | 开发语言（LTS） |
| Spring Boot 3.2 | 微服务基础框架 |
| Spring Cloud 2023 | 微服务治理 |
| Spring Cloud Gateway | API 网关 |
| Nacos 2.3 | 服务注册与配置中心 |
| Spring Data JPA | 数据持久层 |
| Spring Security + JWT | 认证与授权 |
| MySQL 8.0 | 关系型数据库 |
| Redis 7 | 缓存与会话管理 |
| RabbitMQ 3 | 消息队列 |
| Alibaba Druid | 数据库连接池 |
| 阿里云 OSS | 文件对象存储 |
| 支付宝沙箱 | 支付集成 |
| Swagger / OpenAPI | API 文档 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3 | 前端框架 |
| Vite 5 | 构建工具 |
| Pinia | 状态管理 |
| Vue Router 4 | 路由管理 |
| Element Plus | UI 组件库 |
| Axios | HTTP 客户端 |
| STOMP.js + SockJS | WebSocket 实时通讯 |
| ECharts | 数据可视化（管理端） |

### AI 智能打标

| 技术 | 说明 |
|------|------|
| Python 3.10 | 开发语言 |
| FastAPI | Web 框架 |
| TensorFlow 2.14 | 深度学习框架 |
| DeepDanbooru | 二次元图像识别模型 |
| Pillow | 图像处理 |

### 基础设施

| 技术 | 说明 |
|------|------|
| Docker & Docker Compose | 容器化部署 |
| Nginx | 反向代理与静态资源 |
| Maven | 后端构建工具 |
| Git | 版本控制 |

## 系统架构

```
                              ┌──────────────┐
                              │    Nginx     │
                              │  (反向代理)   │
                              └──────┬───────┘
                                     │
                    ┌────────────────┼────────────────┐
                    │                │                │
             ┌──────┴──────┐  ┌─────┴──────┐  ┌─────┴──────┐
             │  用户前端    │  │  管理后台   │  │  API 请求   │
             │  (Vue 3)    │  │  (Vue 3)   │  │  /api/*    │
             └─────────────┘  └────────────┘  └─────┬──────┘
                                                     │
                                              ┌──────┴───────┐
                                              │   Gateway    │
                                              │  (网关:8080)  │
                                              └──────┬───────┘
                                                     │
                         ┌──────────┬──────────┬─────┴────┬──────────┐
                         │          │          │          │          │
                   ┌─────┴────┐┌───┴────┐┌────┴───┐┌────┴───┐┌────┴───┐
                   │  用户服务 ││作品服务 ││约稿服务 ││通知服务 ││文件服务 │
                   │  :8081   ││ :8082  ││ :8083  ││ :8084  ││ :8085  │
                   └────┬─────┘└───┬────┘└────┬───┘└────┬───┘└────┬───┘
                        │          │          │         │         │
                   ┌────┴──────────┴──────────┴─────────┘         │
                   │                                              │
            ┌──────┴──────┐  ┌──────────┐  ┌──────────┐  ┌──────┴──────┐
            │   MySQL 8   │  │  Redis 7  │  │ RabbitMQ │  │ 阿里云 OSS  │
            └─────────────┘  └──────────┘  └──────────┘  └─────────────┘
                                    │
                             ┌──────┴──────┐
                             │  AI 打标服务  │
                             │  (Python)   │
                             │   :8000     │
                             └─────────────┘
```

## 项目结构

```
pixiv-platform/
├── common/                      # 公共模块（DTO、异常、工具类）
├── gateway-service/             # API 网关（路由、鉴权、限流）
├── user-service/                # 用户服务（认证、个人中心、钱包）
├── artwork-service/             # 作品服务（发布、浏览、搜索、互动）
├── commission-service/          # 约稿服务（约稿、支付、优惠券）
├── notification-service/        # 通知服务（系统通知、反馈）
├── file-service/                # 文件服务（阿里云 OSS 上传）
├── ai-service/                  # AI 打标服务（Python + DeepDanbooru）
├── frontend/
│   ├── user/                    # 用户端前端（Vue 3）
│   └── admin/                   # 管理后台前端（Vue 3）
└── pom.xml                      # Maven 父 POM
```

## 微服务说明

| 服务 | 端口 | 说明 | 主要功能 |
|------|------|------|---------|
| **gateway-service** | 8080 | API 网关 | 路由转发、JWT 鉴权、跨域处理、限流 |
| **user-service** | 8081 | 用户服务 | 注册登录、个人中心、画师申请、钱包提现、会员签到 |
| **artwork-service** | 8082 | 作品服务 | 作品 CRUD、点赞收藏评论、AI 打标、排行榜、企划比赛 |
| **commission-service** | 8083 | 约稿服务 | 约稿方案、订单管理、支付宝支付、优惠券、私信 |
| **notification-service** | 8084 | 通知服务 | 系统通知、WebSocket 实时推送（通知 + 私信）、未读计数、用户反馈 |
| **file-service** | 8085 | 文件服务 | 图片上传、缩略图生成、OSS 存储 |
| **ai-service** | 8000 | AI 打标 | DeepDanbooru 图像识别、标签翻译 |

## 功能模块

### 用户端（30 个页面）

| 模块 | 功能 |
|------|------|
| 🏠 首页 | 作品瀑布流、推荐内容 |
| 🔐 认证 | 注册、登录、邮箱验证 |
| 🖼️ 作品 | 浏览、搜索、筛选、详情 |
| ❤️ 互动 | 点赞、收藏、评论 |
| 👤 个人 | 个人主页、资料编辑、收藏夹 |
| 🎨 画师 | 画师主页、作品集、关注 |
| 🖌️ 工作台 | 发布作品、管理作品、收入统计 |
| 📝 约稿 | 发起约稿、约稿方案、订单管理 |
| 💬 聊天 | WebSocket 实时私信、约稿沟通 |
| 🏆 比赛 | 企划比赛、投稿、投票 |
| 📊 排行 | 日榜、周榜、月榜 |
| 🎫 会员 | VIP 开通、签到领积分、优惠券 |
| 🛒 订单 | 订单列表、支付、支付结果 |
| 🔔 通知 | 系统通知、消息提醒 |
| 📜 历史 | 浏览记录 |
| 🎟️ 优惠 | 优惠券领取、使用 |

### 管理后台（14 个页面）

| 模块 | 功能 |
|------|------|
| 📊 仪表盘 | 数据概览、图表统计 |
| 👥 用户管理 | 用户列表、禁用启用 |
| ✅ 申请审核 | 画师申请审批 |
| 🖼️ 作品管理 | 作品审核、违规删除 |
| 💬 评论管理 | 评论审核、删除 |
| 📝 约稿管理 | 约稿订单监管 |
| 💰 财务管理 | 提现审批、收入统计 |
| 💳 支付管理 | 支付订单查询 |
| 🏆 比赛管理 | 企划创建、管理 |
| 🎫 会员管理 | 会员权益配置 |
| 🎟️ 优惠券 | 优惠券发放管理 |
| 💡 反馈管理 | 用户反馈处理 |
| 📋 审计日志 | 操作日志查询 |

## 快速开始

### 环境要求

- Java 21+
- Maven 3.8+
- Node.js 18+
- Python 3.10+
- Docker & Docker Compose
- MySQL 8.0、Redis 7、RabbitMQ 3、Nacos 2.3

### 1. 克隆项目

```bash
git clone https://github.com/danjis/pixiv-platform.git
cd pixiv-platform
```

### 2. 启动中间件

使用 Docker Compose 启动所有中间件（MySQL、Redis、RabbitMQ、Nacos）：

```bash
# 在项目父目录 bs-3/ 下
docker compose -f docker-compose.dev.yml up -d mysql redis rabbitmq nacos
```

### 3. 后端启动

```bash
# 构建公共模块
mvn clean install -DskipTests

# 依次启动各服务（或在 IDE 中启动）
# Gateway → User → Artwork → Commission → Notification → File
```

### 4. 前端启动

```bash
# 用户端
cd frontend/user
npm install
npm run dev    # http://localhost:3000

# 管理后台
cd frontend/admin
npm install
npm run dev    # http://localhost:3001
```

### 5. AI 服务启动

```bash
cd ai-service
pip install -r requirements.txt
# 下载 DeepDanbooru 模型到 models/deepdanbooru/ 目录
uvicorn main:app --host 0.0.0.0 --port 8000
```

> **注意**：AI 服务需要下载 DeepDanbooru 预训练模型（约 617MB），请从 [DeepDanbooru Releases](https://github.com/KichangKim/DeepDanbooru/releases) 下载并解压到 `ai-service/models/deepdanbooru/` 目录。

## 数据库设计

系统采用分库设计，共 4 个业务数据库：

| 数据库 | 说明 | 主要表 |
|-------|------|--------|
| `user_db` | 用户数据 | users, artists, artist_wallets, follows, user_memberships, audit_logs 等 |
| `artwork_db` | 作品数据 | artworks, artwork_images, tags, comments, likes, favorites, contests 等 |
| `commission_db` | 约稿数据 | commissions, commission_plans, payment_orders, coupons, conversations 等 |
| `notification_db` | 通知数据 | notifications, feedbacks |

## 项目统计

| 指标 | 数量 |
|------|------|
| 微服务数 | 7 个（6 Java + 1 Python） |
| Controller | 23 个 |
| Service | 27 个 |
| Entity | 34 个 |
| 用户端页面 | 30 个 |
| 管理端页面 | 14 个 |
| API 接口 | 100+ 个 |
| 数据库表 | 30+ 张 |

## 部署

项目支持 Docker Compose 一键部署，所有服务（中间件 + 微服务 + 前端）均已容器化：

```bash
# 全量部署
docker compose -f docker-compose.dev.yml up -d
```

## License

[MIT](LICENSE)
