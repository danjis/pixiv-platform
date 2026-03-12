package com.pixiv.user.repository;

import com.pixiv.user.entity.WithdrawalRequest;
import com.pixiv.user.entity.WithdrawalRequest.WithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {

    Page<WithdrawalRequest> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<WithdrawalRequest> findByStatusOrderByCreatedAtDesc(WithdrawalStatus status, Pageable pageable);

    Page<WithdrawalRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<WithdrawalRequest> findByUserIdAndStatus(Long userId, WithdrawalStatus status);

    long countByStatus(WithdrawalStatus status);
}
