package com.pixiv.notification.service;

import com.pixiv.common.dto.Result;
import com.pixiv.notification.dto.CommissionSnapshot;
import com.pixiv.notification.dto.CreateNotificationRequest;
import com.pixiv.notification.dto.PaymentSnapshot;
import com.pixiv.notification.entity.Feedback;
import com.pixiv.notification.feign.CommissionServiceClient;
import com.pixiv.notification.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户反馈 / 售后服务
 */
@Service
public class FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);

    private static final String TYPE_AFTER_SALE = "AFTER_SALE";
    private static final String ACTION_PLATFORM_INTERVENTION = "PLATFORM_INTERVENTION";
    private static final String ACTION_REFUND_REVIEW = "REFUND_REVIEW";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_RESOLVED = "RESOLVED";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final String RESOLUTION_NONE = "NONE";
    private static final String RESOLUTION_REFUND_EXECUTED = "REFUND_EXECUTED";
    private static final String RESOLUTION_REFUND_REJECTED = "REFUND_REJECTED";
    private static final String RESOLUTION_INTERVENTION_CLOSED = "INTERVENTION_CLOSED";
    private static final String MODE_FULL = "FULL";
    private static final String MODE_FINAL_ONLY = "FINAL_ONLY";
    private static final List<String> VALID_TYPES = Arrays.asList(
            "BUG_REPORT", "FEATURE_REQUEST", "COMPLAINT", "CONSULTATION", "OTHER", TYPE_AFTER_SALE);
    private static final List<String> OPEN_CASE_STATUSES = Arrays.asList(STATUS_PENDING, STATUS_PROCESSING);

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private CommissionServiceClient commissionServiceClient;

    @Autowired
    private NotificationService notificationService;

    /**
     * 提交普通反馈
     */
    @Transactional
    public Map<String, Object> submitFeedback(Long userId, String type, String title, String content,
            String contactInfo) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("反馈标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("反馈内容不能为空");
        }
        if (type == null || !VALID_TYPES.contains(type)) {
            type = "OTHER";
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(type);
        feedback.setTitle(title.trim());
        feedback.setContent(content.trim());
        feedback.setContactInfo(contactInfo);
        feedback.setStatus(STATUS_PENDING);
        feedback.setResolution(RESOLUTION_NONE);
        feedbackRepository.save(feedback);

        logger.info("用户反馈提交成功: userId={}, type={}, title={}", userId, type, title);
        return convertToMap(feedback);
    }

    /**
     * 提交售后/申诉申请
     */
    @Transactional
    public Map<String, Object> submitAfterSale(Long userId, Long commissionId, Long paymentId,
            String requestedAction, String title, String content, String contactInfo) {
        if (commissionId == null) {
            throw new IllegalArgumentException("售后申请必须关联约稿");
        }
        if (content == null || content.trim().length() < 10) {
            throw new IllegalArgumentException("请至少填写 10 个字的申请说明");
        }

        CommissionSnapshot commission = getCommissionSnapshot(userId, commissionId);
        boolean isClient = userId.equals(commission.getClientId());
        boolean isArtist = userId.equals(commission.getArtistId());
        if (!isClient && !isArtist) {
            throw new IllegalArgumentException("无权对该约稿提交售后申请");
        }

        String action = normalizeRequestedAction(requestedAction, paymentId);
        List<PaymentSnapshot> payments = getPaymentSnapshots(userId, commissionId);
        PaymentSnapshot matchedPayment = paymentId != null ? findPayment(payments, paymentId) : null;

        if (matchedPayment != null) {
            if (!userId.equals(matchedPayment.getPayerId())) {
                throw new IllegalArgumentException("只有付款方才能对该支付记录发起售后申请");
            }
            if (!"PAID".equals(matchedPayment.getStatus())) {
                throw new IllegalArgumentException("当前支付记录状态不支持售后申请");
            }
            if (feedbackRepository.existsByTypeAndPaymentIdAndStatusIn(TYPE_AFTER_SALE, paymentId, OPEN_CASE_STATUSES)) {
                throw new IllegalArgumentException("该支付记录已有处理中售后单，请勿重复提交");
            }
        } else if (feedbackRepository.existsByTypeAndCommissionIdAndUserIdAndStatusIn(
                TYPE_AFTER_SALE, commissionId, userId, OPEN_CASE_STATUSES)) {
            throw new IllegalArgumentException("该约稿已有处理中售后单，请勿重复提交");
        }

        validateAfterSaleAction(commission, matchedPayment, action, isClient);

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(TYPE_AFTER_SALE);
        feedback.setTitle(buildAfterSaleTitle(title, commission, matchedPayment, action));
        feedback.setContent(content.trim());
        feedback.setContactInfo(contactInfo);
        feedback.setCommissionId(commissionId);
        feedback.setPaymentId(matchedPayment != null ? matchedPayment.getId() : null);
        feedback.setPaymentOrderNo(matchedPayment != null ? matchedPayment.getOrderNo() : null);
        feedback.setPaymentType(matchedPayment != null ? matchedPayment.getPaymentType() : null);
        feedback.setPaymentAmount(matchedPayment != null ? matchedPayment.getAmount() : null);
        feedback.setRequestedAction(action);
        feedback.setApplicantRole(isClient ? "CLIENT" : "ARTIST");
        feedback.setCounterpartyUserId(isClient ? commission.getArtistId() : commission.getClientId());
        feedback.setStatus(STATUS_PENDING);
        feedback.setResolution(RESOLUTION_NONE);
        feedbackRepository.save(feedback);

        logger.info("售后申请提交成功: feedbackId={}, userId={}, commissionId={}, paymentId={}, action={}",
                feedback.getId(), userId, commissionId, feedback.getPaymentId(), action);

        sendSystemNotice(userId,
                "你的售后申请已提交，平台会尽快人工审核。",
                "/orders");
        return convertToMap(feedback);
    }

    /**
     * 获取用户反馈列表
     */
    public Map<String, Object> getUserFeedbacks(Long userId, int page, int size, String type, Long commissionId) {
        Page<Feedback> pageResult;
        PageRequest pageable = PageRequest.of(page, size);

        if (type != null && !type.isBlank() && commissionId != null) {
            pageResult = feedbackRepository.findByUserIdAndTypeAndCommissionIdOrderByCreatedAtDesc(
                    userId, type, commissionId, pageable);
        } else if (type != null && !type.isBlank()) {
            pageResult = feedbackRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        } else {
            pageResult = feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        return buildPageResult(pageResult, page, size);
    }

    /**
     * 获取所有反馈（管理端）
     */
    public Map<String, Object> getAllFeedbacks(int page, int size, String status, String type) {
        Page<Feedback> pageResult;
        PageRequest pageable = PageRequest.of(page, size);

        if (status != null && !status.isBlank() && type != null && !type.isBlank()) {
            pageResult = feedbackRepository.findByTypeAndStatusOrderByCreatedAtDesc(type, status, pageable);
        } else if (status != null && !status.isBlank()) {
            pageResult = feedbackRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else if (type != null && !type.isBlank()) {
            pageResult = feedbackRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
        } else {
            pageResult = feedbackRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        Map<String, Object> result = buildPageResult(pageResult, page, size);
        result.put("pendingCount", feedbackRepository.countByStatus(STATUS_PENDING));
        result.put("processingCount", feedbackRepository.countByStatus(STATUS_PROCESSING));
        return result;
    }

    /**
     * 管理员回复普通反馈
     */
    @Transactional
    public Map<String, Object> replyFeedback(Long feedbackId, String reply, String newStatus) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("反馈不存在: " + feedbackId));

        if (TYPE_AFTER_SALE.equals(feedback.getType())) {
            throw new IllegalArgumentException("售后单请使用专用处理接口");
        }

        if (reply != null && !reply.trim().isEmpty()) {
            feedback.setAdminReply(reply.trim());
            feedback.setRepliedAt(LocalDateTime.now());
        }
        if (newStatus != null && !newStatus.isBlank()) {
            feedback.setStatus(newStatus);
        }
        feedbackRepository.save(feedback);

        logger.info("管理员回复反馈: feedbackId={}, status={}", feedbackId, newStatus);
        return convertToMap(feedback);
    }

    /**
     * 管理员处理售后单
     */
    @Transactional
    public Map<String, Object> processAfterSale(Long feedbackId, Long adminId, String action,
            String reply, String refundMode) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("售后单不存在: " + feedbackId));

        if (!TYPE_AFTER_SALE.equals(feedback.getType())) {
            throw new IllegalArgumentException("该记录不是售后单");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("请选择处理动作");
        }

        String normalizedAction = action.trim().toUpperCase(Locale.ROOT);
        if (reply != null && !reply.trim().isEmpty()) {
            feedback.setAdminReply(reply.trim());
            feedback.setRepliedAt(LocalDateTime.now());
        }
        feedback.setHandledByAdminId(adminId);

        switch (normalizedAction) {
            case "PROCESSING" -> {
                feedback.setStatus(STATUS_PROCESSING);
                feedback.setResolution(RESOLUTION_NONE);
                feedbackRepository.save(feedback);
                sendSystemNotice(feedback.getUserId(),
                        "你的售后申请已进入人工审核阶段，请耐心等待处理结果。",
                        "/orders");
            }
            case "REJECT" -> {
                feedback.setStatus(STATUS_RESOLVED);
                feedback.setResolution(RESOLUTION_REFUND_REJECTED);
                feedback.setResolvedAt(LocalDateTime.now());
                feedbackRepository.save(feedback);
                sendSystemNotice(feedback.getUserId(),
                        "你的售后申请已处理完成，平台暂不支持本次退款申请。",
                        "/orders");
            }
            case "APPROVE_REFUND" -> {
                executeRefund(feedback, adminId, refundMode, reply);
                feedback.setStatus(STATUS_RESOLVED);
                feedback.setResolution(RESOLUTION_REFUND_EXECUTED);
                feedback.setResolvedAt(LocalDateTime.now());
                feedbackRepository.save(feedback);
                notifyAfterSaleRefund(feedback, refundMode);
            }
            case "CLOSE" -> {
                feedback.setStatus(STATUS_CLOSED);
                feedback.setResolution(RESOLUTION_INTERVENTION_CLOSED);
                feedback.setResolvedAt(LocalDateTime.now());
                feedbackRepository.save(feedback);
                sendSystemNotice(feedback.getUserId(),
                        "你的售后申请已关闭，如仍有问题可重新补充材料提交。",
                        "/orders");
            }
            default -> throw new IllegalArgumentException("不支持的处理动作: " + action);
        }

        logger.info("售后单处理完成: feedbackId={}, action={}, resolution={}",
                feedbackId, normalizedAction, feedback.getResolution());
        return convertToMap(feedback);
    }

    private void executeRefund(Feedback feedback, Long adminId, String refundMode, String reply) {
        String mode = normalizeRefundMode(feedback, refundMode);
        String reason = buildAdminReason(feedback, reply, mode);

        if (MODE_FINAL_ONLY.equals(mode)) {
            if (feedback.getPaymentId() == null || !"FINAL_PAYMENT".equals(feedback.getPaymentType())) {
                throw new IllegalArgumentException("当前售后单不支持仅退尾款");
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("paymentId", feedback.getPaymentId());
            body.put("reason", reason);
            Result<Void> result = commissionServiceClient.adminRefund(adminId, "ADMIN", body);
            if (result == null || !result.isSuccess()) {
                throw new IllegalArgumentException(result != null ? result.getMessage() : "退尾款失败");
            }
            return;
        }

        if (feedback.getCommissionId() == null) {
            throw new IllegalArgumentException("售后单缺少约稿信息，无法执行全额退款");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("reason", reason);
        body.put("refundDeposit", true);
        Result<CommissionSnapshot> result = commissionServiceClient.adminCancelCommission(
                adminId, "ADMIN", feedback.getCommissionId(), body);
        if (result == null || !result.isSuccess()) {
            throw new IllegalArgumentException(result != null ? result.getMessage() : "取消约稿并退款失败");
        }
    }

    private void validateAfterSaleAction(CommissionSnapshot commission, PaymentSnapshot payment,
            String action, boolean isClient) {
        String commissionStatus = commission.getStatus();

        if (ACTION_PLATFORM_INTERVENTION.equals(action)) {
            if (!isClient) {
                throw new IllegalArgumentException("当前仅支持委托方发起平台介入申请");
            }
            if (!Arrays.asList("DEPOSIT_PAID", "IN_PROGRESS", "DELIVERED", "COMPLETED").contains(commissionStatus)) {
                throw new IllegalArgumentException("当前约稿阶段不支持发起平台介入");
            }
            return;
        }

        if (!ACTION_REFUND_REVIEW.equals(action)) {
            throw new IllegalArgumentException("无效的售后申请类型");
        }
        if (payment == null) {
            throw new IllegalArgumentException("退款审核必须关联已支付订单");
        }
        if ("DEPOSIT".equals(payment.getPaymentType())) {
            if (!Arrays.asList("DEPOSIT_PAID", "IN_PROGRESS", "DELIVERED", "COMPLETED").contains(commissionStatus)) {
                throw new IllegalArgumentException("当前阶段不支持针对定金发起退款审核");
            }
            return;
        }
        if ("FINAL_PAYMENT".equals(payment.getPaymentType())) {
            if (!"COMPLETED".equals(commissionStatus)) {
                throw new IllegalArgumentException("尾款售后仅支持在订单完成后发起");
            }
            return;
        }
        throw new IllegalArgumentException("当前支付类型不支持售后退款审核");
    }

    private CommissionSnapshot getCommissionSnapshot(Long userId, Long commissionId) {
        Result<CommissionSnapshot> result = commissionServiceClient.getCommission(userId, commissionId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new IllegalArgumentException(result != null ? result.getMessage() : "约稿不存在或不可访问");
        }
        return result.getData();
    }

    private List<PaymentSnapshot> getPaymentSnapshots(Long userId, Long commissionId) {
        Result<List<PaymentSnapshot>> result = commissionServiceClient.getCommissionPayments(userId, commissionId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return Collections.emptyList();
        }
        return result.getData();
    }

    private PaymentSnapshot findPayment(List<PaymentSnapshot> payments, Long paymentId) {
        return payments.stream()
                .filter(item -> Objects.equals(item.getId(), paymentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到关联的支付记录"));
    }

    private String normalizeRequestedAction(String requestedAction, Long paymentId) {
        if (requestedAction == null || requestedAction.isBlank()) {
            return paymentId != null ? ACTION_REFUND_REVIEW : ACTION_PLATFORM_INTERVENTION;
        }
        String action = requestedAction.trim().toUpperCase(Locale.ROOT);
        if (!ACTION_PLATFORM_INTERVENTION.equals(action) && !ACTION_REFUND_REVIEW.equals(action)) {
            throw new IllegalArgumentException("无效的售后申请动作");
        }
        return action;
    }

    private String normalizeRefundMode(Feedback feedback, String refundMode) {
        if ("FINAL_PAYMENT".equals(feedback.getPaymentType()) && MODE_FINAL_ONLY.equalsIgnoreCase(refundMode)) {
            return MODE_FINAL_ONLY;
        }
        return MODE_FULL;
    }

    private String buildAfterSaleTitle(String title, CommissionSnapshot commission, PaymentSnapshot payment, String action) {
        if (title != null && !title.trim().isEmpty()) {
            return title.trim();
        }
        if (ACTION_PLATFORM_INTERVENTION.equals(action)) {
            return "约稿平台介入申请 #" + commission.getId() + " - " + commission.getTitle();
        }
        String paymentLabel = payment != null && "FINAL_PAYMENT".equals(payment.getPaymentType()) ? "尾款" : "定金";
        return "约稿售后申请 #" + commission.getId() + " - " + paymentLabel + "退款审核";
    }

    private String buildAdminReason(Feedback feedback, String reply, String mode) {
        StringBuilder builder = new StringBuilder("售后单#").append(feedback.getId()).append(" 审核通过");
        if (MODE_FINAL_ONLY.equals(mode)) {
            builder.append("，退还尾款");
        } else {
            builder.append("，取消约稿并退款");
        }
        if (reply != null && !reply.trim().isEmpty()) {
            builder.append("：").append(reply.trim());
        }
        return builder.toString();
    }

    private void notifyAfterSaleRefund(Feedback feedback, String refundMode) {
        String mode = normalizeRefundMode(feedback, refundMode);
        if (MODE_FINAL_ONLY.equals(mode)) {
            sendSystemNotice(feedback.getUserId(),
                    "你的售后申请已通过，平台已退还尾款，请留意支付记录变化。",
                    "/orders");
            if (feedback.getCounterpartyUserId() != null) {
                sendSystemNotice(feedback.getCounterpartyUserId(),
                        "约稿相关售后已处理，平台已执行尾款退款，请关注订单状态。",
                        "/commission/" + feedback.getCommissionId());
            }
            return;
        }

        sendSystemNotice(feedback.getUserId(),
                "你的售后申请已通过，平台已取消约稿并执行退款。",
                "/commission/" + feedback.getCommissionId());
    }

    private void sendSystemNotice(Long userId, String content, String linkUrl) {
        if (userId == null) {
            return;
        }
        try {
            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setUserId(userId);
            request.setType("SYSTEM");
            request.setContent(content);
            request.setLinkUrl(linkUrl);
            notificationService.createNotification(request);
        } catch (Exception e) {
            logger.warn("发送系统通知失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    private Map<String, Object> buildPageResult(Page<Feedback> pageResult, int page, int size) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (Feedback f : pageResult.getContent()) {
            records.add(convertToMap(f));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", pageResult.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", pageResult.getTotalPages());
        return result;
    }

    private Map<String, Object> convertToMap(Feedback f) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", f.getId());
        map.put("userId", f.getUserId());
        map.put("type", f.getType());
        map.put("title", f.getTitle());
        map.put("content", f.getContent());
        map.put("contactInfo", f.getContactInfo());
        map.put("commissionId", f.getCommissionId());
        map.put("paymentId", f.getPaymentId());
        map.put("paymentOrderNo", f.getPaymentOrderNo());
        map.put("paymentType", f.getPaymentType());
        map.put("paymentAmount", f.getPaymentAmount());
        map.put("requestedAction", f.getRequestedAction());
        map.put("applicantRole", f.getApplicantRole());
        map.put("counterpartyUserId", f.getCounterpartyUserId());
        map.put("status", f.getStatus());
        map.put("resolution", f.getResolution());
        map.put("adminReply", f.getAdminReply());
        map.put("repliedAt", f.getRepliedAt());
        map.put("handledByAdminId", f.getHandledByAdminId());
        map.put("resolvedAt", f.getResolvedAt());
        map.put("createdAt", f.getCreatedAt());
        map.put("updatedAt", f.getUpdatedAt());
        return map;
    }
}
