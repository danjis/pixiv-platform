package com.pixiv.artwork.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 评价实体类
 * 存储用户对作品的评价记录
 */
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_artwork", columnList = "artwork_id"),
        @Index(name = "idx_comment_user", columnList = "user_id"),
        @Index(name = "idx_comment_created", columnList = "created_at"),
        @Index(name = "idx_comment_parent", columnList = "parent_id")
})
public class Comment {

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
     * 父评论 ID（NULL 表示顶级评论）
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 被回复的用户 ID（用于嵌套回复中显示 "@用户名"）
     */
    @Column(name = "reply_to_user_id")
    private Long replyToUserId;

    /**
     * 评价内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getReplyToUserId() {
        return replyToUserId;
    }

    public void setReplyToUserId(Long replyToUserId) {
        this.replyToUserId = replyToUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
