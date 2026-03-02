package com.pixiv.notification.entity;

/**
 * 通知类型枚举
 * 定义系统中所有可能的通知类型
 */
public enum NotificationType {
    /**
     * 收到约稿请求
     */
    COMMISSION_REQUEST,
    
    /**
     * 约稿被接受
     */
    COMMISSION_ACCEPTED,
    
    /**
     * 约稿被拒绝
     */
    COMMISSION_REJECTED,
    
    /**
     * 约稿完成
     */
    COMMISSION_COMPLETED,
    
    /**
     * 画师申请通过
     */
    APPLICATION_APPROVED,
    
    /**
     * 画师申请被拒绝
     */
    APPLICATION_REJECTED,
    
    /**
     * 新粉丝关注
     */
    NEW_FOLLOWER,
    
    /**
     * 作品被点赞
     */
    ARTWORK_LIKED,
    
    /**
     * 作品被收藏
     */
    ARTWORK_FAVORITED,
    
    /**
     * 作品收到新评价
     */
    ARTWORK_COMMENTED,
    
    /**
     * 企划有新投稿
     */
    PROJECT_SUBMISSION
}
