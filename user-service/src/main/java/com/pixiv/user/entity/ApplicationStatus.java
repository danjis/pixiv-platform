package com.pixiv.user.entity;

/**
 * 画师申请状态枚举
 */
public enum ApplicationStatus {
    /**
     * 待审核
     */
    PENDING,
    
    /**
     * 已批准
     */
    APPROVED,
    
    /**
     * 已拒绝
     */
    REJECTED
}
