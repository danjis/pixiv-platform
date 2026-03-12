package com.pixiv.user.repository;

import com.pixiv.user.entity.UserCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCheckinRepository extends JpaRepository<UserCheckin, Long> {
    Optional<UserCheckin> findByUserIdAndCheckinDate(Long userId, LocalDate checkinDate);

    List<UserCheckin> findByUserIdOrderByCheckinDateDesc(Long userId);

    List<UserCheckin> findByUserIdAndCheckinDateBetweenOrderByCheckinDateAsc(Long userId, LocalDate startDate,
            LocalDate endDate);

    boolean existsByUserIdAndCheckinDate(Long userId, LocalDate checkinDate);
}
