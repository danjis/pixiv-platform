package com.pixiv.commission.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 私信对话实体
 *
 * 表示两个用户之间的一对一对话
 */
@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_conv_user1", columnList = "user1_id"),
        @Index(name = "idx_conv_user2", columnList = "user2_id"),
        @Index(name = "idx_conv_last_msg", columnList = "last_message_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_conv_users", columnNames = { "user1_id", "user2_id" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户1 ID（较小的 ID） */
    @Column(name = "user1_id", nullable = false)
    private Long user1Id;

    /** 用户2 ID（较大的 ID） */
    @Column(name = "user2_id", nullable = false)
    private Long user2Id;

    /** 最后一条消息预览 */
    @Column(name = "last_message", length = 200)
    private String lastMessage;

    /** 最后消息时间 */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /** 用户1 的未读消息数 */
    @Column(name = "user1_unread", nullable = false)
    @Builder.Default
    private Integer user1Unread = 0;

    /** 用户2 的未读消息数 */
    @Column(name = "user2_unread", nullable = false)
    @Builder.Default
    private Integer user2Unread = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (lastMessageAt == null)
            lastMessageAt = createdAt;
    }
}
