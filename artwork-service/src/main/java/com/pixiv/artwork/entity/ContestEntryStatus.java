package com.pixiv.artwork.entity;

/**
 * 参赛作品状态枚举
 */
public enum ContestEntryStatus {
    /** 已提交 */
    SUBMITTED,
    /** 已通过审核 */
    APPROVED,
    /** 已拒绝 */
    REJECTED,
    /** 获奖作品 */
    WINNER
}
