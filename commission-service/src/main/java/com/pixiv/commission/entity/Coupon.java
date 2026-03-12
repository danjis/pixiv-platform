package com.pixiv.commission.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体
 * 管理员创建，用户领取并在约稿支付时使用
 */
@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupon_code", columnList = "code", unique = true),
        @Index(name = "idx_coupon_status", columnList = "status"),
        @Index(name = "idx_coupon_end_time", columnList = "end_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 优惠券代码（唯一，用于领取） */
    @Column(name = "code", length = 32, nullable = false, unique = true)
    private String code;

    /** 优惠券名称 */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /** 优惠券描述 */
    @Column(name = "description", length = 500)
    private String description;

    /** 优惠券类型：百分比折扣 / 固定金额 */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private CouponType type;

    /** 折扣值：百分比类型为 0-100 的整数(如10表示打9折)，固定金额为减免金额 */
    @Column(name = "discount_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal discountValue;

    /** 最低订单金额（满多少可用） */
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    /** 最大折扣金额（百分比类型的上限） */
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    /** 总发行量 */
    @Column(name = "total_quantity", nullable = false)
    @Builder.Default
    private Integer totalQuantity = 0;

    /** 已领取数量 */
    @Column(name = "claimed_quantity", nullable = false)
    @Builder.Default
    private Integer claimedQuantity = 0;

    /** 已使用数量 */
    @Column(name = "used_quantity", nullable = false)
    @Builder.Default
    private Integer usedQuantity = 0;

    /** 生效开始时间 */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /** 生效结束时间 */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /** 优惠券状态 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private CouponStatus status = CouponStatus.ACTIVE;

    /** 发放方式：PUBLIC（领券中心可领）/ CODE_ONLY（仅凭码兑换） */
    @Column(name = "distribution_type", length = 20, nullable = false)
    @Builder.Default
    private String distributionType = "PUBLIC";

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

    /**
     * 是否可领取
     */
    public boolean isClaimable() {
        LocalDateTime now = LocalDateTime.now();
        return status == CouponStatus.ACTIVE
                && now.isAfter(startTime) && now.isBefore(endTime)
                && claimedQuantity < totalQuantity;
    }

    /**
     * 计算折扣金额
     */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (orderAmount.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }
        if (type == CouponType.FIXED) {
            // 固定减免，不超过订单金额
            return discountValue.min(orderAmount);
        } else {
            // 百分比折扣
            BigDecimal discount = orderAmount.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                discount = maxDiscountAmount;
            }
            return discount;
        }
    }
}
