package com.pixiv.artwork.service;

import com.pixiv.artwork.entity.Artwork;

/**
 * 热度分数计算器（统一公式）
 *
 * 公式: views * 0.1 + likes * 3 + favorites * 5 + comments * 2
 *
 * 所有需要计算热度的地方（浏览/点赞/收藏/评论）均应调用此类，
 * 避免公式散落在多个 Service 中导致不一致。
 */
public final class HotnessCalculator {

    private HotnessCalculator() {
    }

    public static final double WEIGHT_VIEW = 0.1;
    public static final double WEIGHT_LIKE = 3.0;
    public static final double WEIGHT_FAVORITE = 5.0;
    public static final double WEIGHT_COMMENT = 2.0;

    /**
     * 根据作品实体的各项计数计算热度分数
     */
    public static double calculate(Artwork artwork) {
        return artwork.getViewCount() * WEIGHT_VIEW
                + artwork.getLikeCount() * WEIGHT_LIKE
                + artwork.getFavoriteCount() * WEIGHT_FAVORITE
                + artwork.getCommentCount() * WEIGHT_COMMENT;
    }

    /**
     * 根据原始计数计算热度分数
     */
    public static double calculate(long viewCount, long likeCount, long favoriteCount, long commentCount) {
        return viewCount * WEIGHT_VIEW
                + likeCount * WEIGHT_LIKE
                + favoriteCount * WEIGHT_FAVORITE
                + commentCount * WEIGHT_COMMENT;
    }
}
