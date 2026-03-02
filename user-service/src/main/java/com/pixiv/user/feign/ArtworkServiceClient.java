package com.pixiv.user.feign;

import com.pixiv.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 作品服务 Feign 客户端
 * 
 * 用于调用 artwork-service 的接口
 */
@FeignClient(
    name = "artwork-service",
    path = "/api/artworks"
)
public interface ArtworkServiceClient {
    
    /**
     * 获取画师的作品数量
     * 
     * @param artistId 画师 ID
     * @return 作品数量
     */
    @GetMapping("/count")
    Result<Long> getArtworkCount(@RequestParam(value = "artistId", required = true) Long artistId);
}
