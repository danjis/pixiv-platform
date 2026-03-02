package com.pixiv.artwork.feign;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.pixiv.common.dto.ArtistDTO;
import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import java.util.Collections;
import java.util.List;

/**
 * 用户服务 Feign 客户端降级处理工厂
 * 
 * 当用户服务不可用或调用失败时，提供降级处理逻辑
 * 
 * 降级场景：
 * 1. 服务不可用（连接超时、服务宕机）
 * 2. 调用超时（响应时间过长）
 * 3. 触发 Sentinel 熔断规则（慢调用比例、异常比例、异常数）
 * 4. 触发 Sentinel 限流规则（QPS 超过阈值）
 * 
 * @author Pixiv Platform Team
 */
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClientFallbackFactory.class);

    @Override
    public UserServiceClient create(Throwable cause) {
        // 判断是否为 Sentinel 熔断或限流
        boolean isSentinelBlock = cause instanceof BlockException;
        String errorType = isSentinelBlock ? "Sentinel 熔断/限流" : "服务调用异常";

        return new UserServiceClient() {
            @Override
            public Result<UserDTO> getUserById(Long userId) {
                log.error("[降级处理] 调用用户服务失败 - 获取用户信息, 用户 ID: {}, 错误类型: {}, 错误信息: {}",
                        userId,
                        errorType,
                        cause.getMessage());

                if (isSentinelBlock) {
                    log.warn("[熔断降级] 用户服务触发 Sentinel 保护机制，用户 ID: {}", userId);
                    return Result.error(503, "用户服务繁忙，请稍后重试");
                }

                return Result.error(503, "用户服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<ArtistDTO> getArtistByUserId(Long userId) {
                log.error("[降级处理] 调用用户服务失败 - 获取画师信息, 用户 ID: {}, 错误类型: {}, 错误信息: {}",
                        userId,
                        errorType,
                        cause.getMessage());

                if (isSentinelBlock) {
                    log.warn("[熔断降级] 用户服务触发 Sentinel 保护机制，用户 ID: {}", userId);
                    return Result.error(503, "用户服务繁忙，请稍后重试");
                }

                return Result.error(503, "用户服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<Boolean> isArtist(Long userId) {
                log.error("[降级处理] 调用用户服务失败 - 验证画师身份, 用户 ID: {}, 错误类型: {}, 错误信息: {}",
                        userId,
                        errorType,
                        cause.getMessage());

                if (isSentinelBlock) {
                    log.warn("[熔断降级] 用户服务触发 Sentinel 保护机制，用户 ID: {}", userId);
                    return Result.error(503, "用户服务繁忙，请稍后重试");
                }

                // 降级策略：返回错误，不允许发布作品（安全策略）
                return Result.error(503, "用户服务暂时不可用，无法验证画师身份");
            }

            @Override
            public Result<List<Long>> getFollowingIds(Long userId) {
                log.error("[降级处理] 调用用户服务失败 - 获取关注ID列表, 用户 ID: {}, 错误类型: {}, 错误信息: {}",
                        userId,
                        errorType,
                        cause.getMessage());

                if (isSentinelBlock) {
                    log.warn("[熔断降级] 用户服务触发 Sentinel 保护机制，用户 ID: {}", userId);
                    return Result.error(503, "用户服务繁忙，请稍后重试");
                }

                // 降级策略：返回空列表
                return Result.success(Collections.emptyList());
            }

            @Override
            public Result<List<Long>> getFollowerIds(Long userId) {
                log.error("[降级处理] 调用用户服务失败 - 获取粉丝ID列表, 用户 ID: {}, 错误: {}",
                        userId, cause.getMessage());
                return Result.success(Collections.emptyList());
            }

            @Override
            public Result<List<UserDTO>> getUsersByIds(List<Long> userIds) {
                log.error("[降级处理] 调用用户服务失败 - 批量获取用户信息, 用户 ID 数量: {}, 错误类型: {}, 错误信息: {}",
                        userIds != null ? userIds.size() : 0,
                        errorType,
                        cause.getMessage());

                // 降级策略：返回错误，使调用方能够检测到失败并执行降级逻辑
                return Result.error(503, "用户服务批量查询不可用");
            }
        };
    }
}
