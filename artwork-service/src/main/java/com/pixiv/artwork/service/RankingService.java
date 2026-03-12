package com.pixiv.artwork.service;

import com.pixiv.artwork.dto.ArtworkDTO;
import com.pixiv.artwork.entity.Artwork;
import com.pixiv.artwork.entity.ArtworkStatus;
import com.pixiv.artwork.repository.ArtworkRepository;
import com.pixiv.common.dto.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 排行榜服务
 * 
 * 使用 Redis ZSet 维护实时排行榜，支持日榜/周榜/月榜/总榜。
 * 支持多种排序维度：热度（hottest）、点赞（most_liked）、收藏（most_favorited）、浏览（most_viewed）。
 * 
 * Redis Key 设计：
 * - artwork:ranking:hottest:daily → 热度日榜（TTL 25小时）
 * - artwork:ranking:most_liked:daily → 点赞日榜（TTL 25小时）
 * - artwork:ranking:most_favorited:weekly → 收藏周榜（TTL 8天）
 * - artwork:ranking:hottest:all → 热度总榜（无TTL，定时刷新）
 * - ...以此类推
 * 
 * 兼容旧key: artwork:ranking:daily 等价于 artwork:ranking:hottest:daily
 */
@Service
public class RankingService {

    private static final Logger logger = LoggerFactory.getLogger(RankingService.class);

    private static final String RANKING_PREFIX = "artwork:ranking:";
    // 旧的 key 保留兼容（等价于 hottest 维度）
    private static final String RANKING_DAILY = RANKING_PREFIX + "daily";
    private static final String RANKING_WEEKLY = RANKING_PREFIX + "weekly";
    private static final String RANKING_MONTHLY = RANKING_PREFIX + "monthly";
    private static final String RANKING_ALL = RANKING_PREFIX + "all";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ArtworkRepository artworkRepository;

    // ==================== 多维度排行榜 Key 构建 ====================

    /**
     * 根据排序类型和时间范围构造 Redis Key
     */
    public String getRankingKey(String sortBy, String period) {
        if (sortBy == null || sortBy.isEmpty())
            sortBy = "hottest";
        if (period == null || period.isEmpty())
            period = "all";
        return RANKING_PREFIX + sortBy.toLowerCase() + ":" + period.toLowerCase();
    }

    /**
     * 判断指定 key 的排行榜是否有数据
     */
    public boolean isRankingAvailable(String key) {
        try {
            Long size = redisTemplate.opsForZSet().zCard(key);
            return size != null && size > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从指定 key 获取排行榜 ID 列表
     */
    public List<Long> getRankingIdsByKey(String key, int page, int size) {
        long start = (long) (page - 1) * size;
        long end = start + size - 1;
        try {
            Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(key,
                    start, end);
            if (tuples == null || tuples.isEmpty()) {
                return Collections.emptyList();
            }
            return tuples.stream()
                    .map(tuple -> Long.parseLong(String.valueOf(tuple.getValue())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("从 Redis 获取排行榜失败: key={}, error={}", key, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 获取指定 key 的排行榜总数
     */
    public long getRankingCountByKey(String key) {
        try {
            Long count = redisTemplate.opsForZSet().zCard(key);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 更新作品在各排行榜中的分数（热度维度）
     * 
     * 每次互动（点赞/收藏/评论/浏览）后调用此方法。
     * 
     * @param artworkId 作品 ID
     * @param score     新的热度分数
     */
    public void updateScore(Long artworkId, double score) {
        try {
            String member = String.valueOf(artworkId);
            // 同时更新所有时间维度的热度排行榜
            redisTemplate.opsForZSet().add(RANKING_DAILY, member, score);
            redisTemplate.opsForZSet().add(RANKING_WEEKLY, member, score);
            redisTemplate.opsForZSet().add(RANKING_MONTHLY, member, score);
            redisTemplate.opsForZSet().add(RANKING_ALL, member, score);

            // 同时更新多维度排行榜 key
            String[] periods = { "day", "week", "month", "all" };
            for (String p : periods) {
                redisTemplate.opsForZSet().add(getRankingKey("hottest", p), member, score);
            }

            // 设置 TTL（仅在 key 不存在TTL时设置，避免每次覆盖）
            setTTLIfNeeded(RANKING_DAILY, 25, TimeUnit.HOURS);
            setTTLIfNeeded(RANKING_WEEKLY, 8, TimeUnit.DAYS);
            setTTLIfNeeded(RANKING_MONTHLY, 32, TimeUnit.DAYS);
            for (String p : new String[] { "day", "week", "month" }) {
                setTTLIfNeeded(getRankingKey("hottest", p), p.equals("day") ? 25 : p.equals("week") ? 192 : 768,
                        TimeUnit.HOURS);
            }

            logger.debug("排行榜分数已更新: artworkId={}, score={}", artworkId, score);
        } catch (Exception e) {
            logger.warn("更新排行榜分数失败: artworkId={}, error={}", artworkId, e.getMessage());
        }
    }

    /**
     * 更新作品的各维度指标排行榜
     * 
     * @param artworkId     作品ID
     * @param likeCount     点赞数
     * @param favoriteCount 收藏数
     * @param viewCount     浏览数
     */
    public void updateMetricScores(Long artworkId, int likeCount, int favoriteCount, long viewCount) {
        try {
            String member = String.valueOf(artworkId);
            String[] periods = { "day", "week", "month", "all" };
            for (String period : periods) {
                redisTemplate.opsForZSet().add(getRankingKey("most_liked", period), member, likeCount);
                redisTemplate.opsForZSet().add(getRankingKey("most_favorited", period), member, favoriteCount);
                redisTemplate.opsForZSet().add(getRankingKey("most_viewed", period), member, viewCount);
            }
            // 设置 TTL
            for (String metric : new String[] { "most_liked", "most_favorited", "most_viewed" }) {
                setTTLIfNeeded(getRankingKey(metric, "day"), 25, TimeUnit.HOURS);
                setTTLIfNeeded(getRankingKey(metric, "week"), 8, TimeUnit.DAYS);
                setTTLIfNeeded(getRankingKey(metric, "month"), 32, TimeUnit.DAYS);
            }
            logger.debug("多维度排行榜已更新: artworkId={}, likes={}, favs={}, views={}", artworkId, likeCount, favoriteCount,
                    viewCount);
        } catch (Exception e) {
            logger.warn("更新多维度排行榜失败: artworkId={}, error={}", artworkId, e.getMessage());
        }
    }

    private void setTTLIfNeeded(String key, long ttl, TimeUnit unit) {
        try {
            Long expire = redisTemplate.getExpire(key);
            if (expire == null || expire < 0) {
                redisTemplate.expire(key, ttl, unit);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 从排行榜移除作品（作品删除时调用）
     */
    public void removeFromRanking(Long artworkId) {
        try {
            String member = String.valueOf(artworkId);
            redisTemplate.opsForZSet().remove(RANKING_DAILY, member);
            redisTemplate.opsForZSet().remove(RANKING_WEEKLY, member);
            redisTemplate.opsForZSet().remove(RANKING_MONTHLY, member);
            redisTemplate.opsForZSet().remove(RANKING_ALL, member);
            // 移除多维度 key
            String[] sortTypes = { "hottest", "most_liked", "most_favorited", "most_viewed" };
            String[] periods = { "day", "week", "month", "all" };
            for (String s : sortTypes) {
                for (String p : periods) {
                    redisTemplate.opsForZSet().remove(getRankingKey(s, p), member);
                }
            }
            logger.debug("作品已从排行榜移除: artworkId={}", artworkId);
        } catch (Exception e) {
            logger.warn("从排行榜移除作品失败: artworkId={}, error={}", artworkId, e.getMessage());
        }
    }

    /**
     * 获取排行榜（从 Redis ZSet 读取）
     * 
     * @param period 时间范围 (day, week, month, all)
     * @param page   页码（从 1 开始）
     * @param size   每页大小
     * @return 作品 ID 列表（按分数倒序）
     */
    public List<Long> getRankingIds(String period, int page, int size) {
        String key = getKeyByPeriod(period);
        long start = (long) (page - 1) * size;
        long end = start + size - 1;

        try {
            Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(key,
                    start, end);

            if (tuples == null || tuples.isEmpty()) {
                return Collections.emptyList();
            }

            return tuples.stream()
                    .map(tuple -> Long.parseLong(String.valueOf(tuple.getValue())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("从 Redis 获取排行榜失败: period={}, error={}", period, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 获取排行榜总数
     */
    public long getRankingCount(String period) {
        String key = getKeyByPeriod(period);
        try {
            Long count = redisTemplate.opsForZSet().zCard(key);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取作品的排名（从 0 开始）
     */
    public Long getArtworkRank(Long artworkId, String period) {
        String key = getKeyByPeriod(period);
        try {
            return redisTemplate.opsForZSet().reverseRank(key, String.valueOf(artworkId));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 定时任务：每天凌晨3点全量刷新总榜
     * 
     * 日常排名更新已由 updateScore() 实时完成，
     * 此任务仅作兜底——修正可能的累积误差。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void refreshAllRanking() {
        logger.info("开始刷新总榜...");
        try {
            // 分批加载所有已发布作品
            int page = 0;
            int batchSize = 500;
            int totalUpdated = 0;

            while (true) {
                List<Artwork> artworks = artworkRepository.findByStatus(
                        ArtworkStatus.PUBLISHED,
                        org.springframework.data.domain.PageRequest.of(page, batchSize)).getContent();

                if (artworks.isEmpty()) {
                    break;
                }

                for (Artwork artwork : artworks) {
                    redisTemplate.opsForZSet().add(
                            RANKING_ALL,
                            String.valueOf(artwork.getId()),
                            artwork.getHotnessScore());
                }

                totalUpdated += artworks.size();
                page++;
            }

            logger.info("总榜刷新完成: 更新 {} 件作品", totalUpdated);
        } catch (Exception e) {
            logger.error("刷新总榜失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 定时任务：每天凌晨2点重建日/周/月榜
     * 
     * 从所有已发布作品中按热度排序写入对应的 Redis ZSet。
     * 日/周/月榜的区别仅在 TTL（过期时间）上，
     * 实时互动通过 updateScore() 持续更新分数。
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void rebuildPeriodRankings() {
        logger.info("开始重建日/周/月榜...");
        try {
            rebuildPeriodRanking("day", RANKING_DAILY, 25, TimeUnit.HOURS);
            rebuildPeriodRanking("week", RANKING_WEEKLY, 8, TimeUnit.DAYS);
            rebuildPeriodRanking("month", RANKING_MONTHLY, 32, TimeUnit.DAYS);

            // 同时重建多维度排行榜
            rebuildMultiDimensionRankings();

            logger.info("日/周/月榜重建完成");
        } catch (Exception e) {
            logger.error("重建周期排行榜失败: {}", e.getMessage(), e);
        }
    }

    private void rebuildPeriodRanking(String period, String key, long ttl, TimeUnit unit) {
        // 删除旧数据
        redisTemplate.delete(key);

        int page = 0;
        int batchSize = 500;
        int count = 0;

        while (true) {
            List<Artwork> artworks = artworkRepository.findByStatus(
                    ArtworkStatus.PUBLISHED,
                    org.springframework.data.domain.PageRequest.of(page, batchSize,
                            org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                                    "hotnessScore")))
                    .getContent();

            if (artworks.isEmpty())
                break;

            for (Artwork artwork : artworks) {
                redisTemplate.opsForZSet().add(key, String.valueOf(artwork.getId()), artwork.getHotnessScore());
            }
            count += artworks.size();
            page++;
        }

        redisTemplate.expire(key, ttl, unit);
        logger.info("{}榜重建完成: {} 件作品", period, count);
    }

    /**
     * 重建多维度排行榜 (hottest/most_liked/most_favorited/most_viewed × day/week/month/all)
     */
    private void rebuildMultiDimensionRankings() {
        String[] periods = { "day", "week", "month", "all" };
        int page = 0;
        int batchSize = 500;

        // 先删除所有多维度 key
        for (String period : periods) {
            for (String metric : new String[] { "hottest", "most_liked", "most_favorited", "most_viewed" }) {
                redisTemplate.delete(getRankingKey(metric, period));
            }
        }

        // 分批加载所有已发布作品
        while (true) {
            List<Artwork> artworks = artworkRepository.findByStatus(
                    ArtworkStatus.PUBLISHED,
                    org.springframework.data.domain.PageRequest.of(page, batchSize)).getContent();

            if (artworks.isEmpty())
                break;

            for (Artwork artwork : artworks) {
                String member = String.valueOf(artwork.getId());
                for (String period : periods) {
                    redisTemplate.opsForZSet().add(getRankingKey("hottest", period), member, artwork.getHotnessScore());
                    redisTemplate.opsForZSet().add(getRankingKey("most_liked", period), member, artwork.getLikeCount());
                    redisTemplate.opsForZSet().add(getRankingKey("most_favorited", period), member,
                            artwork.getFavoriteCount());
                    redisTemplate.opsForZSet().add(getRankingKey("most_viewed", period), member,
                            artwork.getViewCount());
                }
            }
            page++;
        }

        // 设置 TTL
        for (String metric : new String[] { "hottest", "most_liked", "most_favorited", "most_viewed" }) {
            redisTemplate.expire(getRankingKey(metric, "day"), 25, TimeUnit.HOURS);
            redisTemplate.expire(getRankingKey(metric, "week"), 8, TimeUnit.DAYS);
            redisTemplate.expire(getRankingKey(metric, "month"), 32, TimeUnit.DAYS);
        }
    }

    private String getKeyByPeriod(String period) {
        if (period == null)
            return RANKING_ALL;
        switch (period.toLowerCase()) {
            case "day":
                return RANKING_DAILY;
            case "week":
                return RANKING_WEEKLY;
            case "month":
                return RANKING_MONTHLY;
            case "all":
            default:
                return RANKING_ALL;
        }
    }
}
