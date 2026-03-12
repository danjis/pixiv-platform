package com.pixiv.user.repository;

import com.pixiv.user.entity.MembershipLevel;
import com.pixiv.user.entity.UserMembership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    Optional<UserMembership> findByUserId(Long userId);

    Page<UserMembership> findByLevelNot(MembershipLevel level, Pageable pageable);

    Page<UserMembership> findByLevel(MembershipLevel level, Pageable pageable);

    long countByLevelNot(MembershipLevel level);

    /**
     * 查找所有有效的 VIP/SVIP 会员（未过期）
     */
    @Query("SELECT m FROM UserMembership m WHERE m.level <> 'NORMAL' AND m.expireTime > :now")
    List<UserMembership> findActiveMembers(LocalDateTime now);
}
