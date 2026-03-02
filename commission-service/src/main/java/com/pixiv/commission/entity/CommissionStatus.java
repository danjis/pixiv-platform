package com.pixiv.commission.entity;

/**
 * 约稿状态枚举
 *
 * 约稿生命周期（新流程 - 用户发起）：
 * PENDING（用户提交需求）→ QUOTED（画师报价）→ DEPOSIT_PAID（用户支付定金）
 * → IN_PROGRESS（画师创作中）→ DELIVERED（画师交付）→ COMPLETED（用户确认并支付尾款）
 *
 * 分支：
 * PENDING → REJECTED（画师拒绝）
 * DELIVERED → IN_PROGRESS（用户请求修改）
 * 任意阶段 → CANCELLED（取消）
 */
public enum CommissionStatus {
    /** 待处理 - 用户提交约稿需求，等待画师处理 */
    PENDING,
    /** 已报价 - 画师已报价，等待用户确认并支付定金 */
    QUOTED,
    /** 已付定金 - 用户接受报价并通过支付宝支付定金 */
    DEPOSIT_PAID,
    /** 创作中 - 画师已开始创作 */
    IN_PROGRESS,
    /** 已交付 - 画师提交作品，等待用户确认并支付尾款 */
    DELIVERED,
    /** 已完成 - 用户确认满意并支付尾款，约稿完成 */
    COMPLETED,
    /** 已取消 */
    CANCELLED,
    /** 已拒绝 - 画师拒绝了约稿请求 */
    REJECTED
}
