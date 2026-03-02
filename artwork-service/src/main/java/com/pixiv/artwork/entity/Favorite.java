package com.pixiv.artwork.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 收藏实体类
 * 存储用户对作品的收藏记录
 */
@Entity
@Table(name = "favorites", indexes = {
    @Index(name = "idx_favorite_user", columnList = "user_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "idx_favorite_unique", columnNames = {"artwork_id", "user_id"})
})
public class Favorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 作品 ID
     */
    @Column(name = "artwork_id", nullable = false)
    private Long artworkId;
    
    /**
     * 用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
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
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
