package com.pixiv.artwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建/更新比赛请求 DTO
 */
public class ContestRequest {

    @NotBlank(message = "比赛标题不能为空")
    private String title;

    private String description;

    private String coverImage;

    private String rules;

    private String rewardInfo;

    @NotNull(message = "开始时间不能为空")
    private String startTime;

    @NotNull(message = "投稿截止时间不能为空")
    private String endTime;

    @NotNull(message = "投票截止时间不能为空")
    private String votingEndTime;

    private Integer maxEntriesPerArtist = 1;

    // Getters and Setters

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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getVotingEndTime() {
        return votingEndTime;
    }

    public void setVotingEndTime(String votingEndTime) {
        this.votingEndTime = votingEndTime;
    }

    public Integer getMaxEntriesPerArtist() {
        return maxEntriesPerArtist;
    }

    public void setMaxEntriesPerArtist(Integer maxEntriesPerArtist) {
        this.maxEntriesPerArtist = maxEntriesPerArtist;
    }
}
