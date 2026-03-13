package com.pixiv.commission.service;

import com.pixiv.commission.dto.*;
import com.pixiv.commission.entity.Conversation;
import com.pixiv.commission.entity.MessageType;
import com.pixiv.commission.entity.PrivateMessage;
import com.pixiv.commission.feign.UserServiceClient;
import com.pixiv.commission.repository.ConversationRepository;
import com.pixiv.commission.repository.PrivateMessageRepository;
import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 私信/对话服务
 */
@Service
public class ConversationService {

    private static final Logger logger = LoggerFactory.getLogger(ConversationService.class);
    private static final String CHAT_MESSAGE_QUEUE = "chat.message";

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private PrivateMessageRepository privateMessageRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建或获取与目标用户的对话
     */
    @Transactional
    public ConversationDTO createOrGetConversation(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("不能与自己创建对话");
        }

        // 查找已有对话
        return conversationRepository.findByTwoUsers(currentUserId, targetUserId)
                .map(conv -> toDTO(conv, currentUserId))
                .orElseGet(() -> {
                    // 验证目标用户存在
                    UserDTO targetUser = fetchUser(targetUserId);
                    if (targetUser == null) {
                        throw new IllegalArgumentException("目标用户不存在");
                    }

                    // 保证 user1Id < user2Id 维持唯一约束
                    Long u1 = Math.min(currentUserId, targetUserId);
                    Long u2 = Math.max(currentUserId, targetUserId);

                    Conversation conversation = Conversation.builder()
                            .user1Id(u1)
                            .user2Id(u2)
                            .user1Unread(0)
                            .user2Unread(0)
                            .createdAt(LocalDateTime.now())
                            .build();
                    conversation = conversationRepository.save(conversation);
                    logger.info("创建新对话: id={}, u1={}, u2={}", conversation.getId(), u1, u2);
                    return toDTO(conversation, currentUserId);
                });
    }

    /**
     * 获取当前用户的所有对话列表
     */
    public List<ConversationDTO> getMyConversations(Long userId) {
        return conversationRepository.findByUserId(userId).stream()
                .map(conv -> toDTO(conv, userId))
                .collect(Collectors.toList());
    }

    /**
     * 获取对话详情
     */
    public ConversationDTO getConversation(Long userId, Long conversationId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("对话不存在"));
        if (!conv.getUser1Id().equals(userId) && !conv.getUser2Id().equals(userId)) {
            throw new IllegalArgumentException("无权查看此对话");
        }
        return toDTO(conv, userId);
    }

    /**
     * 获取对话中的消息（分页，倒序）
     */
    public Page<PrivateMessageDTO> getMessages(Long userId, Long conversationId, int page, int size) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("对话不存在"));
        if (!conv.getUser1Id().equals(userId) && !conv.getUser2Id().equals(userId)) {
            throw new IllegalArgumentException("无权查看此对话消息");
        }
        return privateMessageRepository
                .findByConversationIdOrderByCreatedAtDesc(conversationId, PageRequest.of(page, size))
                .map(this::toMessageDTO);
    }

    /**
     * 发送私信
     */
    @Transactional
    public PrivateMessageDTO sendMessage(Long senderId, Long conversationId, SendPrivateMessageRequest request) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("对话不存在"));

        if (!conv.getUser1Id().equals(senderId) && !conv.getUser2Id().equals(senderId)) {
            throw new IllegalArgumentException("无权在此对话中发送消息");
        }

        MessageType msgType = MessageType.TEXT;
        if (request.getMessageType() != null) {
            try {
                msgType = MessageType.valueOf(request.getMessageType().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // 默认 TEXT
            }
        }

        PrivateMessage message = PrivateMessage.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .content(request.getContent())
                .messageType(msgType)
                .attachmentUrl(request.getAttachmentUrl())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        message = privateMessageRepository.save(message);

        // 更新对话的最后消息和未读数
        conv.setLastMessage(request.getContent());
        conv.setLastMessageAt(LocalDateTime.now());
        if (conv.getUser1Id().equals(senderId)) {
            conv.setUser2Unread(conv.getUser2Unread() + 1);
        } else {
            conv.setUser1Unread(conv.getUser1Unread() + 1);
        }
        conversationRepository.save(conv);

        logger.info("发送私信: convId={}, senderId={}, msgId={}", conversationId, senderId, message.getId());

        // 通过 RabbitMQ 推送给接收方，notification-service 消费后通过 WebSocket 实时推送
        Long recipientId = conv.getUser1Id().equals(senderId) ? conv.getUser2Id() : conv.getUser1Id();
        PrivateMessageDTO msgDTO = toMessageDTO(message);
        pushChatMessageEvent(recipientId, msgDTO);

        return msgDTO;
    }

    /**
     * 标记对话中的消息为已读
     */
    @Transactional
    public void markAsRead(Long userId, Long conversationId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("对话不存在"));

        if (!conv.getUser1Id().equals(userId) && !conv.getUser2Id().equals(userId)) {
            throw new IllegalArgumentException("无权操作此对话");
        }

        privateMessageRepository.markAllAsRead(conversationId, userId);
        if (conv.getUser1Id().equals(userId)) {
            conv.setUser1Unread(0);
        } else {
            conv.setUser2Unread(0);
        }
        conversationRepository.save(conv);
    }

    // ==================== Private Helpers ====================

    private ConversationDTO toDTO(Conversation conv, Long currentUserId) {
        Long otherUserId = conv.getUser1Id().equals(currentUserId)
                ? conv.getUser2Id()
                : conv.getUser1Id();
        int unread = conv.getUser1Id().equals(currentUserId)
                ? conv.getUser1Unread()
                : conv.getUser2Unread();

        UserDTO otherUser = fetchUser(otherUserId);
        String otherName = otherUser != null ? otherUser.getUsername() : "用户" + otherUserId;
        String otherAvatar = otherUser != null ? otherUser.getAvatarUrl() : null;

        return ConversationDTO.builder()
                .id(conv.getId())
                .otherUserId(otherUserId)
                .otherUserName(otherName)
                .otherUserAvatar(otherAvatar)
                .lastMessage(conv.getLastMessage())
                .lastMessageAt(conv.getLastMessageAt())
                .unreadCount(unread)
                .createdAt(conv.getCreatedAt())
                .build();
    }

    private PrivateMessageDTO toMessageDTO(PrivateMessage msg) {
        UserDTO sender = fetchUser(msg.getSenderId());
        return PrivateMessageDTO.builder()
                .id(msg.getId())
                .conversationId(msg.getConversationId())
                .senderId(msg.getSenderId())
                .senderName(sender != null ? sender.getUsername() : "用户" + msg.getSenderId())
                .senderAvatar(sender != null ? sender.getAvatarUrl() : null)
                .content(msg.getContent())
                .messageType(msg.getMessageType())
                .attachmentUrl(msg.getAttachmentUrl())
                .isRead(msg.getIsRead())
                .createdAt(msg.getCreatedAt())
                .build();
    }

    private UserDTO fetchUser(Long userId) {
        try {
            Result<UserDTO> result = userServiceClient.getUserById(userId);
            return result != null ? result.getData() : null;
        } catch (Exception e) {
            logger.warn("获取用户信息失败: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 通过 RabbitMQ 发送私信事件，由 notification-service 消费并通过 WebSocket 推送
     */
    private void pushChatMessageEvent(Long recipientId, PrivateMessageDTO msgDTO) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("recipientId", recipientId);
            event.put("id", msgDTO.getId());
            event.put("conversationId", msgDTO.getConversationId());
            event.put("senderId", msgDTO.getSenderId());
            event.put("senderName", msgDTO.getSenderName());
            event.put("senderAvatar", msgDTO.getSenderAvatar());
            event.put("content", msgDTO.getContent());
            event.put("messageType", msgDTO.getMessageType() != null ? msgDTO.getMessageType().name() : "TEXT");
            event.put("attachmentUrl", msgDTO.getAttachmentUrl());
            event.put("createdAt", msgDTO.getCreatedAt() != null ? msgDTO.getCreatedAt().toString() : null);
            rabbitTemplate.convertAndSend(CHAT_MESSAGE_QUEUE, event);
        } catch (Exception e) {
            logger.warn("推送私信事件失败: recipientId={}, error={}", recipientId, e.getMessage());
        }
    }
}
