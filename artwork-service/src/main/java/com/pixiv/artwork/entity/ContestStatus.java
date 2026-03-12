package com.pixiv.artwork.entity;

/**
 * 比赛状态枚举
 */
public enum ContestStatus {
    /** 即将开始 */
    UPCOMING,
    /** 进行中（接受投稿） */
    ACTIVE,
    /** 投票阶段 */
    VOTING,
    /** 已结束 */
    ENDED,
    /** 已取消 */
    CANCELLED
}
