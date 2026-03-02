package com.pixiv.artwork.feign;

import com.pixiv.common.dto.ArtistDTO;
import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 用户服务 Feign 客户端
 * 
 * 用于作品服务调用用户服务的 API，主要用于：
 * - 获取用户基本信息
 * - 验证画师身份
 * - 获取画师详细信息
 * 
 * @author Pixiv Platform Team
 */
@FeignClient(name = "user-service", // 服务名称（在 Nacos 中注册的名称）
        path = "/api/users", // 基础路径
        fallbackFactory = UserServiceClientFallbackFactory.class // 降级处理工厂
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
     * @return 画师信息，如果用户不是画师则返回 null
     */
    @GetMapping("/{userId}/artist")
    Result<ArtistDTO> getArtistByUserId(@PathVariable("userId") Long userId);

    /**
     * 验证用户是否为画师
     * 
     * @param userId 用户 ID
     * @return true 表示是画师，false 表示不是画师
     */
    @GetMapping("/{userId}/is-artist")
    Result<Boolean> isArtist(@PathVariable("userId") Long userId);

    /**
     * 获取用户关注的所有人的 ID 列表
     * 
     * @param userId 用户 ID
     * @return 关注的用户 ID 列表
     */
    @GetMapping("/{userId}/following-ids")
    Result<List<Long>> getFollowingIds(@PathVariable("userId") Long userId);

    /**
     * 获取用户的所有粉丝 ID 列表（用于 Feed 推送）
     * 
     * @param userId 用户 ID
     * @return 粉丝 ID 列表
     */
    @GetMapping("/{userId}/follower-ids")
    Result<List<Long>> getFollowerIds(@PathVariable("userId") Long userId);

    /**
     * 批量根据用户 ID 获取用户基本信息
     * 
     * 用于解决 N+1 查询问题，一次请求获取多个用户信息。
     * 
     * @param userIds 用户 ID 列表
     * @return 用户基本信息列表
     */
    @PostMapping("/batch")
    Result<List<UserDTO>> getUsersByIds(@RequestBody List<Long> userIds);
}
