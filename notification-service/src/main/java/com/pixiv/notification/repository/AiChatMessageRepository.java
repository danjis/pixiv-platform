package com.pixiv.notification.repository;

import com.pixiv.notification.entity.AiChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    List<AiChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    List<AiChatMessage> findBySessionIdOrderByCreatedAtDesc(Long sessionId, Pageable pageable);

    long countBySessionId(Long sessionId);
}
