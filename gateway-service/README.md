# API 网关服务 (Gateway Service)

## 概述

API 网关服务是整个微服务架构的统一入口，负责路由转发、认证鉴权、限流保护、跨域处理等功能。

## 功能特性

- **统一入口**：所有前端请求通过网关访问后端服务
- **路由转发**：根据请求路径自动转发到对应的微服务
- **JWT 认证**：验证用户身份，保护需要认证的接口
- **负载均衡**：自动选择健康的服务实例
- **CORS 跨域**：支持前端应用跨域访问
- **请求日志**：记录所有请求的详细信息
- **全局异常处理**：统一处理异常并返回标准格式的错误响应
- **健康检查**：提供健康检查端点，用于服务监控

## 技术栈

- Spring Boot 3.2.2
- Spring Cloud Gateway
- Nacos Discovery & Config
- Spring Cloud LoadBalancer
- JWT (jjwt 0.12.3)
- Redis (会话存储)

## 端口配置

- 服务端口：8080
- Nacos 服务器：47.120.61.152:8848

## 路由配置

### 用户服务路由
- `/api/auth/**` - 认证相关接口（登录、注册、刷新令牌）
- `/api/users/**` - 用户管理接口
- `/api/follows/**` - 关注功能接口

### 作品服务路由
- `/api/artworks/**` - 作品管理接口
- `/api/tags/**` - 标签管理接口

### 约稿服务路由
- `/api/commissions/**` - 约稿订单接口
- `/api/projects/**` - 企划管理接口

### 通知服务路由
- `/api/notifications/**` - 通知接口

### 文件服务路由
- `/api/files/**` - 文件上传接口

## 认证机制

### JWT 令牌验证

网关会验证所有请求的 JWT 令牌（白名单路径除外），并将用户信息添加到请求头中传递给下游服务：

- `X-User-Id`：用户 ID
- `X-Username`：用户名
- `X-User-Role`：用户角色

### 白名单路径

以下路径不需要认证即可访问：

- `/api/auth/login` - 登录
- `/api/auth/register` - 注册
- `/api/auth/refresh` - 刷新令牌
- `/api/artworks` - 作品列表（游客可访问）
- `/api/artworks/*/` - 作品详情（游客可访问）
- `/api/tags/**` - 标签（游客可访问）
- `/actuator/**` - 健康检查

## 启动方式

### 前置条件

1. 确保 Nacos 服务器正常运行（47.120.61.152:8848）
2. 确保 Redis 服务器正常运行（47.120.61.152:6379）
3. 确保已在 Nacos 中创建 `dev` 命名空间
4. 确保已在 Nacos 中上传共享配置（shared-redis.yml）

### 启动命令

```bash
# 在 gateway-service 目录下执行
mvn spring-boot:run

# 或者先编译再运行
mvn clean package
java -jar target/gateway-service-1.0.0-SNAPSHOT.jar
```

### 验证启动

访问健康检查端点：
```bash
curl http://localhost:8080/actuator/health
```

查看网关路由信息：
```bash
curl http://localhost:8080/actuator/gateway/routes
```

## 配置说明

### application.yml

主配置文件，包含服务名称、Nacos 配置、日志配置等。

### application-dev.yml

开发环境配置，包含网关路由、CORS 配置、Redis 配置、JWT 配置等。

### bootstrap.yml

引导配置文件，在 application.yml 之前加载，用于配置 Nacos 等基础设施。

## 过滤器说明

### RequestLogFilter

- 优先级：最高（Ordered.HIGHEST_PRECEDENCE）
- 功能：记录所有请求的基本信息和处理时间

### AuthenticationFilter

- 优先级：-100
- 功能：验证 JWT 令牌，将用户信息添加到请求头

## 异常处理

全局异常处理器会捕获所有异常并返回统一格式的错误响应：

```json
{
  "code": 500,
  "message": "错误信息",
  "data": null
}
```

## 开发注意事项

1. **JWT 密钥**：生产环境必须修改 `jwt.secret` 配置
2. **CORS 配置**：生产环境应该配置具体的前端域名，而不是允许所有来源
3. **白名单路径**：根据实际需求调整白名单路径
4. **日志级别**：生产环境建议将日志级别调整为 INFO 或 WARN

## 监控和运维

### 健康检查

```bash
# 检查服务健康状态
curl http://localhost:8080/actuator/health

# 查看详细健康信息
curl http://localhost:8080/actuator/health/gatewayHealthIndicator
```

### 查看路由信息

```bash
# 查看所有路由
curl http://localhost:8080/actuator/gateway/routes

# 刷新路由
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

## 故障排查

### 服务无法启动

1. 检查 Nacos 服务器是否正常运行
2. 检查 Redis 服务器是否正常运行
3. 检查端口 8080 是否被占用
4. 查看启动日志中的错误信息

### 路由转发失败

1. 检查目标微服务是否已注册到 Nacos
2. 检查路由配置是否正确
3. 查看网关日志中的错误信息

### JWT 验证失败

1. 检查 JWT 密钥配置是否正确
2. 检查令牌是否已过期
3. 检查 Authorization 请求头格式是否正确（Bearer token）

## 后续优化

- [ ] 添加限流功能（使用 Sentinel）
- [ ] 添加熔断降级功能
- [ ] 添加链路追踪（使用 Zipkin）
- [ ] 添加 API 文档聚合功能
- [ ] 优化日志输出格式
- [ ] 添加更多的健康检查指标
