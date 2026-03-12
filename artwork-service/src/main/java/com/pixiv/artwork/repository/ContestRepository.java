package com.pixiv.artwork.repository;

import com.pixiv.artwork.entity.Contest;
import com.pixiv.artwork.entity.ContestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 比赛 Repository
 */
@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

    Page<Contest> findByStatus(ContestStatus status, Pageable pageable);

    Page<Contest> findByStatusIn(List<ContestStatus> statuses, Pageable pageable);

    List<Contest> findByStatusIn(List<ContestStatus> statuses);

    List<Contest> findByStatus(ContestStatus status);

    Page<Contest> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
