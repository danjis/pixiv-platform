package com.pixiv.commission.entity;

/**
 * 支付订单状态枚举
 */
public enum PaymentStatus {
    /** 待支付 */
    PENDING,
    /** 已支付 */
    PAID,
    /** 已退款 */
    REFUNDED,
    /** 支付失败 */
    FAILED,
    /** 已关闭（超时未支付） */
    CLOSED
}
