package com.pixiv.user.feign;

import com.pixiv.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 约稿服务 Feign 客户端
 *
 * 用于调用 commission-service 的内部接口
 */
@FeignClient(name = "commission-service", path = "/api/coupons")
public interface CommissionServiceClient {

    /**
     * 积分兑换创建用户优惠券
     *
     * @param body 包含 userId, couponName, amount, validDays
     * @return 优惠券信息
     */
    @PostMapping("/internal/create-for-user")
    Result<Map<String, Object>> createCouponForUser(@RequestBody Map<String, Object> body);
}
