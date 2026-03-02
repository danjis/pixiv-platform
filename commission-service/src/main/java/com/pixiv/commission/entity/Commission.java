package com.pixiv.commission.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 约稿实体
 *
 * 表示一个完整的约稿订单，支持全额托管支付模式
 */
@Entity
@Table(name = "commissions", indexes = {
        @Index(name = "idx_commission_client", columnList = "client_id"),
        @Index(name = "idx_commission_artist", columnList = "artist_id"),
        @Index(name = "idx_commission_status", columnList = "status"),
        @Index(name = "idx_commission_conversation", columnList = "conversation_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 委托方用户 ID */
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    /** 画师用户 ID */
    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    /** 关联的私信对话 ID */
    @Column(name = "conversation_id")
    private Long conversationId;

    /** 约稿标题 */
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    /** 需求描述 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 总金额 */
    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    /** 定金金额 */
    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;

    /** 定金是否已付 */
    @Column(name = "deposit_paid", nullable = false)
    @Builder.Default
    private Boolean depositPaid = false;

    /** 尾款是否已付（全额释放标记） */
    @Column(name = "final_paid", nullable = false)
    @Builder.Default
    private Boolean finalPaid = false;

    /** 截止日期 */
    @Column(name = "deadline")
    private LocalDate deadline;

    /** 允许修改次数 */
    @Column(name = "revisions_allowed")
    @Builder.Default
    private Integer revisionsAllowed = 3;

    /** 已使用修改次数 */
    @Column(name = "revisions_used")
    @Builder.Default
    private Integer revisionsUsed = 0;

    /** 交付作品 URL */
    @Column(name = "delivery_url", length = 500)
    private String deliveryUrl;

    /** 交付说明 */
    @Column(name = "delivery_note", columnDefinition = "TEXT")
    private String deliveryNote;

    /** 交付时间 */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /** 取消原因 */
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    /** 用户预算（参考价格） */
    @Column(name = "budget", precision = 10, scale = 2)
    private BigDecimal budget;

    /** 参考图片 URL */
    @Column(name = "reference_urls", columnDefinition = "TEXT")
    private String referenceUrls;

    /** 画师报价说明 */
    @Column(name = "quote_note", columnDefinition = "TEXT")
    private String quoteNote;

    /** 关联的约稿方案 ID */
    @Column(name = "commission_plan_id")
    private Long commissionPlanId;

    /** 约稿状态 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CommissionStatus status = CommissionStatus.PENDING;

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
