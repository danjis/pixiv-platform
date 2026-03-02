package com.pixiv.commission.controller;

import com.pixiv.commission.dto.PaymentOrderDTO;
import com.pixiv.commission.entity.PaymentType;
import com.pixiv.commission.service.PaymentService;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 *
 * 处理支付宝沙箱支付相关操作
 */
@Tag(name = "支付管理", description = "支付宝沙箱支付接口")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付订单，返回支付宝支付页面 HTML
     */
    @Operation(summary = "创建支付", description = "生成支付宝支付表单，前端提交表单跳转支付宝")
    @PostMapping("/create")
    public ResponseEntity<Result<String>> createPayment(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            Long commissionId = Long.valueOf(body.get("commissionId").toString());
            String typeStr = body.get("paymentType").toString();
            PaymentType paymentType = PaymentType.valueOf(typeStr.toUpperCase());

            String payForm = paymentService.createPayment(userId, commissionId, paymentType);
            return ResponseEntity.ok(Result.success(payForm));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("创建支付失败", e);
            return ResponseEntity.internalServerError()
                    .body(Result.error("创建支付失败: " + e.getMessage()));
        }
    }

    /**
     * 支付宝异步通知回调（非常重要！）
     * 支付宝服务器直接调用此接口，无需 X-User-Id
     */
    @Operation(summary = "支付宝异步通知", description = "支付宝回调接口，处理支付结果")
    @PostMapping("/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            // 将 request 中的参数转成 Map
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
                String[] values = entry.getValue();
                StringBuilder valueStr = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    valueStr.append(i == values.length - 1 ? values[i] : values[i] + ",");
                }
                params.put(entry.getKey(), valueStr.toString());
            }

            return paymentService.handleAlipayNotify(params);
        } catch (Exception e) {
            logger.error("处理支付宝通知异常", e);
            return "failure";
        }
    }

    /**
     * 查询支付订单状态
     */
    @Operation(summary = "查询支付状态")
    @GetMapping("/status")
    public ResponseEntity<Result<PaymentOrderDTO>> getPaymentStatus(
            @RequestParam("orderNo") String orderNo) {
        try {
            PaymentOrderDTO dto = paymentService.getPaymentStatus(orderNo);
            return ResponseEntity.ok(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 继续支付已有的待支付订单
     */
    @Operation(summary = "继续支付", description = "对已有的待支付订单重新生成支付宝表单")
    @PostMapping("/continue")
    public ResponseEntity<Result<String>> continuePay(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            String orderNo = body.get("orderNo").toString();
            String payForm = paymentService.continuePay(userId, orderNo);
            return ResponseEntity.ok(Result.success(payForm));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("继续支付失败", e);
            return ResponseEntity.internalServerError()
                    .body(Result.error("继续支付失败: " + e.getMessage()));
        }
    }

    /**
     * 获取约稿的所有支付记录
     */
    @Operation(summary = "约稿支付记录")
    @GetMapping("/commission/{commissionId}")
    public ResponseEntity<Result<List<PaymentOrderDTO>>> getCommissionPayments(
            @PathVariable("commissionId") Long commissionId,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            List<PaymentOrderDTO> payments = paymentService.getPaymentsByCommission(userId, commissionId);
            return ResponseEntity.ok(Result.success(payments));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }
}
