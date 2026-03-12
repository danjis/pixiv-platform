package com.pixiv.artwork.dto;

import com.pixiv.artwork.entity.ContestEntry;

import java.time.LocalDateTime;

/**
 * 参赛作品信息 DTO
 */
public class ContestEntryDTO {

    private Long id;
    private Long contestId;
    private Long artistId;
    private String artistName;
    private String artistAvatar;
    private Long artworkId;
    private String title;
    private String description;
    private String imageUrl;
    private String thumbnailUrl;
    private Double totalScore;
    private Integer voteCount;
    private Double averageScore;
    private Integer rankPosition;
    private String status;
    private LocalDateTime submittedAt;
    // 当前用户是否已投票
    private Boolean hasVoted;
    // 当前用户的评分
    private Integer myScore;
    // 比赛标题（用于首页展示）
    private String contestTitle;

    public ContestEntryDTO() {
    }

    public ContestEntryDTO(ContestEntry entry) {
        this.id = entry.getId();
        this.contestId = entry.getContestId();
        this.artistId = entry.getArtistId();
        this.artistName = entry.getArtistName();
        this.artworkId = entry.getArtworkId();
        this.title = entry.getTitle();
        this.description = entry.getDescription();
        this.imageUrl = entry.getImageUrl();
        this.thumbnailUrl = entry.getThumbnailUrl();
        this.totalScore = entry.getTotalScore();
        this.voteCount = entry.getVoteCount();
        this.averageScore = entry.getAverageScore();
        this.rankPosition = entry.getRankPosition();
        this.status = entry.getStatus().name();
        this.submittedAt = entry.getSubmittedAt();
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

    public String getArtistAvatar() {
        return artistAvatar;
    }

    public void setArtistAvatar(String artistAvatar) {
        this.artistAvatar = artistAvatar;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Boolean getHasVoted() {
        return hasVoted;
    }

    public void setHasVoted(Boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    public Integer getMyScore() {
        return myScore;
    }

    public void setMyScore(Integer myScore) {
        this.myScore = myScore;
    }

    public String getContestTitle() {
        return contestTitle;
    }

    public void setContestTitle(String contestTitle) {
        this.contestTitle = contestTitle;
    }
}
