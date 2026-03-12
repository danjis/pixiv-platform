package com.pixiv.artwork.dto;

import com.pixiv.artwork.entity.ContestVote;

import java.time.LocalDateTime;

/**
 * 投票信息 DTO
 */
public class ContestVoteDTO {

    private Long id;
    private Long contestId;
    private Long entryId;
    private Long voterId;
    private String voterName;
    private String voterAvatar;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;

    public ContestVoteDTO() {
    }

    public ContestVoteDTO(ContestVote vote) {
        this.id = vote.getId();
        this.contestId = vote.getContestId();
        this.entryId = vote.getEntryId();
        this.voterId = vote.getVoterId();
        this.score = vote.getScore();
        this.comment = vote.getComment();
        this.createdAt = vote.getCreatedAt();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public Long getVoterId() {
        return voterId;
    }

    public void setVoterId(Long voterId) {
        this.voterId = voterId;
    }

    public String getVoterName() {
        return voterName;
    }

    public void setVoterName(String voterName) {
        this.voterName = voterName;
    }

    public String getVoterAvatar() {
        return voterAvatar;
    }

    public void setVoterAvatar(String voterAvatar) {
        this.voterAvatar = voterAvatar;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
