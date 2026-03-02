package com.pixiv.notification.service;

import com.pixiv.common.dto.PageResult;
import com.pixiv.notification.dto.CreateNotificationRequest;
import com.pixiv.notification.dto.NotificationDTO;
import com.pixiv.notification.entity.Notification;
import com.pixiv.notification.entity.NotificationType;
import com.pixiv.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知服务
 * 
 * 处理通知的创建、查询、标记已读等业务逻辑
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 创建通知
     * 
     * @param request 创建通知请求
     * @return 通知 DTO
     */
    @Transactional
    public NotificationDTO createNotification(CreateNotificationRequest request) {
        logger.info("创建通知: userId={}, type={}, content={}",
                request.getUserId(), request.getType(), request.getContent());

        try {
            // 验证通知类型
            NotificationType type;
            try {
                type = NotificationType.valueOf(request.getType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的通知类型: " + request.getType());
            }

            // 创建通知实体
            Notification notification = new Notification();
            notification.setUserId(request.getUserId());
            notification.setType(type);
            notification.setContent(request.getContent());
            notification.setLinkUrl(request.getLinkUrl());
            notification.setIsRead(false);

            // 保存到数据库
            notification = notificationRepository.save(notification);

            logger.info("通知创建成功: notificationId={}", notification.getId());

            NotificationDTO dto = convertToDTO(notification);

            // 通过 WebSocket 实时推送给在线用户
            pushNotificationViaWebSocket(dto);

            return dto;
        } catch (Exception e) {
            logger.error("创建通知失败: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取用户的通知列表（分页）
     * 
     * @param userId 用户 ID
     * @param page   页码（从 1 开始）
     * @param size   每页大小
     * @return 通知分页结果
     */
    public PageResult<NotificationDTO> getNotifications(Long userId, int page, int size) {
        logger.info("查询通知列表: userId={}, page={}, size={}", userId, page, size);

        try {
            // 创建分页参数（Spring Data JPA 的页码从 0 开始）
            Pageable pageable = PageRequest.of(page - 1, size);

            // 查询通知列表
            Page<Notification> notificationPage = notificationRepository
                    .findByUserIdOrderByCreatedAtDesc(userId, pageable);

            // 转换为 DTO
            Page<NotificationDTO> dtoPage = notificationPage.map(this::convertToDTO);

            logger.info("查询通知列表成功: userId={}, total={}", userId, dtoPage.getTotalElements());

            return new PageResult<>(
                    dtoPage.getContent(),
                    dtoPage.getTotalElements(),
                    page,
                    size);
        } catch (Exception e) {
            logger.error("查询通知列表失败: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取用户的未读通知数量
     * 
     * @param userId 用户 ID
     * @return 未读通知数量
     */
    public long getUnreadCount(Long userId) {
        logger.info("查询未读通知数量: userId={}", userId);

        try {
            long count = notificationRepository.countUnreadByUserId(userId);
            logger.info("查询未读通知数量成功: userId={}, count={}", userId, count);
            return count;
        } catch (Exception e) {
            logger.error("查询未读通知数量失败: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 标记通知为已读
     * 
     * @param notificationId 通知 ID
     * @param userId         用户 ID（用于验证权限）
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        logger.info("标记通知为已读: notificationId={}, userId={}", notificationId, userId);

        try {
            // 查询通知
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new IllegalArgumentException("通知不存在"));

            // 验证权限
            if (!notification.getUserId().equals(userId)) {
                throw new IllegalArgumentException("无权操作此通知");
            }

            // 标记为已读
            notification.setIsRead(true);
            notificationRepository.save(notification);

            logger.info("标记通知为已读成功: notificationId={}", notificationId);
        } catch (Exception e) {
            logger.error("标记通知为已读失败: notificationId={}, error={}", notificationId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 标记所有通知为已读
     * 
     * @param userId 用户 ID
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        logger.info("标记所有通知为已读: userId={}", userId);

        try {
            int count = notificationRepository.markAllAsReadByUserId(userId);
            logger.info("标记所有通知为已读成功: userId={}, count={}", userId, count);
        } catch (Exception e) {
            logger.error("标记所有通知为已读失败: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 删除通知
     * 
     * @param notificationId 通知 ID
     * @param userId         用户 ID（用于验证权限）
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        logger.info("删除通知: notificationId={}, userId={}", notificationId, userId);

        try {
            // 查询通知
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new IllegalArgumentException("通知不存在"));

            // 验证权限
            if (!notification.getUserId().equals(userId)) {
                throw new IllegalArgumentException("无权操作此通知");
            }

            // 删除通知
            notificationRepository.delete(notification);

            logger.info("删除通知成功: notificationId={}", notificationId);
        } catch (Exception e) {
            logger.error("删除通知失败: notificationId={}, error={}", notificationId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 将 Notification 实体转换为 DTO
     * 
     * @param notification 通知实体
     * @return 通知 DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setType(notification.getType().name());
        dto.setContent(notification.getContent());
        dto.setLinkUrl(notification.getLinkUrl());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    /**
     * 通过 WebSocket 实时推送通知给在线用户
     *
     * @param dto 通知 DTO
     */
    private void pushNotificationViaWebSocket(NotificationDTO dto) {
        if (messagingTemplate == null) {
            return;
        }
        try {
            String destination = "/topic/notifications/" + dto.getUserId();
            messagingTemplate.convertAndSend(destination, dto);
            logger.debug("WebSocket 推送通知成功: userId={}, destination={}", dto.getUserId(), destination);
        } catch (Exception e) {
            logger.warn("WebSocket 推送通知失败: userId={}, error={}", dto.getUserId(), e.getMessage());
            // 推送失败不影响通知创建，用户仍可通过轮询获取
        }
    }
}
