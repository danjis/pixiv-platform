package com.pixiv.commission.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 私信消息实体
 */
@Entity
@Table(name = "private_messages", indexes = {
        @Index(name = "idx_pm_conversation", columnList = "conversation_id"),
        @Index(name = "idx_pm_sender", columnList = "sender_id"),
        @Index(name = "idx_pm_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 对话 ID */
    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    /** 发送者 ID */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /** 消息内容 */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /** 消息类型 */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 30, nullable = false)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    /** 附件 URL（图片消息时使用） */
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    /** 是否已读 */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
