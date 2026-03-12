package com.pixiv.artwork.feign;

import com.pixiv.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 会员服务 Feign 客户端
 * 
 * 用于获取用户会员等级信息，支持 VIP 徽章和评论高亮等功能
 */
@FeignClient(name = "user-service", contextId = "membershipServiceClient", path = "/api/membership")
public interface MembershipServiceClient {

    /**
     * 根据用户 ID 获取会员信息
     * 
     * @param userId 用户 ID
     * @return 会员信息（包含 level, expireTime, expired 等字段）
     */
    @GetMapping("/{userId}")
    Result<Map<String, Object>> getMembership(@PathVariable("userId") Long userId);
}
