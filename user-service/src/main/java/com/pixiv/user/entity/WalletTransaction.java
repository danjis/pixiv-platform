package com.pixiv.user.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易记录
 * 记录每笔收入、冻结、解冻、提现等操作
 */
@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "idx_wallet_tx_user", columnList = "user_id"),
        @Index(name = "idx_wallet_tx_created", columnList = "created_at")
})
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 画师用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 交易类型 */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private TransactionType type;

    /** 交易金额（正数） */
    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    /** 交易后余额 */
    @Column(name = "balance_after", precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    /** 描述/备注 */
    @Column(name = "description", length = 500)
    private String description;

    /** 关联的约稿ID（可选） */
    @Column(name = "commission_id")
    private Long commissionId;

    /** 关联的支付订单号（可选） */
    @Column(name = "order_no", length = 64)
    private String orderNo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 交易类型枚举
     */
    public enum TransactionType {
        /** 收入（约稿完成） */
        INCOME,
        /** 冻结（约稿进行中的定金） */
        FREEZE,
        /** 解冻（约稿完成，定金变为可用） */
        UNFREEZE,
        /** 退款扣减 */
        REFUND,
        /** 提现 */
        WITHDRAW
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCommissionId() {
        return commissionId;
    }

    public void setCommissionId(Long commissionId) {
        this.commissionId = commissionId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
