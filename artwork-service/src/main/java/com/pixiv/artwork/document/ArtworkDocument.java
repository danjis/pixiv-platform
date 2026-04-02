package com.pixiv.artwork.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch 作品文档实体
 * 
 * 映射到 ES 的 artworks 索引，支持中文全文检索（IK 分词器）
 * 
 * 索引设置：
 * - 单分片（开发环境，单节点部署）
 * - 零副本（单节点无法分配副本）
 */
@Document(indexName = "artworks")
@Setting(shards = 1, replicas = 0)
public class ArtworkDocument {

    @Id
    private Long id;

    /**
     * 作品标题 — 主字段使用 IK 分词器全文检索，子字段 keyword 用于精确匹配和聚合
     */
    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"), otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword, ignoreAbove = 256)
    })
    private String title;

    /**
     * 作品描述 — IK 分词器全文检索
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;

    /**
     * 标签列表 — Keyword 类型，支持精确筛选和聚合统计
     */
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    /**
     * 画师名称 — 支持 IK 分词搜索
     */
    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"), otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword, ignoreAbove = 100)
    })
    private String artistName;

    @Field(type = FieldType.Long)
    private Long artistId;

    @Field(type = FieldType.Keyword, index = false)
    private String imageUrl;

    @Field(type = FieldType.Keyword, index = false)
    private String thumbnailUrl;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Field(type = FieldType.Integer)
    private Integer likeCount;

    @Field(type = FieldType.Integer)
    private Integer favoriteCount;

    @Field(type = FieldType.Integer)
    private Integer commentCount;

    @Field(type = FieldType.Double)
    private Double hotnessScore;

    @Field(type = FieldType.Boolean)
    private Boolean isAigc;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    /**
     * 图像特征向量 — 256 维，用于以图搜图 kNN 向量检索
     * 由 DeepDanbooru 模型提取的标签概率分布，经 L2 归一化
     */
    @Field(type = FieldType.Dense_Vector, dims = 256)
    private float[] featureVector;

    // ==================== Constructors ====================

    public ArtworkDocument() {
    }

    // ==================== Getters and Setters ====================

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtistId(Long artistId) {
        this.artistId = artistId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public float[] getFeatureVector() {
        return featureVector;
    }

    public void setFeatureVector(float[] featureVector) {
        this.featureVector = featureVector;
    }
}
