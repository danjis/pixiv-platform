package com.pixiv.artwork.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 作品标签关联实体类
 * 存储作品与标签的多对多关系，支持智能打标和手动添加
 */
@Entity
@Table(name = "artwork_tags", indexes = {
    @Index(name = "idx_artwork_tag_artwork", columnList = "artwork_id"),
    @Index(name = "idx_artwork_tag_tag", columnList = "tag_id")
})
public class ArtworkTag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 作品 ID
     */
    @Column(name = "artwork_id", nullable = false)
    private Long artworkId;
    
    /**
     * 标签 ID
     */
    @Column(name = "tag_id", nullable = false)
    private Long tagId;
    
    /**
     * 标签来源（自动打标或手动添加）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TagSource source;
    
    /**
     * 置信度（仅用于自动打标，范围 0.0-1.0）
     */
    @Column(columnDefinition = "FLOAT")
    private Float confidence;
    
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
    
    public Long getArtworkId() {
        return artworkId;
    }
    
    public void setArtworkId(Long artworkId) {
        this.artworkId = artworkId;
    }
    
    public Long getTagId() {
        return tagId;
    }
    
    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
    
    public TagSource getSource() {
        return source;
    }
    
    public void setSource(TagSource source) {
        this.source = source;
    }
    
    public Float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
