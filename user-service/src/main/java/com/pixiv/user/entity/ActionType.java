package com.pixiv.user.entity;

/**
 * 管理员操作类型枚举
 */
public enum ActionType {
    /**
     * 批准画师申请
     */
    APPROVE_ARTIST_APPLICATION,
    
    /**
     * 拒绝画师申请
     */
    REJECT_ARTIST_APPLICATION,
    
    /**
     * 删除作品
     */
    DELETE_ARTWORK,
    
    /**
     * 删除评论
     */
    DELETE_COMMENT,
    
    /**
     * 封禁用户
     */
    BAN_USER,
    
    /**
     * 解封用户
     */
    UNBAN_USER,
    
    /**
     * 其他操作
     */
    OTHER
}
