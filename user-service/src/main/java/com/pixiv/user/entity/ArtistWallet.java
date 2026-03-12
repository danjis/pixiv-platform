package com.pixiv.user.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 画师钱包实体
 * 记录画师的收入余额，一个画师对应一个钱包
 */
@Entity
@Table(name = "artist_wallets")
public class ArtistWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 画师用户ID */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /** 总收入（累计） */
    @Column(name = "total_income", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalIncome = BigDecimal.ZERO;

    /** 可用余额 */
    @Column(name = "available_balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    /** 冻结金额（进行中的约稿，尚未确认完成） */
    @Column(name = "frozen_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal frozenAmount = BigDecimal.ZERO;

    /** 已提现金额（累计） */
    @Column(name = "withdrawn_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal withdrawnAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(BigDecimal frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    public BigDecimal getWithdrawnAmount() {
        return withdrawnAmount;
    }

    public void setWithdrawnAmount(BigDecimal withdrawnAmount) {
        this.withdrawnAmount = withdrawnAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
