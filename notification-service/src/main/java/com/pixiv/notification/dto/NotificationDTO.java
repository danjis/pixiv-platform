package com.pixiv.notification.dto;

import com.pixiv.notification.entity.NotificationType;
import java.time.LocalDateTime;

/**
 * 通知 DTO
 * 用于返回通知信息给前端
 */
public class NotificationDTO {
    
    /**
     * 通知 ID
     */
    private Long id;
    
    /**
     * 接收通知的用户 ID
     */
    private Long userId;
    
    /**
     * 通知类型（字符串形式）
     */
    private String type;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 关联链接 URL
     */
    private String linkUrl;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    // Constructors
    
    public NotificationDTO() {
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getLinkUrl() {
        return linkUrl;
    }
    
    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
