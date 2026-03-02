package com.pixiv.artwork.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Feed 流服务
 * <p>
 * 使用 Redis List 维护每个用户的 Feed 收件箱，
 * 采用推拉结合策略优化关注动态加载性能：
 * - 当画师发布作品时，将作品 ID 推送（LPUSH）到所有粉丝的 Feed 列表
 * - 查询 Feed 时优先从 Redis 获取，命中失败再降级到数据库查询
 */
@Service
public class FeedService {

    private static final Logger logger = LoggerFactory.getLogger(FeedService.class);

    /** Redis key 前缀：用户 Feed 收件箱 */
    private static final String FEED_KEY_PREFIX = "feed:inbox:";
    /** Feed 列表最大长度 */
    private static final int MAX_FEED_SIZE = 1000;
    /** Feed 缓存过期时间（3 天） */
    private static final long FEED_TTL_DAYS = 3;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 将作品 ID 推送到指定粉丝的 Feed 收件箱
     *
     * @param followerIds 粉丝 ID 列表
     * @param artworkId   新发布的作品 ID
     */
    public void pushToFollowerFeeds(List<Long> followerIds, Long artworkId) {
        if (followerIds == null || followerIds.isEmpty()) {
            return;
        }
        logger.info("推送作品到 {} 个粉丝的 Feed: artworkId={}", followerIds.size(), artworkId);
        for (Long followerId : followerIds) {
            try {
                String key = FEED_KEY_PREFIX + followerId;
                redisTemplate.opsForList().leftPush(key, artworkId);
                // 裁剪列表，避免无限增长
                redisTemplate.opsForList().trim(key, 0, MAX_FEED_SIZE - 1);
                // 设置/刷新过期时间
                redisTemplate.expire(key, FEED_TTL_DAYS, TimeUnit.DAYS);
            } catch (Exception e) {
                logger.warn("推送 Feed 失败: followerId={}, artworkId={}, error={}",
                        followerId, artworkId, e.getMessage());
            }
        }
    }

    /**
     * 从 Redis Feed 收件箱获取作品 ID（分页）
     *
     * @param userId 用户 ID
     * @param page   页码（从 1 开始）
     * @param size   每页大小
     * @return 作品 ID 列表；Redis 无数据时返回 null（触发降级到 DB）
     */
    public List<Long> getFeedArtworkIds(Long userId, int page, int size) {
        try {
            String key = FEED_KEY_PREFIX + userId;
            // 检查 key 是否存在
            Boolean exists = redisTemplate.hasKey(key);
            if (exists == null || !exists) {
                return null; // 返回 null 表示需要降级到数据库
            }

            long start = (long) (page - 1) * size;
            long end = start + size - 1;

            List<Object> rawIds = redisTemplate.opsForList().range(key, start, end);
            if (rawIds == null || rawIds.isEmpty()) {
                return Collections.emptyList();
            }

            return rawIds.stream()
                    .map(obj -> {
                        if (obj instanceof Number) {
                            return ((Number) obj).longValue();
                        }
                        return Long.parseLong(obj.toString());
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("从 Redis 获取 Feed 失败，降级到 DB: userId={}, error={}", userId, e.getMessage());
            return null; // 降级
        }
    }

    /**
     * 获取用户 Feed 收件箱总长度
     *
     * @param userId 用户 ID
     * @return Feed 长度；Redis 无数据时返回 -1
     */
    public long getFeedSize(Long userId) {
        try {
            String key = FEED_KEY_PREFIX + userId;
            Long size = redisTemplate.opsForList().size(key);
            return size != null ? size : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 从用户 Feed 中移除指定作品（作品删除时调用）
     *
     * @param followerIds 粉丝 ID 列表
     * @param artworkId   被删除的作品 ID
     */
    public void removeFromFollowerFeeds(List<Long> followerIds, Long artworkId) {
        if (followerIds == null || followerIds.isEmpty()) {
            return;
        }
        for (Long followerId : followerIds) {
            try {
                String key = FEED_KEY_PREFIX + followerId;
                redisTemplate.opsForList().remove(key, 0, artworkId);
            } catch (Exception e) {
                logger.warn("移除 Feed 中的作品失败: followerId={}, artworkId={}", followerId, artworkId);
            }
        }
    }
}
