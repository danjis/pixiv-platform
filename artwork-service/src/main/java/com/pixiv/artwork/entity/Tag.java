package com.pixiv.artwork.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 标签实体类
 * 存储作品标签信息，支持智能打标和手动添加
 */
@Entity
@Table(name = "tags", indexes = {
        @Index(name = "idx_tag_name", columnList = "name", unique = true),
        @Index(name = "idx_tag_usage_count", columnList = "usage_count")
})
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 标签名称，唯一（英文）
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 标签中文名称（翻译）
     */
    @Column(name = "name_zh", length = 100)
    private String nameZh;

    /**
     * 标签使用次数，用于统计热门标签
     */
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameZh() {
        return nameZh;
    }

    public void setNameZh(String nameZh) {
        this.nameZh = nameZh;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 增加使用次数
     */
    public void incrementUsageCount() {
        this.usageCount++;
    }

    /**
     * 减少使用次数
     */
    public void decrementUsageCount() {
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }
}
