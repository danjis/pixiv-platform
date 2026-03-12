package com.pixiv.artwork.dto;

import com.pixiv.artwork.entity.Contest;
import com.pixiv.artwork.entity.ContestStatus;

import java.time.LocalDateTime;

/**
 * 比赛信息 DTO
 */
public class ContestDTO {

    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private String rules;
    private String rewardInfo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime votingEndTime;
    private Integer maxEntriesPerArtist;
    private String status;
    private Integer entryCount;
    private LocalDateTime createdAt;

    public ContestDTO() {
    }

    public ContestDTO(Contest contest) {
        this.id = contest.getId();
        this.title = contest.getTitle();
        this.description = contest.getDescription();
        this.coverImage = contest.getCoverImage();
        this.rules = contest.getRules();
        this.rewardInfo = contest.getRewardInfo();
        this.startTime = contest.getStartTime();
        this.endTime = contest.getEndTime();
        this.votingEndTime = contest.getVotingEndTime();
        this.maxEntriesPerArtist = contest.getMaxEntriesPerArtist();
        this.status = contest.getStatus().name();
        this.entryCount = contest.getEntryCount();
        this.createdAt = contest.getCreatedAt();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getRewardInfo() {
        return rewardInfo;
    }

    public void setRewardInfo(String rewardInfo) {
        this.rewardInfo = rewardInfo;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getVotingEndTime() {
        return votingEndTime;
    }

    public void setVotingEndTime(LocalDateTime votingEndTime) {
        this.votingEndTime = votingEndTime;
    }

    public Integer getMaxEntriesPerArtist() {
        return maxEntriesPerArtist;
    }

    public void setMaxEntriesPerArtist(Integer maxEntriesPerArtist) {
        this.maxEntriesPerArtist = maxEntriesPerArtist;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(Integer entryCount) {
        this.entryCount = entryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
