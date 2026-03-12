package com.pixiv.commission.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 用户优惠券实体
 * 记录用户领取和使用优惠券的情况
 */
@Entity
@Table(name = "user_coupons", indexes = {
        @Index(name = "idx_uc_user_id", columnList = "user_id"),
        @Index(name = "idx_uc_coupon_id", columnList = "coupon_id"),
        @Index(name = "idx_uc_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_coupon", columnNames = { "user_id", "coupon_id" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户 ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 优惠券 ID */
    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    /** 使用状态 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private UserCouponStatus status = UserCouponStatus.UNUSED;

    /** 使用时间 */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /** 用于哪笔约稿 */
    @Column(name = "commission_id")
    private Long commissionId;

    /** 领取时间 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /** 关联的优惠券信息（懒加载） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", insertable = false, updatable = false)
    private Coupon coupon;
}
