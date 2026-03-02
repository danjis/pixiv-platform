package com.pixiv.commission.repository;

import com.pixiv.commission.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /** 查找用户参与的所有对话，按最后消息时间倒序 */
    @Query("SELECT c FROM Conversation c WHERE c.user1Id = :userId OR c.user2Id = :userId ORDER BY c.lastMessageAt DESC NULLS LAST")
    List<Conversation> findByUserId(@Param("userId") Long userId);

    /** 查找两个用户之间的对话（user1 < user2 保证唯一性） */
    @Query("SELECT c FROM Conversation c WHERE (c.user1Id = :u1 AND c.user2Id = :u2) OR (c.user1Id = :u2 AND c.user2Id = :u1)")
    Optional<Conversation> findByTwoUsers(@Param("u1") Long u1, @Param("u2") Long u2);

    /** 重置某个用户在某对话中的未读数 */
    @Modifying
    @Query("UPDATE Conversation c SET c.user1Unread = 0 WHERE c.id = :convId AND c.user1Id = :userId")
    void clearUser1Unread(@Param("convId") Long convId, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Conversation c SET c.user2Unread = 0 WHERE c.id = :convId AND c.user2Id = :userId")
    void clearUser2Unread(@Param("convId") Long convId, @Param("userId") Long userId);
}
