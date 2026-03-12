package com.pixiv.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户反馈实体类
 * 存储用户提交的反馈/客服请求
 */
@Entity
@Table(name = "feedbacks", indexes = {
        @Index(name = "idx_feedback_user", columnList = "user_id"),
        @Index(name = "idx_feedback_status", columnList = "status"),
        @Index(name = "idx_feedback_created", columnList = "created_at")
})
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 提交用户 ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 反馈类型: BUG_REPORT / FEATURE_REQUEST / COMPLAINT / CONSULTATION / OTHER */
    @Column(name = "type", length = 30, nullable = false)
    private String type;

    /** 反馈标题 */
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    /** 反馈内容 */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 联系方式（邮箱或手机，选填） */
    @Column(name = "contact_info", length = 200)
    private String contactInfo;

    /** 处理状态: PENDING / PROCESSING / RESOLVED / CLOSED */
    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING";

    /** 管理员回复 */
    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;

    /** 管理员回复时间 */
    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }

    public LocalDateTime getRepliedAt() {
        return repliedAt;
    }

    public void setRepliedAt(LocalDateTime repliedAt) {
        this.repliedAt = repliedAt;
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
