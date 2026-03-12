package com.pixiv.artwork.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 比赛参赛作品实体
 */
@Entity
@Table(name = "contest_entries", indexes = {
        @Index(name = "idx_entry_contest", columnList = "contest_id"),
        @Index(name = "idx_entry_artist", columnList = "artist_id"),
        @Index(name = "idx_entry_avg_score", columnList = "average_score")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_contest_artist", columnNames = { "contest_id", "artist_id" })
})
public class ContestEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contest_id", nullable = false)
    private Long contestId;

    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @Column(name = "artist_name", nullable = false, length = 50)
    private String artistName;

    @Column(name = "artwork_id")
    private Long artworkId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "total_score", nullable = false)
    private Double totalScore = 0.0;

    @Column(name = "vote_count", nullable = false)
    private Integer voteCount = 0;

    @Column(name = "average_score", nullable = false)
    private Double averageScore = 0.0;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContestEntryStatus status = ContestEntryStatus.SUBMITTED;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }

    // 投票计算方法
    public void addVote(int score) {
        addVote(score, 1.0);
    }

    /**
     * 投票
     * 
     * @param score  投票分数
     * @param weight 权重（固定传1.0）
     */
    public void addVote(int score, double weight) {
        this.totalScore += score * weight;
        this.voteCount++;
        this.averageScore = this.totalScore / this.voteCount;
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

    public Long getArtistId() {
        return artistId;
    }

    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Long getArtworkId() {
        return artworkId;
    }

    public void setArtworkId(Long artworkId) {
        this.artworkId = artworkId;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Integer getRankPosition() {
        return rankPosition;
    }

    public void setRankPosition(Integer rankPosition) {
        this.rankPosition = rankPosition;
    }

    public ContestEntryStatus getStatus() {
        return status;
    }

    public void setStatus(ContestEntryStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
