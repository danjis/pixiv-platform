package com.pixiv.notification.repository;

import com.pixiv.notification.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Page<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Feedback> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type, Pageable pageable);

    Page<Feedback> findByUserIdAndTypeAndCommissionIdOrderByCreatedAtDesc(
            Long userId, String type, Long commissionId, Pageable pageable);

    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Feedback> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<Feedback> findByTypeOrderByCreatedAtDesc(String type, Pageable pageable);

    Page<Feedback> findByTypeAndStatusOrderByCreatedAtDesc(String type, String status, Pageable pageable);

    boolean existsByTypeAndPaymentIdAndStatusIn(String type, Long paymentId, Collection<String> statuses);

    boolean existsByTypeAndCommissionIdAndUserIdAndStatusIn(
            String type, Long commissionId, Long userId, Collection<String> statuses);

    long countByStatus(String status);
}
