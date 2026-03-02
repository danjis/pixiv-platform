package com.pixiv.artwork.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 作品实体类
 * 存储画师发布的艺术作品信息
 */
@Entity
@Table(name = "artworks", indexes = {
        @Index(name = "idx_artwork_artist", columnList = "artist_id"),
        @Index(name = "idx_artwork_created", columnList = "created_at"),
        @Index(name = "idx_artwork_hotness", columnList = "hotness_score"),
        @Index(name = "idx_artwork_status", columnList = "status")
})
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 画师 ID（关联用户服务）
     */
    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    /**
     * 画师名称（冗余字段，避免频繁调用用户服务）
     */
    @Column(name = "artist_name", nullable = false, length = 100)
    private String artistName;

    /**
     * 画师头像 URL（冗余字段，避免频繁调用用户服务）
     */
    @Column(name = "artist_avatar", length = 500)
    private String artistAvatar;

    /**
     * 作品标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 作品描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 原图 URL
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * 缩略图 URL
     */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /**
     * 浏览次数
     */
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    /**
     * 点赞次数
     */
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    /**
     * 收藏次数
     */
    @Column(name = "favorite_count", nullable = false)
    private Integer favoriteCount = 0;

    /**
     * 评价次数
     */
    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    /**
     * 热度分数（用于排序推荐）
     */
    @Column(name = "hotness_score", nullable = false)
    private Double hotnessScore = 0.0;

    /**
     * 作品状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArtworkStatus status = ArtworkStatus.PUBLISHED;

    /**
     * 图片数量
     */
    @Column(name = "image_count", nullable = false)
    private Integer imageCount = 1;

    /**
     * 是否为 AIGC（AI 生成内容）作品
     */
    @Column(name = "is_aigc", nullable = false)
    private Boolean isAigc = false;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
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

    /**
     * 增加浏览次数
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 增加点赞次数
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * 减少点赞次数
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 增加收藏次数
     */
    public void incrementFavoriteCount() {
        this.favoriteCount++;
    }

    /**
     * 减少收藏次数
     */
    public void decrementFavoriteCount() {
        if (this.favoriteCount > 0) {
            this.favoriteCount--;
        }
    }

    /**
     * 增加评价次数
     */
    public void incrementCommentCount() {
        this.commentCount++;
    }

    /**
     * 减少评价次数
     */
    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}
