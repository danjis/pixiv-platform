package com.pixiv.artwork.dto;

import com.pixiv.artwork.entity.ArtworkStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作品 DTO
 * 
 * 用于返回作品基本信息
 */
public class ArtworkDTO {

    private Long id;
    private Long artistId;
    private String artistName;
    private String artistAvatar;
    private String title;
    private String description;
    private String imageUrl;
    private String thumbnailUrl;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Double hotnessScore;
    private ArtworkStatus status;
    private Integer imageCount;
    private Boolean isAigc;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TagDTO> tags;
    private List<ImageDTO> images;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Double getHotnessScore() {
        return hotnessScore;
    }

    public void setHotnessScore(Double hotnessScore) {
        this.hotnessScore = hotnessScore;
    }

    public ArtworkStatus getStatus() {
        return status;
    }

    public void setStatus(ArtworkStatus status) {
        this.status = status;
    }

    public Integer getImageCount() {
        return imageCount;
    }

    public void setImageCount(Integer imageCount) {
        this.imageCount = imageCount;
    }

    public Boolean getIsAigc() {
        return isAigc;
    }

    public void setIsAigc(Boolean isAigc) {
        this.isAigc = isAigc;
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

    public List<TagDTO> getTags() {
        return tags;
    }

    public void setTags(List<TagDTO> tags) {
        this.tags = tags;
    }

    public List<ImageDTO> getImages() {
        return images;
    }

    public void setImages(List<ImageDTO> images) {
        this.images = images;
    }
}
