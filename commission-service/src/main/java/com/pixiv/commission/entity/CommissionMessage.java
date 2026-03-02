package com.pixiv.commission.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 约稿消息实体类
 * 
 * 表示约稿过程中客户和画师之间的沟通消息
 * 
 * 需求: 8.1 - 约稿订单处理
 */
@Entity
@Table(name = "commission_messages", indexes = {
    @Index(name = "idx_message_commission", columnList = "commission_id"),
    @Index(name = "idx_message_sender", columnList = "sender_id"),
    @Index(name = "idx_message_created", columnList = "created_at")
})
public class CommissionMessage {
    
    /**
     * 消息 ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 约稿 ID（关联的约稿订单）
     */
    @Column(name = "commission_id", nullable = false)
    private Long commissionId;
    
    /**
     * 发送者 ID（用户或画师）
     */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    
    /**
     * 消息内容
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    /**
     * 附件 URL（可选，用于上传进度图等）
     */
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 创建时自动设置创建时间
     */
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
    
    public Long getCommissionId() {
        return commissionId;
    }
    
    public void setCommissionId(Long commissionId) {
        this.commissionId = commissionId;
    }
    
    public Long getSenderId() {
        return senderId;
    }
    
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getAttachmentUrl() {
        return attachmentUrl;
    }
    
    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
