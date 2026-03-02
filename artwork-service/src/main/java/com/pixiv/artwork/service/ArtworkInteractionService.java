package com.pixiv.artwork.service;

import com.pixiv.artwork.entity.Artwork;
import com.pixiv.artwork.entity.ArtworkStatus;
import com.pixiv.artwork.entity.Favorite;
import com.pixiv.artwork.entity.Like;
import com.pixiv.artwork.repository.ArtworkRepository;
import com.pixiv.artwork.repository.FavoriteRepository;
import com.pixiv.artwork.repository.LikeRepository;
import com.pixiv.common.exception.BusinessException;
import com.pixiv.common.constant.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 作品互动服务
 * 处理点赞、收藏等社交互动功能
 */
@Service
public class ArtworkInteractionService {

    private static final Logger logger = LoggerFactory.getLogger(ArtworkInteractionService.class);

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String ARTWORK_CACHE_PREFIX = "artwork:";
    private static final String NOTIFICATION_QUEUE = "notification.create";

    /**
     * 点赞作品
     * 
     * @param userId    用户 ID
     * @param artworkId 作品 ID
     * @throws IllegalArgumentException 如果作品不存在或已点赞
     */
    @Transactional
    public void likeArtwork(Long userId, Long artworkId) {
        logger.info("用户点赞作品: userId={}, artworkId={}", userId, artworkId);

        // 1. 验证作品存在
        Artwork artwork = artworkRepository.findByIdAndStatus(artworkId, ArtworkStatus.PUBLISHED)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTWORK_NOT_FOUND, "作品不存在或未发布"));

        // 2. 检查是否已点赞
        if (likeRepository.existsByArtworkIdAndUserId(artworkId, userId)) {
            logger.warn("用户已点赞该作品: userId={}, artworkId={}", userId, artworkId);
            throw new BusinessException(ErrorCode.ALREADY_LIKED, "您已经点赞过该作品");
        }

        // 3. 创建点赞记录
        Like like = new Like();
        like.setArtworkId(artworkId);
        like.setUserId(userId);
        likeRepository.save(like);

        // 4. 增加作品点赞计数
        artwork.incrementLikeCount();
        artworkRepository.save(artwork);

        logger.info("点赞成功: userId={}, artworkId={}, newLikeCount={}",
                userId, artworkId, artwork.getLikeCount());

        // 5. 清除作品详情缓存
        String cacheKey = ARTWORK_CACHE_PREFIX + artworkId;
        redisTemplate.delete(cacheKey);

        // 6. 通知作品画师（不通知自己给自己点赞）
        if (!userId.equals(artwork.getArtistId())) {
            sendNotification(artwork.getArtistId(), "ARTWORK_LIKED",
                    "你的作品「" + artwork.getTitle() + "」收到了一个新的点赞",
                    "/artworks/" + artworkId);
        }

        // 7. 更新热度分数
        updateHotnessScore(artwork);
    }

    /**
     * 取消点赞作品
     * 
     * @param userId    用户 ID
     * @param artworkId 作品 ID
     * @throws IllegalArgumentException 如果作品不存在或未点赞
     */
    @Transactional
    public void unlikeArtwork(Long userId, Long artworkId) {
        logger.info("用户取消点赞作品: userId={}, artworkId={}", userId, artworkId);

        // 1. 验证作品存在
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTWORK_NOT_FOUND, "作品不存在"));

        // 2. 检查是否已点赞
        if (!likeRepository.existsByArtworkIdAndUserId(artworkId, userId)) {
            logger.warn("用户未点赞该作品: userId={}, artworkId={}", userId, artworkId);
            throw new BusinessException(ErrorCode.NOT_LIKED, "您还未点赞该作品");
        }

        // 3. 删除点赞记录
        likeRepository.deleteByArtworkIdAndUserId(artworkId, userId);

        // 4. 减少作品点赞计数
        artwork.decrementLikeCount();
        artworkRepository.save(artwork);

        logger.info("取消点赞成功: userId={}, artworkId={}, newLikeCount={}",
                userId, artworkId, artwork.getLikeCount());

        // 5. 清除作品详情缓存
        String cacheKey = ARTWORK_CACHE_PREFIX + artworkId;
        redisTemplate.delete(cacheKey);

        // 6. 更新热度分数
        updateHotnessScore(artwork);
    }

    /**
     * 收藏作品
     * 
     * @param userId    用户 ID
     * @param artworkId 作品 ID
     * @throws IllegalArgumentException 如果作品不存在或已收藏
     */
    @Transactional
    public void favoriteArtwork(Long userId, Long artworkId) {
        logger.info("用户收藏作品: userId={}, artworkId={}", userId, artworkId);

        // 1. 验证作品存在
        Artwork artwork = artworkRepository.findByIdAndStatus(artworkId, ArtworkStatus.PUBLISHED)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTWORK_NOT_FOUND, "作品不存在或未发布"));

        // 2. 检查是否已收藏
        if (favoriteRepository.existsByArtworkIdAndUserId(artworkId, userId)) {
            logger.warn("用户已收藏该作品: userId={}, artworkId={}", userId, artworkId);
            throw new BusinessException(ErrorCode.ALREADY_FAVORITED, "您已经收藏过该作品");
        }

        // 3. 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setArtworkId(artworkId);
        favorite.setUserId(userId);
        favoriteRepository.save(favorite);

        // 4. 增加作品收藏计数
        artwork.incrementFavoriteCount();
        artworkRepository.save(artwork);

        logger.info("收藏成功: userId={}, artworkId={}, newFavoriteCount={}",
                userId, artworkId, artwork.getFavoriteCount());

        // 5. 清除作品详情缓存
        String cacheKey = ARTWORK_CACHE_PREFIX + artworkId;
        redisTemplate.delete(cacheKey);

        // 6. 通知作品画师（不通知自己收藏自己的作品）
        if (!userId.equals(artwork.getArtistId())) {
            sendNotification(artwork.getArtistId(), "ARTWORK_FAVORITED",
                    "你的作品「" + artwork.getTitle() + "」被收藏了",
                    "/artworks/" + artworkId);
        }

        // 7. 更新热度分数
        updateHotnessScore(artwork);
    }

    /**
     * 取消收藏作品
     * 
     * @param userId    用户 ID
     * @param artworkId 作品 ID
     * @throws IllegalArgumentException 如果作品不存在或未收藏
     */
    @Transactional
    public void unfavoriteArtwork(Long userId, Long artworkId) {
        logger.info("用户取消收藏作品: userId={}, artworkId={}", userId, artworkId);

        // 1. 验证作品存在
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTWORK_NOT_FOUND, "作品不存在"));

        // 2. 检查是否已收藏
        if (!favoriteRepository.existsByArtworkIdAndUserId(artworkId, userId)) {
            logger.warn("用户未收藏该作品: userId={}, artworkId={}", userId, artworkId);
            throw new BusinessException(ErrorCode.NOT_FAVORITED, "您还未收藏该作品");
        }

        // 3. 删除收藏记录
        favoriteRepository.deleteByArtworkIdAndUserId(artworkId, userId);

        // 4. 减少作品收藏计数
        artwork.decrementFavoriteCount();
        artworkRepository.save(artwork);

        logger.info("取消收藏成功: userId={}, artworkId={}, newFavoriteCount={}",
                userId, artworkId, artwork.getFavoriteCount());

        // 5. 清除作品详情缓存
        String cacheKey = ARTWORK_CACHE_PREFIX + artworkId;
        redisTemplate.delete(cacheKey);

        // 6. 更新热度分数
        updateHotnessScore(artwork);
    }

    /**
     * 检查用户是否点赞了作品
     * 
     * @param userId    用户 ID
     * @param artworkId 作品 ID
     * @return 是否已点赞
     */
    public boolean isLiked(Long userId, Long artworkId) {
        return likeRepository.existsByArtworkIdAndUserId(artworkId, userId);
    }

    /**
     * 检查用户是否收藏了作品
     * 
     * @param userId    用户 ID
     * @param artworkId 作品 ID
     * @return 是否已收藏
     */
    public boolean isFavorited(Long userId, Long artworkId) {
        return favoriteRepository.existsByArtworkIdAndUserId(artworkId, userId);
    }

    // ===================== 私有辅助方法 =====================

    /**
     * 更新作品热度分数
     * 热度公式: views * 0.1 + likes * 3 + favorites * 5 + comments * 2
     */
    private void updateHotnessScore(Artwork artwork) {
        double score = HotnessCalculator.calculate(artwork);
        artwork.setHotnessScore(score);
        artworkRepository.save(artwork);
        logger.debug("热度分数已更新: artworkId={}, score={}", artwork.getId(), score);

        // 同步更新排行榜
        rankingService.updateScore(artwork.getId(), score);
    }

    /**
     * 通过 RabbitMQ 发送通知到 notification-service
     */
    private void sendNotification(Long userId, String type, String content, String linkUrl) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("type", type);
            message.put("content", content);
            message.put("linkUrl", linkUrl);
            rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, message);
            logger.debug("通知消息已发送: userId={}, type={}", userId, type);
        } catch (Exception e) {
            // 通知发送失败不影响主流程
            logger.warn("发送通知失败: userId={}, type={}, error={}", userId, type, e.getMessage());
        }
    }
}
