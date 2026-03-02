package com.pixiv.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 画师信息 DTO
 * 用于服务间传递画师信息
 *
 * @author Pixiv Platform Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 画师 ID
     */
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像 URL
     */
    private String avatarUrl;

    /**
     * 作品集 URL
     */
    private String portfolioUrl;

    /**
     * 画师描述
     */
    private String description;

    /**
     * 粉丝数量
     */
    private Integer followerCount;

    /**
     * 作品数量
     */
    private Integer artworkCount;

    /**
     * 约稿数量
     */
    private Integer commissionCount;

    /**
     * 是否接受约稿
     */
    private Boolean acceptingCommissions;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
