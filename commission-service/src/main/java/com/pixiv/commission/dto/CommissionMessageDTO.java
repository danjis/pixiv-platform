package com.pixiv.commission.dto;

import java.time.LocalDateTime;

/**
 * 约稿消息 DTO
 * 
 * @author Pixiv Platform Team
 */
public class CommissionMessageDTO {
    
    /**
     * 消息 ID
     */
    private Long id;
    
    /**
     * 约稿 ID
     */
    private Long commissionId;
    
    /**
     * 发送者 ID
     */
    private Long senderId;
    
    /**
     * 发送者用户名
     */
    private String senderName;
    
    /**
     * 发送者头像
     */
    private String senderAvatar;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 附件 URL
     */
    private String attachmentUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
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
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getSenderAvatar() {
        return senderAvatar;
    }
    
    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
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
