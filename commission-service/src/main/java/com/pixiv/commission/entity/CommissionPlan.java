package com.pixiv.commission.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 约稿方案（画师预设的服务套餐）
 *
 * 画师可以创建多个约稿方案，用户在发起约稿时可选择方案
 */
@Entity
@Table(name = "commission_plans", indexes = {
        @Index(name = "idx_plan_artist", columnList = "artist_id"),
        @Index(name = "idx_plan_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 画师用户 ID */
    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    /** 方案标题 */
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    /** 方案描述 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 起始价格 */
    @Column(name = "price_start", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceStart;

    /** 最高价格（可选，用于显示价格区间） */
    @Column(name = "price_end", precision = 10, scale = 2)
    private BigDecimal priceEnd;

    /** 预计工期（天） */
    @Column(name = "estimated_days")
    private Integer estimatedDays;

    /** 包含修改次数 */
    @Column(name = "revisions_included")
    @Builder.Default
    private Integer revisionsIncluded = 3;

    /** 方案类型（如: 头像、半身、全身、插画、立绘等） */
    @Column(name = "category", length = 50)
    private String category;

    /** 示例图片（逗号分隔 URL） */
    @Column(name = "sample_images", columnDefinition = "TEXT")
    private String sampleImages;

    /** 是否启用 */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /** 排序权重 */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

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
