package com.pixiv.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置
 * 
 * 配置所有微服务的路由规则，将请求转发到对应的服务
 * 
 * @author Pixiv Platform Team
 */
@Configuration
public class GatewayRoutesConfig {

        /**
         * 配置路由规则
         * 
         * @param builder 路由构建器
         * @return 路由定位器
         */
        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
                return builder.routes()
                                // ========== 用户服务路由 ==========
                                // 认证相关接口（登录、注册、刷新令牌等）
                                .route("user-service-auth", r -> r
                                                .path("/api/auth/**")
                                                .uri("lb://user-service")) // lb:// 表示使用负载均衡

                                // 验证码接口
                                .route("user-service-captcha", r -> r
                                                .path("/api/captcha/**")
                                                .uri("lb://user-service"))

                                // 用户管理接口
                                .route("user-service-users", r -> r
                                                .path("/api/users/**")
                                                .uri("lb://user-service"))

                                // 关注功能接口
                                .route("user-service-follows", r -> r
                                                .path("/api/follows/**")
                                                .uri("lb://user-service"))

                                // 会员接口
                                .route("user-service-membership", r -> r
                                                .path("/api/membership/**")
                                                .uri("lb://user-service"))

                                // 签到/积分接口
                                .route("user-service-checkin", r -> r
                                                .path("/api/checkin/**")
                                                .uri("lb://user-service"))

                                // ========== 管理员路由 ==========
                                // 管理员-作品管理接口（转发到 artwork-service）
                                .route("admin-artworks", r -> r
                                                .path("/api/admin/artworks/**")
                                                .uri("lb://artwork-service"))

                                // 管理员-评论管理接口（转发到 artwork-service）
                                .route("admin-comments", r -> r
                                                .path("/api/admin/comments/**")
                                                .uri("lb://artwork-service"))

                                // 管理员-比赛管理接口（转发到 artwork-service）
                                .route("admin-contests", r -> r
                                                .path("/api/admin/contests/**")
                                                .uri("lb://artwork-service"))

                                // 管理员-其他管理接口（转发到 user-service）
                                .route("admin-service", r -> r
                                                .path("/api/admin/**")
                                                .uri("lb://user-service"))

                                // ========== 作品服务路由 ==========
                                // 作品管理接口
                                .route("artwork-service-artworks", r -> r
                                                .path("/api/artworks/**")
                                                .uri("lb://artwork-service"))

                                // 标签管理接口
                                .route("artwork-service-tags", r -> r
                                                .path("/api/tags/**")
                                                .uri("lb://artwork-service"))

                                // 浏览记录接口
                                .route("artwork-service-browsing-history", r -> r
                                                .path("/api/browsing-history/**")
                                                .uri("lb://artwork-service"))

                                // 比赛接口
                                .route("artwork-service-contests", r -> r
                                                .path("/api/contests/**")
                                                .uri("lb://artwork-service"))

                                // ========== 约稿服务路由 ==========
                                // 约稿订单接口
                                .route("commission-service-commissions", r -> r
                                                .path("/api/commissions/**")
                                                .uri("lb://commission-service"))

                                // 支付接口
                                .route("commission-service-payments", r -> r
                                                .path("/api/payments/**")
                                                .uri("lb://commission-service"))

                                // 约稿方案接口
                                .route("commission-service-plans", r -> r
                                                .path("/api/commission-plans/**")
                                                .uri("lb://commission-service"))

                                // 私信/对话接口
                                .route("commission-service-conversations", r -> r
                                                .path("/api/conversations/**")
                                                .uri("lb://commission-service"))

                                // 优惠券接口
                                .route("commission-service-coupons", r -> r
                                                .path("/api/coupons/**")
                                                .uri("lb://commission-service"))

                                // ========== 通知服务路由 ==========
                                // 通知接口
                                .route("notification-service-notifications", r -> r
                                                .path("/api/notifications/**")
                                                .uri("lb://notification-service"))

                                // 反馈/客服接口
                                .route("notification-service-feedback", r -> r
                                                .path("/api/feedback/**")
                                                .uri("lb://notification-service"))

                                // WebSocket 实时通知（STOMP over SockJS）
                                .route("notification-service-ws", r -> r
                                                .path("/ws/notifications/**")
                                                .uri("lb:ws://notification-service"))

                                // ========== 文件服务路由 ==========
                                // 文件上传接口
                                .route("file-service-files", r -> r
                                                .path("/api/files/**")
                                                .uri("lb://file-service"))

                                // ========== API 文档聚合路由 ==========
                                .route("user-service-api-docs", r -> r
                                                .path("/api/users/v3/api-docs")
                                                .filters(f -> f.rewritePath("/api/users/v3/api-docs", "/v3/api-docs"))
                                                .uri("lb://user-service"))

                                .route("artwork-service-api-docs", r -> r
                                                .path("/api/artworks/v3/api-docs")
                                                .filters(f -> f.rewritePath("/api/artworks/v3/api-docs",
                                                                "/v3/api-docs"))
                                                .uri("lb://artwork-service"))

                                .route("commission-service-api-docs", r -> r
                                                .path("/api/commissions/v3/api-docs")
                                                .filters(f -> f.rewritePath("/api/commissions/v3/api-docs",
                                                                "/v3/api-docs"))
                                                .uri("lb://commission-service"))

                                .route("notification-service-api-docs", r -> r
                                                .path("/api/notifications/v3/api-docs")
                                                .filters(f -> f.rewritePath("/api/notifications/v3/api-docs",
                                                                "/v3/api-docs"))
                                                .uri("lb://notification-service"))

                                .route("file-service-api-docs", r -> r
                                                .path("/api/files/v3/api-docs")
                                                .filters(f -> f.rewritePath("/api/files/v3/api-docs", "/v3/api-docs"))
                                                .uri("lb://file-service"))

                                .build();
        }

}
