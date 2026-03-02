package com.pixiv.commission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 发送约稿消息请求 DTO
 * 
 * @author Pixiv Platform Team
 */
public class SendMessageRequest {
    
    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(min = 1, max = 2000, message = "消息内容长度必须在 1-2000 字符之间")
    private String content;
    
    /**
     * 附件 URL（可选，用于上传进度图等）
     */
    @Size(max = 500, message = "附件 URL 长度不能超过 500 字符")
    private String attachmentUrl;
    
    // Getters and Setters
    
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
}
