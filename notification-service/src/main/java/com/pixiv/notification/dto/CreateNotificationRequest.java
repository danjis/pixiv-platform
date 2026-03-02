package com.pixiv.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建通知请求 DTO
 * 用于接收创建通知的请求参数
 */
public class CreateNotificationRequest {
    
    /**
     * 接收通知的用户 ID
     */
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;
    
    /**
     * 通知类型（字符串形式）
     */
    @NotBlank(message = "通知类型不能为空")
    private String type;
    
    /**
     * 通知内容
     */
    @NotBlank(message = "通知内容不能为空")
    private String content;
    
    /**
     * 关联链接 URL（可选）
     */
    private String linkUrl;
    
    // Constructors
    
    public CreateNotificationRequest() {
    }
    
    public CreateNotificationRequest(Long userId, String type, String content, String linkUrl) {
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.linkUrl = linkUrl;
    }
    
    // Getters and Setters
    
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
}
