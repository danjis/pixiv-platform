package com.pixiv.user.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现申请实体
 * 画师发起提现 → 管理员审批 → 完成/拒绝
 */
@Entity
@Table(name = "withdrawal_requests", indexes = {
        @Index(name = "idx_withdrawal_user", columnList = "user_id"),
        @Index(name = "idx_withdrawal_status", columnList = "status"),
        @Index(name = "idx_withdrawal_created", columnList = "created_at")
})
public class WithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 画师用户ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 提现金额 */
    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    /** 支付宝账号 */
    @Column(name = "alipay_account", length = 100, nullable = false)
    private String alipayAccount;

    /** 支付宝实名（收款方姓名） */
    @Column(name = "alipay_name", length = 50)
    private String alipayName;

    /** 状态 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private WithdrawalStatus status = WithdrawalStatus.PENDING;

    /** 处理人（管理员ID） */
    @Column(name = "processed_by")
    private Long processedBy;

    /** 处理时间 */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /** 管理员备注 */
    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum WithdrawalStatus {
        /** 待审批 */
        PENDING,
        /** 已通过（已打款） */
        APPROVED,
        /** 已拒绝（金额退回可用余额） */
        REJECTED
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getAlipayAccount() {
        return alipayAccount;
    }

    public void setAlipayAccount(String alipayAccount) {
        this.alipayAccount = alipayAccount;
    }

    public String getAlipayName() {
        return alipayName;
    }

    public void setAlipayName(String alipayName) {
        this.alipayName = alipayName;
    }

    public WithdrawalStatus getStatus() {
        return status;
    }

    public void setStatus(WithdrawalStatus status) {
        this.status = status;
    }

    public Long getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Long processedBy) {
        this.processedBy = processedBy;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
