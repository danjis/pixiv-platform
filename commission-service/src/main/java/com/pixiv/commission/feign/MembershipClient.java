package com.pixiv.commission.feign;

import com.pixiv.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 会员服务 Feign 客户端（commission-service调用user-service）
 * 
 * 用于获取用户会员等级，计算VIP手续费折扣，以及支付成功后升级会员
 */
@FeignClient(name = "user-service", contextId = "commissionMembershipClient", path = "/api/membership")
public interface MembershipClient {

    /**
     * 根据用户 ID 获取会员信息
     */
    @GetMapping("/{userId}")
    Result<Map<String, Object>> getMembership(@PathVariable("userId") Long userId);

    /**
     * 内部接口：支付成功后升级会员
     */
    @PostMapping("/internal/upgrade")
    Result<Map<String, Object>> upgradeMembership(@RequestBody Map<String, Object> request);
}
