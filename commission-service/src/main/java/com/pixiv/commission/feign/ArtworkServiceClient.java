package com.pixiv.commission.feign;

import com.pixiv.common.dto.ArtworkDTO;
import com.pixiv.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 作品服务 Feign 客户端
 * 
 * 用于约稿服务调用作品服务的 API，主要用于：
 * - 获取作品信息
 * - 验证作品归属
 * - 企划投稿时关联作品
 * 
 * @author Pixiv Platform Team
 */
@FeignClient(
    name = "artwork-service",         // 服务名称（在 Nacos 中注册的名称）
    path = "/api/artworks",           // 基础路径
    fallbackFactory = ArtworkServiceClientFallbackFactory.class  // 降级处理工厂
)
public interface ArtworkServiceClient {

    /**
     * 根据作品 ID 获取作品信息
     * 
     * @param artworkId 作品 ID
     * @return 作品信息
     */
    @GetMapping("/{artworkId}")
    Result<ArtworkDTO> getArtworkById(@PathVariable("artworkId") Long artworkId);
}
