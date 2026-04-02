package com.pixiv.notification.repository;

import com.pixiv.notification.entity.AiChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long> {

    Page<AiChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    Optional<AiChatSession> findByIdAndUserId(Long id, Long userId);

    List<AiChatSession> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, String status);
}
