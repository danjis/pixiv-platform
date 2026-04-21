package com.pixiv.notification.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户反馈实体类
 * 存储用户提交的反馈/客服请求
 */
@Entity
@Table(name = "feedbacks", indexes = {
        @Index(name = "idx_feedback_user", columnList = "user_id"),
        @Index(name = "idx_feedback_status", columnList = "status"),
        @Index(name = "idx_feedback_created", columnList = "created_at"),
        @Index(name = "idx_feedback_type", columnList = "type"),
        @Index(name = "idx_feedback_commission", columnList = "commission_id"),
        @Index(name = "idx_feedback_payment", columnList = "payment_id")
})
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 提交用户 ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 反馈类型: BUG_REPORT / FEATURE_REQUEST / COMPLAINT / CONSULTATION / OTHER */
    @Column(name = "type", length = 30, nullable = false)
    private String type;

    /** 反馈标题 */
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    /** 反馈内容 */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 联系方式（邮箱或手机，选填） */
    @Column(name = "contact_info", length = 200)
    private String contactInfo;

    /** 关联约稿 ID（售后单使用） */
    @Column(name = "commission_id")
    private Long commissionId;

    /** 关联支付记录 ID（售后单使用） */
    @Column(name = "payment_id")
    private Long paymentId;

    /** 关联支付订单号（售后单使用） */
    @Column(name = "payment_order_no", length = 64)
    private String paymentOrderNo;

    /** 关联支付类型：DEPOSIT / FINAL_PAYMENT */
    @Column(name = "payment_type", length = 30)
    private String paymentType;

    /** 关联支付金额 */
    @Column(name = "payment_amount", precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    /** 申请动作：PLATFORM_INTERVENTION / REFUND_REVIEW */
    @Column(name = "requested_action", length = 40)
    private String requestedAction;

    /** 申请人角色：CLIENT / ARTIST */
    @Column(name = "applicant_role", length = 20)
    private String applicantRole;

    /** 对方用户 ID */
    @Column(name = "counterparty_user_id")
    private Long counterpartyUserId;

    /** 处理状态: PENDING / PROCESSING / RESOLVED / CLOSED */
    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING";

    /** 售后处理结果：NONE / REFUND_EXECUTED / REFUND_REJECTED / INTERVENTION_CLOSED */
    @Column(name = "resolution", length = 40)
    private String resolution = "NONE";

    /** 管理员回复 */
    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;

    /** 管理员回复时间 */
    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    /** 处理管理员 ID */
    @Column(name = "handled_by_admin_id")
    private Long handledByAdminId;

    /** 处理完成时间 */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Long getCommissionId() {
        return commissionId;
    }

    public void setCommissionId(Long commissionId) {
        this.commissionId = commissionId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentOrderNo() {
        return paymentOrderNo;
    }

    public void setPaymentOrderNo(String paymentOrderNo) {
        this.paymentOrderNo = paymentOrderNo;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getRequestedAction() {
        return requestedAction;
    }

    public void setRequestedAction(String requestedAction) {
        this.requestedAction = requestedAction;
    }

    public String getApplicantRole() {
        return applicantRole;
    }

    public void setApplicantRole(String applicantRole) {
        this.applicantRole = applicantRole;
    }

    public Long getCounterpartyUserId() {
        return counterpartyUserId;
    }

    public void setCounterpartyUserId(Long counterpartyUserId) {
        this.counterpartyUserId = counterpartyUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }

    public LocalDateTime getRepliedAt() {
        return repliedAt;
    }

    public void setRepliedAt(LocalDateTime repliedAt) {
        this.repliedAt = repliedAt;
    }

    public Long getHandledByAdminId() {
        return handledByAdminId;
    }

    public void setHandledByAdminId(Long handledByAdminId) {
        this.handledByAdminId = handledByAdminId;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
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
