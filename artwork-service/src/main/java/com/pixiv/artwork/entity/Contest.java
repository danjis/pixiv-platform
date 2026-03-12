package com.pixiv.artwork.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 比赛实体
 */
@Entity
@Table(name = "contests", indexes = {
        @Index(name = "idx_contest_status", columnList = "status"),
        @Index(name = "idx_contest_start", columnList = "start_time"),
        @Index(name = "idx_contest_end", columnList = "end_time")
})
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(columnDefinition = "TEXT")
    private String rules;

    @Column(name = "reward_info", columnDefinition = "TEXT")
    private String rewardInfo;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "voting_end_time", nullable = false)
    private LocalDateTime votingEndTime;

    @Column(name = "max_entries_per_artist", nullable = false)
    private Integer maxEntriesPerArtist = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContestStatus status = ContestStatus.UPCOMING;

    @Column(name = "entry_count", nullable = false)
    private Integer entryCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public ContestStatus getStatus() {
        return status;
    }

    public void setStatus(ContestStatus status) {
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void incrementEntryCount() {
        this.entryCount++;
    }
}
