package com.pixiv.notification.dto;

/**
 * 未读通知数量 DTO
 * 用于返回用户的未读通知数量
 */
public class UnreadCountDTO {
    
    /**
     * 未读通知数量
     */
    private Long count;
    
    // Constructors
    
    public UnreadCountDTO() {
    }
    
    public UnreadCountDTO(Long count) {
        this.count = count;
    }
    
    // Getters and Setters
    
    public Long getCount() {
        return count;
    }
    
    public void setCount(Long count) {
        this.count = count;
    }
}
