package com.pixiv.commission.repository;

import com.pixiv.commission.entity.PrivateMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {

    /** 分页查询对话中的消息，按时间倒序 */
    Page<PrivateMessage> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    /** 统计某对话中某用户的未读消息数 */
    @Query("SELECT COUNT(m) FROM PrivateMessage m WHERE m.conversationId = :convId AND m.senderId <> :userId AND m.isRead = false")
    long countUnread(@Param("convId") Long convId, @Param("userId") Long userId);

    /** 标记对话中他人发来的消息为已读 */
    @Modifying
    @Query("UPDATE PrivateMessage m SET m.isRead = true WHERE m.conversationId = :convId AND m.senderId <> :userId AND m.isRead = false")
    void markAllAsRead(@Param("convId") Long convId, @Param("userId") Long userId);
}
