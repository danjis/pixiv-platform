package com.pixiv.commission.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单实体
 *
 * 每次支付（定金/尾款）都会创建一条记录，对接支付宝
 */
@Entity
@Table(name = "payment_orders", indexes = {
        @Index(name = "idx_payment_order_no", columnList = "order_no", unique = true),
        @Index(name = "idx_payment_commission", columnList = "commission_id"),
        @Index(name = "idx_payment_payer", columnList = "payer_id"),
        @Index(name = "idx_payment_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 平台订单号（唯一，传给支付宝的 out_trade_no） */
    @Column(name = "order_no", length = 64, nullable = false, unique = true)
    private String orderNo;

    /** 关联的约稿 ID */
    @Column(name = "commission_id", nullable = false)
    private Long commissionId;

    /** 支付方用户 ID（委托方） */
    @Column(name = "payer_id", nullable = false)
    private Long payerId;

    /** 收款方用户 ID（画师） */
    @Column(name = "payee_id", nullable = false)
    private Long payeeId;

    /** 支付金额 */
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /** 支付类型：定金 / 尾款 */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", length = 20, nullable = false)
    private PaymentType paymentType;

    /** 支付宝交易号（回调后填入） */
    @Column(name = "alipay_trade_no", length = 64)
    private String alipayTradeNo;

    /** 支付状态 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /** 订单标题（展示给支付宝的商品名） */
    @Column(name = "subject", length = 256)
    private String subject;

    /** 支付宝回调时间 */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /** 退款时间 */
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    /** 退款原因 */
    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
