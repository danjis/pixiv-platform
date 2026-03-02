package com.pixiv.commission.feign;

import com.pixiv.common.dto.ArtistDTO;
import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务 Feign 客户端
 * 
 * 用于约稿服务调用用户服务的 API，主要用于：
 * - 获取用户基本信息
 * - 验证画师身份
 * - 获取画师详细信息
 * 
 * @author Pixiv Platform Team
 */
@FeignClient(
    name = "user-service",           // 服务名称（在 Nacos 中注册的名称）
    path = "/api/users",              // 基础路径
    fallbackFactory = UserServiceClientFallbackFactory.class  // 降级处理工厂
)
public interface UserServiceClient {

    /**
     * 根据用户 ID 获取用户信息
     * 
     * @param userId 用户 ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    Result<UserDTO> getUserById(@PathVariable("userId") Long userId);
    
    /**
     * 根据用户 ID 获取画师信息
     * 
     * @param userId 用户 ID
     * @return 画师信息
     */
    @GetMapping("/{userId}/artist")
    Result<ArtistDTO> getArtistByUserId(@PathVariable("userId") Long userId);
}
