package com.pixiv.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作品信息 DTO
 * 用于服务间传递作品基本信息
 *
 * @author Pixiv Platform Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtworkDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 作品 ID
     */
    private Long id;

    /**
     * 画师 ID
     */
    private Long artistId;

    /**
     * 画师名称
     */
    private String artistName;

    /**
     * 作品标题
     */
    private String title;

    /**
     * 作品描述
     */
    private String description;

    /**
     * 图片 URL
     */
    private String imageUrl;

    /**
     * 缩略图 URL
     */
    private String thumbnailUrl;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 评价数
     */
    private Integer commentCount;

    /**
     * 热度分数
     */
    private Double hotnessScore;

    /**
     * 作品状态（DRAFT, PUBLISHED, DELETED）
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
