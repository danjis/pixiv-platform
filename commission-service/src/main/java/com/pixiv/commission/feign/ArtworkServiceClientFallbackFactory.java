package com.pixiv.commission.feign;

import com.pixiv.common.dto.ArtworkDTO;
import com.pixiv.common.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 作品服务 Feign 客户端降级处理工厂
 * 
 * 当作品服务不可用或调用失败时，提供降级处理逻辑
 * 
 * @author Pixiv Platform Team
 */
@Slf4j
@Component
public class ArtworkServiceClientFallbackFactory implements FallbackFactory<ArtworkServiceClient> {

    @Override
    public ArtworkServiceClient create(Throwable cause) {
        return new ArtworkServiceClient() {
            @Override
            public Result<ArtworkDTO> getArtworkById(Long artworkId) {
                log.error("调用作品服务失败，获取作品信息: artworkId={}, 错误信息: {}", 
                    artworkId, 
                    cause.getMessage(), 
                    cause);
                
                return Result.error(503, "作品服务暂时不可用，请稍后重试");
            }
        };
    }
}
