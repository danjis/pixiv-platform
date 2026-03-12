package com.pixiv.artwork.repository;

import com.pixiv.artwork.entity.ContestEntry;
import com.pixiv.artwork.entity.ContestEntryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 比赛参赛作品 Repository
 */
@Repository
public interface ContestEntryRepository extends JpaRepository<ContestEntry, Long> {

    Page<ContestEntry> findByContestIdOrderByAverageScoreDesc(Long contestId, Pageable pageable);

    Page<ContestEntry> findByContestIdAndStatusOrderByAverageScoreDesc(Long contestId, ContestEntryStatus status,
            Pageable pageable);

    List<ContestEntry> findByContestIdOrderByAverageScoreDesc(Long contestId);

    List<ContestEntry> findByContestIdAndStatusOrderByAverageScoreDesc(Long contestId, ContestEntryStatus status);

    Optional<ContestEntry> findByContestIdAndArtistId(Long contestId, Long artistId);

    int countByContestIdAndArtistId(Long contestId, Long artistId);

    int countByContestId(Long contestId);

    List<ContestEntry> findByArtistId(Long artistId);

    List<ContestEntry> findByContestIdInAndStatusOrderByAverageScoreDesc(List<Long> contestIds,
            ContestEntryStatus status, Pageable pageable);
}
