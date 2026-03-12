package com.pixiv.commission.feign;

import com.pixiv.common.dto.ArtistDTO;
import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 用户服务 Feign 客户端降级处理工厂
 * 
 * 当用户服务不可用或调用失败时，提供降级处理逻辑
 * 
 * @author Pixiv Platform Team
 */
@Slf4j
@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public Result<UserDTO> getUserById(Long userId) {
                log.error("调用用户服务失败，获取用户信息: userId={}, 错误信息: {}",
                        userId, cause.getMessage(), cause);
                return Result.error(503, "用户服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<ArtistDTO> getArtistByUserId(Long userId) {
                log.error("调用用户服务失败，获取画师信息: userId={}, 错误信息: {}",
                        userId, cause.getMessage(), cause);
                return Result.error(503, "用户服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<String> freezeWalletAmount(java.util.Map<String, Object> body) {
                log.error("调用用户服务失败，冻结钱包: body={}, 错误: {}", body, cause.getMessage(), cause);
                return Result.error(503, "用户服务暂时不可用");
            }

            @Override
            public Result<String> unfreezeWalletAmount(java.util.Map<String, Object> body) {
                log.error("调用用户服务失败，解冻钱包: body={}, 错误: {}", body, cause.getMessage(), cause);
                return Result.error(503, "用户服务暂时不可用");
            }

            @Override
            public Result<String> addWalletIncome(java.util.Map<String, Object> body) {
                log.error("调用用户服务失败，记录收入: body={}, 错误: {}", body, cause.getMessage(), cause);
                return Result.error(503, "用户服务暂时不可用");
            }

            @Override
            public Result<String> cancelWalletFreeze(java.util.Map<String, Object> body) {
                log.error("调用用户服务失败，取消冻结: body={}, 错误: {}", body, cause.getMessage(), cause);
                return Result.error(503, "用户服务暂时不可用");
            }
        };
    }
}
