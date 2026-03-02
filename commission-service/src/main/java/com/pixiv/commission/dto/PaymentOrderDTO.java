package com.pixiv.commission.dto;

import com.pixiv.commission.entity.PaymentStatus;
import com.pixiv.commission.entity.PaymentType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderDTO {
    private Long id;
    private String orderNo;
    private Long commissionId;
    private Long payerId;
    private Long payeeId;
    private BigDecimal amount;
    private PaymentType paymentType;
    private String alipayTradeNo;
    private PaymentStatus status;
    private String subject;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    /** 待支付订单的过期时间 */
    private LocalDateTime expireAt;
}
