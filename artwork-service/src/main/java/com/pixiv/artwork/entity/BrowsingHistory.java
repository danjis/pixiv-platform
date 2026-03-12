package com.pixiv.artwork.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 浏览记录实体
 * 记录用户浏览过的作品，同一用户对同一作品只保留最新浏览时间
 */
@Entity
@Table(name = "browsing_history", indexes = {
        @Index(name = "idx_bh_user_viewed", columnList = "user_id, viewed_at"),
        @Index(name = "idx_bh_artwork", columnList = "artwork_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_artwork", columnNames = { "user_id", "artwork_id" })
})
public class BrowsingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 作品 ID
     */
    @Column(name = "artwork_id", nullable = false)
    private Long artworkId;

    /**
     * 浏览时间
     */
    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @PrePersist
    protected void onCreate() {
        viewedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getArtworkId() {
        return artworkId;
    }

    public void setArtworkId(Long artworkId) {
        this.artworkId = artworkId;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
}
