package com.pixiv.artwork.repository;

import com.pixiv.artwork.entity.ContestVote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 比赛投票 Repository
 */
@Repository
public interface ContestVoteRepository extends JpaRepository<ContestVote, Long> {

    Optional<ContestVote> findByEntryIdAndVoterId(Long entryId, Long voterId);

    boolean existsByEntryIdAndVoterId(Long entryId, Long voterId);

    Page<ContestVote> findByEntryIdOrderByCreatedAtDesc(Long entryId, Pageable pageable);

    int countByEntryId(Long entryId);

    int countByVoterIdAndContestId(Long voterId, Long contestId);
}
