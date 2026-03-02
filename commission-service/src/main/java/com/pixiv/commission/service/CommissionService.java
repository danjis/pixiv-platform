package com.pixiv.commission.service;

import com.pixiv.commission.dto.*;
import com.pixiv.commission.entity.Commission;
import com.pixiv.commission.entity.CommissionMessage;
import com.pixiv.commission.entity.CommissionStatus;
import com.pixiv.commission.feign.UserServiceClient;
import com.pixiv.commission.repository.CommissionMessageRepository;
import com.pixiv.commission.repository.CommissionRepository;
import com.pixiv.common.constant.ErrorCode;
import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UserDTO;
import com.pixiv.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 约稿服务（新流程 - 用户发起）
 *
 * 核心流程:
 * 用户发起约稿请求(PENDING) → 画师报价(QUOTED) → 用户支付定金(DEPOSIT_PAID)
 * → 画师开始创作(IN_PROGRESS) → 画师交付(DELIVERED)
 * → 用户确认并支付尾款(COMPLETED) / 请求修改(回到 IN_PROGRESS)
 */
@Service
public class CommissionService {

    private static final Logger logger = LoggerFactory.getLogger(CommissionService.class);

    @Autowired
    private CommissionRepository commissionRepository;

    @Autowired
    private CommissionMessageRepository commissionMessageRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String NOTIFICATION_QUEUE = "notification.create";

    // ===================== 创建约稿请求（用户发起） =====================

    /**
     * 用户向画师发起约稿请求
     *
     * @param clientId 创建者（当前用户，即委托方）
     * @param request  请求参数
     * @return 约稿 DTO
     */
    @Transactional
    public CommissionDTO createCommission(Long clientId, CreateCommissionRequest request) {
        Long artistId = request.getTargetUserId();
        if (clientId.equals(artistId)) {
            throw new BusinessException(ErrorCode.CANNOT_COMMISSION_SELF, "不能向自己发起约稿");
        }

        // 验证画师存在
        UserDTO artist = fetchUser(artistId);
        if (artist == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "目标画师不存在");
        }

        // 创建约稿：用户为委托方，目标为画师，状态为 PENDING
        // totalAmount 在画师报价(QUOTED)阶段才确定，创建时默认为 0
        Commission commission = Commission.builder()
                .clientId(clientId)
                .artistId(artistId)
                .conversationId(request.getConversationId())
                .title(request.getTitle())
                .description(request.getDescription())
                .budget(request.getBudget())
                .totalAmount(BigDecimal.ZERO)
                .depositAmount(BigDecimal.ZERO)
                .referenceUrls(request.getReferenceUrls())
                .commissionPlanId(request.getCommissionPlanId())
                .depositPaid(false)
                .finalPaid(false)
                .deadline(request.getDeadline())
                .revisionsAllowed(3)
                .revisionsUsed(0)
                .status(CommissionStatus.PENDING)
                .build();

        commission = commissionRepository.save(commission);
        logger.info("约稿请求已创建: id={}, client={}, artist={}", commission.getId(), clientId, artistId);

        // 通知画师有新的约稿请求
        sendNotification(artistId, "COMMISSION_REQUEST",
                "您收到了一个新的约稿请求：" + commission.getTitle(),
                "/commissions/" + commission.getId());

        return toDTO(commission);
    }

    // ===================== 查询 =====================

    /**
     * 获取约稿详情
     */
    public CommissionDTO getCommission(Long userId, Long commissionId) {
        Commission c = findAndCheckAccess(commissionId, userId);
        return toDTO(c);
    }

    /**
     * 获取我的约稿列表（作为委托方或画师）
     */
    public Page<CommissionDTO> getMyCommissions(Long userId, String role, CommissionStatus status,
            int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Commission> commissions;

        if ("artist".equalsIgnoreCase(role)) {
            commissions = status != null
                    ? commissionRepository.findByArtistIdAndStatus(userId, status, pageable)
                    : commissionRepository.findByArtistId(userId, pageable);
        } else {
            commissions = status != null
                    ? commissionRepository.findByClientIdAndStatus(userId, status, pageable)
                    : commissionRepository.findByClientId(userId, pageable);
        }
        return commissions.map(this::toDTO);
    }

    // ===================== 画师报价 =====================

    /**
     * 画师报价 → PENDING → QUOTED
     */
    @Transactional
    public CommissionDTO quoteCommission(Long artistId, Long commissionId, QuoteCommissionRequest request) {
        Commission c = findAndCheckAccess(commissionId, artistId);
        checkStatus(c, CommissionStatus.PENDING);

        if (!c.getArtistId().equals(artistId)) {
            throw new BusinessException(ErrorCode.COMMISSION_STATUS_ERROR, "只有画师才能报价");
        }

        // 计算定金
        BigDecimal depositRatio = request.getDepositRatio() != null
                ? request.getDepositRatio()
                : new BigDecimal("0.30");
        BigDecimal depositAmount = request.getTotalAmount()
                .multiply(depositRatio)
                .setScale(2, RoundingMode.HALF_UP);

        c.setTotalAmount(request.getTotalAmount());
        c.setDepositAmount(depositAmount);
        c.setRevisionsAllowed(request.getRevisionsAllowed() != null ? request.getRevisionsAllowed() : 3);
        c.setQuoteNote(request.getQuoteNote());
        if (request.getDeadline() != null) {
            c.setDeadline(request.getDeadline());
        }
        c.setStatus(CommissionStatus.QUOTED);
        c = commissionRepository.save(c);

        logger.info("画师已报价: id={}, artist={}, amount={}, deposit={}",
                commissionId, artistId, request.getTotalAmount(), depositAmount);

        // 通知委托方画师已报价
        sendNotification(c.getClientId(), "COMMISSION_REQUEST",
                "您的约稿「" + c.getTitle() + "」已收到画师报价：¥" + request.getTotalAmount(),
                "/commissions/" + commissionId);

        return toDTO(c);
    }

    // ===================== 状态变更 =====================

    /**
     * 委托方接受报价（定金支付通过 PaymentService 处理）
     * 此方法保留作为兼容，实际支付由 PaymentService.handleAlipayNotify 回调自动更新状态
     */
    @Transactional
    public CommissionDTO acceptAndPayDeposit(Long clientId, Long commissionId) {
        Commission c = findAndCheckAccess(commissionId, clientId);
        // 支持从 PENDING 或 QUOTED 状态接受
        if (c.getStatus() != CommissionStatus.PENDING && c.getStatus() != CommissionStatus.QUOTED) {
            throw new BusinessException(ErrorCode.COMMISSION_STATUS_ERROR, "当前状态不允许此操作: " + c.getStatus());
        }

        if (!c.getClientId().equals(clientId)) {
            throw new BusinessException(ErrorCode.NOT_COMMISSION_PARTICIPANT, "只有委托方才能接受方案");
        }

        c.setDepositPaid(true);
        c.setStatus(CommissionStatus.DEPOSIT_PAID);
        c = commissionRepository.save(c);

        logger.info("约稿已接受: id={}, client={}", commissionId, clientId);
        return toDTO(c);
    }

    /**
     * 画师拒绝约稿请求 → REJECTED
     */
    @Transactional
    public CommissionDTO rejectCommission(Long artistId, Long commissionId, String reason) {
        Commission c = findAndCheckAccess(commissionId, artistId);
        if (c.getStatus() != CommissionStatus.PENDING && c.getStatus() != CommissionStatus.QUOTED) {
            throw new BusinessException(ErrorCode.COMMISSION_STATUS_ERROR, "当前状态不允许拒绝: " + c.getStatus());
        }

        if (!c.getArtistId().equals(artistId)) {
            throw new BusinessException(ErrorCode.NOT_COMMISSION_PARTICIPANT, "只有画师才能拒绝约稿请求");
        }

        c.setStatus(CommissionStatus.REJECTED);
        c.setCancelReason(reason);
        c = commissionRepository.save(c);

        logger.info("约稿已拒绝: id={}, artist={}", commissionId, artistId);

        // 通知委托方约稿被拒绝
        sendNotification(c.getClientId(), "COMMISSION_REJECTED",
                "您的约稿「" + c.getTitle() + "」已被画师拒绝" + (reason != null ? "，原因：" + reason : ""),
                "/commissions/" + commissionId);

        return toDTO(c);
    }

    /**
     * 画师开始创作 → IN_PROGRESS
     */
    @Transactional
    public CommissionDTO startWork(Long artistId, Long commissionId) {
        Commission c = findAndCheckAccess(commissionId, artistId);
        checkStatus(c, CommissionStatus.DEPOSIT_PAID);

        if (!c.getArtistId().equals(artistId)) {
            throw new BusinessException(ErrorCode.NOT_COMMISSION_PARTICIPANT, "只有画师才能开始创作");
        }

        c.setStatus(CommissionStatus.IN_PROGRESS);
        c = commissionRepository.save(c);

        logger.info("画师开始创作: id={}, artist={}", commissionId, artistId);
        return toDTO(c);
    }

    /**
     * 画师交付作品 → DELIVERED
     */
    @Transactional
    public CommissionDTO deliverWork(Long artistId, Long commissionId, DeliverWorkRequest request) {
        Commission c = findAndCheckAccess(commissionId, artistId);
        if (c.getStatus() != CommissionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.COMMISSION_STATUS_ERROR, "当前状态不允许交付");
        }
        if (!c.getArtistId().equals(artistId)) {
            throw new BusinessException(ErrorCode.NOT_COMMISSION_PARTICIPANT, "只有画师才能交付作品");
        }

        c.setDeliveryUrl(request.getDeliveryUrl());
        c.setDeliveryNote(request.getDeliveryNote());
        c.setDeliveredAt(LocalDateTime.now());
        c.setStatus(CommissionStatus.DELIVERED);
        c = commissionRepository.save(c);

        logger.info("作品已交付: id={}, artist={}", commissionId, artistId);

        // 通知委托方作品已交付
        sendNotification(c.getClientId(), "COMMISSION_COMPLETED",
                "您的约稿「" + c.getTitle() + "」已交付，请查收",
                "/commissions/" + commissionId);

        return toDTO(c);
    }

    /**
     * 委托方确认收货（尾款支付通过 PaymentService 处理）
     * 此方法保留作为兼容，实际尾款由 PaymentService.handleAlipayNotify 回调自动更新
     */
    @Transactional
    public CommissionDTO confirmDelivery(Long clientId, Long commissionId) {
        Commission c = findAndCheckAccess(commissionId, clientId);
        checkStatus(c, CommissionStatus.DELIVERED);

        if (!c.getClientId().equals(clientId)) {
            throw new BusinessException(ErrorCode.NOT_COMMISSION_PARTICIPANT, "只有委托方才能确认收货");
        }

        c.setFinalPaid(true);
        c.setStatus(CommissionStatus.COMPLETED);
        c = commissionRepository.save(c);

        logger.info("约稿已完成: id={}, client={}", commissionId, clientId);

        // 通知画师约稿已完成
        sendNotification(c.getArtistId(), "COMMISSION_COMPLETED",
                "约稿「" + c.getTitle() + "」已完成，尾款已支付",
                "/commissions/" + commissionId);

        return toDTO(c);
    }

    /**
     * 委托方请求修改（消耗修改次数）→ 回到 IN_PROGRESS
     */
    @Transactional
    public CommissionDTO requestRevision(Long clientId, Long commissionId) {
        Commission c = findAndCheckAccess(commissionId, clientId);
        checkStatus(c, CommissionStatus.DELIVERED);

        if (!c.getClientId().equals(clientId)) {
            throw new BusinessException(ErrorCode.NOT_COMMISSION_PARTICIPANT, "只有委托方才能请求修改");
        }
        if (c.getRevisionsUsed() >= c.getRevisionsAllowed()) {
            throw new BusinessException(ErrorCode.COMMISSION_STATUS_ERROR,
                    "已达最大修改次数(" + c.getRevisionsAllowed() + "次)");
        }

        c.setRevisionsUsed(c.getRevisionsUsed() + 1);
        c.setStatus(CommissionStatus.IN_PROGRESS);
        c = commissionRepository.save(c);

        logger.info("请求修改: id={}, revisionsUsed={}/{}", commissionId, c.getRevisionsUsed(), c.getRevisionsAllowed());
        return toDTO(c);
    }

    /**
     * 取消约稿 → CANCELLED
     */
    @Transactional
    public CommissionDTO cancelCommission(Long userId, Long commissionId, String reason) {
        Commission c = findAndCheckAccess(commissionId, userId);

        if (c.getStatus() == CommissionStatus.COMPLETED || c.getStatus() == CommissionStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.COMMISSION_STATUS_ERROR, "已完成或已取消的约稿无法再取消");
        }

        // 业务规则：定金不退款。约稿取消后，已支付的定金不予退还，
        // 这是约稿平台的标准商业实践，定金作为画师时间成本的补偿。
        c.setStatus(CommissionStatus.CANCELLED);
        c.setCancelReason(reason);
        c = commissionRepository.save(c);

        logger.info("约稿已取消: id={}, user={}, reason={}", commissionId, userId, reason);

        // 通知对方约稿已取消
        Long otherUserId = userId.equals(c.getClientId()) ? c.getArtistId() : c.getClientId();
        sendNotification(otherUserId, "COMMISSION_REJECTED",
                "约稿「" + c.getTitle() + "」已被取消" + (reason != null ? "，原因：" + reason : ""),
                "/commissions/" + commissionId);

        return toDTO(c);
    }

    // ===================== 约稿内消息 =====================

    public List<CommissionMessageDTO> getMessages(Long userId, Long commissionId) {
        findAndCheckAccess(commissionId, userId);
        return commissionMessageRepository.findByCommissionIdOrderByCreatedAtAsc(commissionId)
                .stream()
                .map(this::toMessageDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommissionMessageDTO sendMessage(Long senderId, Long commissionId, SendMessageRequest request) {
        findAndCheckAccess(commissionId, senderId);
        CommissionMessage message = new CommissionMessage();
        message.setCommissionId(commissionId);
        message.setSenderId(senderId);
        message.setContent(request.getContent());
        message.setAttachmentUrl(request.getAttachmentUrl());
        message.setCreatedAt(LocalDateTime.now());
        message = commissionMessageRepository.save(message);
        return toMessageDTO(message);
    }

    // ===================== Private Helpers =====================

    private Commission findAndCheckAccess(Long commissionId, Long userId) {
        Commission c = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMISSION_NOT_FOUND, "约稿不存在: " + commissionId));
        if (!c.getClientId().equals(userId) && !c.getArtistId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_COMMISSION_PARTICIPANT, "无权操作此约稿");
        }
        return c;
    }

    private void checkStatus(Commission c, CommissionStatus expected) {
        if (c.getStatus() != expected) {
            throw new BusinessException(ErrorCode.COMMISSION_STATUS_ERROR,
                    String.format("当前状态 %s 不允许此操作，需要状态: %s", c.getStatus(), expected));
        }
    }

    private CommissionDTO toDTO(Commission c) {
        UserDTO client = fetchUser(c.getClientId());
        UserDTO artist = fetchUser(c.getArtistId());

        return CommissionDTO.builder()
                .id(c.getId())
                .clientId(c.getClientId())
                .clientName(client != null ? client.getUsername() : "用户" + c.getClientId())
                .clientAvatar(client != null ? client.getAvatarUrl() : null)
                .artistId(c.getArtistId())
                .artistName(artist != null ? artist.getUsername() : "用户" + c.getArtistId())
                .artistAvatar(artist != null ? artist.getAvatarUrl() : null)
                .conversationId(c.getConversationId())
                .title(c.getTitle())
                .description(c.getDescription())
                .totalAmount(c.getTotalAmount())
                .depositAmount(c.getDepositAmount())
                .depositPaid(c.getDepositPaid())
                .finalPaid(c.getFinalPaid())
                .budget(c.getBudget())
                .referenceUrls(c.getReferenceUrls())
                .quoteNote(c.getQuoteNote())
                .commissionPlanId(c.getCommissionPlanId())
                .deadline(c.getDeadline())
                .revisionsAllowed(c.getRevisionsAllowed())
                .revisionsUsed(c.getRevisionsUsed())
                .deliveryUrl(c.getDeliveryUrl())
                .deliveryNote(c.getDeliveryNote())
                .deliveredAt(c.getDeliveredAt())
                .cancelReason(c.getCancelReason())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private CommissionMessageDTO toMessageDTO(CommissionMessage msg) {
        UserDTO sender = fetchUser(msg.getSenderId());
        CommissionMessageDTO dto = new CommissionMessageDTO();
        dto.setId(msg.getId());
        dto.setCommissionId(msg.getCommissionId());
        dto.setSenderId(msg.getSenderId());
        dto.setSenderName(sender != null ? sender.getUsername() : "用户" + msg.getSenderId());
        dto.setSenderAvatar(sender != null ? sender.getAvatarUrl() : null);
        dto.setContent(msg.getContent());
        dto.setAttachmentUrl(msg.getAttachmentUrl());
        dto.setCreatedAt(msg.getCreatedAt());
        return dto;
    }

    private UserDTO fetchUser(Long userId) {
        try {
            Result<UserDTO> result = userServiceClient.getUserById(userId);
            return result != null ? result.getData() : null;
        } catch (Exception e) {
            logger.warn("获取用户信息失败: userId={}", userId);
            return null;
        }
    }

    /**
     * 发送通知消息到 RabbitMQ
     */
    private void sendNotification(Long userId, String type, String content, String linkUrl) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("type", type);
            message.put("content", content);
            message.put("linkUrl", linkUrl);
            rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            logger.warn("发送通知失败: userId={}, type={}, error={}", userId, type, e.getMessage());
        }
    }
}
