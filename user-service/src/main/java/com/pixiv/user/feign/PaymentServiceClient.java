package com.pixiv.user.feign;

import com.pixiv.common.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 支付服务 Feign 客户端
 *
 * 用于调用 commission-service 的支付宝转账接口（提现打款）
 */
@FeignClient(name = "commission-service", contextId = "paymentServiceClient", path = "/api/payments")
public interface PaymentServiceClient {

    @PostMapping("/internal/transfer")
    Result<String> transferToAlipay(@RequestBody Map<String, String> body);
}
