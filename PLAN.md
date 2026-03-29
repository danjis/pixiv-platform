# 管理端前端重构计划 — "幻画空间"设计语言

## 概述

将管理端从当前的 **Dark Gold 暗色主题** 重构为与用户端一致的 **"幻画空间"亮色设计语言**：白色背景、柔和阴影、圆润卡片、描边图标、药丸按钮。

**文件总量：** 16个文件，约5100行代码
**核心改动：** 全局样式 `style.css` (463行) + 布局 `AdminLayout.vue` (519行) + 14个视图页面

---

## 设计规范

| 属性 | 当前（Dark Gold） | 目标（幻画空间） |
|------|-------------------|------------------|
| 页面背景 | `#0a1120` 深蓝黑 | `#fff` 白色 |
| 卡片背景 | `#111d2e` 深色 | `#fff` 白色 |
| 侧边栏 | `#070c15` 纯黑 | `#fff` 白色，左侧边框分隔 |
| 主色调 | `#c9a84c` 金色 | `#0096FA` 蓝色 |
| 卡片圆角 | `12px` | `16px~20px` |
| 按钮圆角 | `8px` | `999px`（药丸） |
| 阴影 | 深色多层阴影 | `0 2px 16px rgba(0,0,0,0.04)` |
| 文字颜色 | `#ddd8ce` 浅色 | `#1a1a2e` 深色 |
| 品牌名 | "Pixiv Admin" | "幻画空间管理后台" |
| SVG图标 | 填充式 | 描边式 stroke-width 1.8 |
| 表情符号 | 📋👁❤⭐⚠️ | 替换为描边SVG |

---

## 执行批次

### Batch 0：全局样式 `style.css` + `App.vue`（最关键）
**文件：**
- `frontend/admin/src/style.css` (463行) — CSS变量 + Element Plus覆盖
- `frontend/admin/src/App.vue` (16行) — 注释

**改动：**
- 重写所有CSS变量：背景→白色、文字→深色、主色→蓝色、阴影→柔和
- 重写所有Element Plus组件覆盖样式（表格、按钮、输入框、对话框、标签等）
- 移除金色渐变，改为蓝色系
- 注释中 "Pixiv" → "幻画空间"

### Batch 1：布局 + 登录页
**文件：**
- `frontend/admin/src/layout/AdminLayout.vue` (519行) — 侧边栏+顶栏
- `frontend/admin/src/views/LoginView.vue` (298行) — 登录页

**改动：**
- AdminLayout：侧边栏改为白色背景+左边框、Logo改为"幻画空间"、顶栏改为白色、导航项改为蓝色高亮
- LoginView：移除暗色背景和发光球动画、改为亮色渐变背景、品牌名改为"幻画空间管理后台"、按钮改为药丸形

### Batch 2：仪表盘 + 用户管理 + 画师审核
**文件：**
- `DashboardView.vue` (476行) — 统计卡片+图表+日志
- `UsersView.vue` (199行) — 用户列表
- `ApplicationsView.vue` (628行) — 画师审核卡片

**改动：**
- Dashboard：统计卡片改为白色+柔和阴影、图表面板改为白色、移除金色发光效果、emoji→SVG
- Users：表格区域改为白色卡片、筛选栏圆角化
- Applications：审核卡片改为白色+20px圆角、按钮药丸化、emoji→SVG

### Batch 3：内容管理（作品+评论+比赛）
**文件：**
- `ArtworksView.vue` (211行) — 作品管理
- `CommentsView.vue` (105行) — 评论管理
- `ContestsView.vue` (331行) — 比赛管理

**改动：**
- 统一表格样式：白色背景、柔和阴影、圆角
- emoji统计图标→描边SVG
- 筛选栏药丸化
- 状态标签药丸化

### Batch 4：交易管理（约稿+支付+财务+优惠券）
**文件：**
- `CommissionsView.vue` (195行) — 约稿管理
- `PaymentsView.vue` (186行) — 支付管理
- `FinanceView.vue` (942行) — 财务管理（最大文件）
- `CouponsView.vue` (236行) — 优惠券管理

**改动：**
- 统一表格和筛选样式
- 财务页面的统计卡片和图表改为亮色
- emoji→SVG
- 按钮和标签药丸化

### Batch 5：系统管理（反馈+审计+会员）
**文件：**
- `FeedbackView.vue` (292行) — 反馈管理
- `AuditLogsView.vue` (140行) — 审计日志
- `MembershipView.vue` (335行) — 会员管理

**改动：**
- 统一表格样式
- 状态标签药丸化
- 筛选栏圆角化

---

## 关键原则

1. **不修改 `<script setup>`** — 只改CSS、SVG图标、文字内容
2. **不改模板逻辑** — v-if/v-for/@click/v-model等保持不变
3. **style.css 是核心** — 大部分Element Plus样式在这里全局覆盖，改好这个文件后各页面改动量会大幅减少
4. **保持一致性** — 与用户端的"幻画空间"设计语言完全统一
