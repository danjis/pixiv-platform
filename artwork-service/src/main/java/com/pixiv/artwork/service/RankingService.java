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
 * 
 * Redis Key 设计：
 * - artwork:ranking:daily → 日榜（TTL 25小时）
 * - artwork:ranking:weekly → 周榜（TTL 8天）
 * - artwork:ranking:monthly → 月榜（TTL 32天）
 * - artwork:ranking:all → 总榜（无TTL，定时刷新）
 * 
 * 面试重点：
 * 1. ZADD 实现 O(logN) 实时排名更新
 * 2. ZREVRANGE 实现 O(logN+M) 的Top-K查询
 * 3. 通过 TTL 自动实现日/周/月榜的轮转
 */
@Service
public class RankingService {

    private static final Logger logger = LoggerFactory.getLogger(RankingService.class);

    private static final String RANKING_PREFIX = "artwork:ranking:";
    private static final String RANKING_DAILY = RANKING_PREFIX + "daily";
    private static final String RANKING_WEEKLY = RANKING_PREFIX + "weekly";
    private static final String RANKING_MONTHLY = RANKING_PREFIX + "monthly";
    private static final String RANKING_ALL = RANKING_PREFIX + "all";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ArtworkRepository artworkRepository;

    /**
     * 更新作品在各排行榜中的分数
     * 
     * 每次互动（点赞/收藏/评论/浏览）后调用此方法。
     * 
     * @param artworkId 作品 ID
     * @param score     新的热度分数
     */
    public void updateScore(Long artworkId, double score) {
        try {
            String member = String.valueOf(artworkId);
            // 同时更新所有时间维度的排行榜
            redisTemplate.opsForZSet().add(RANKING_DAILY, member, score);
            redisTemplate.opsForZSet().add(RANKING_WEEKLY, member, score);
            redisTemplate.opsForZSet().add(RANKING_MONTHLY, member, score);
            redisTemplate.opsForZSet().add(RANKING_ALL, member, score);

            // 设置 TTL（仅在 key 不存在TTL时设置，避免每次覆盖）
            if (redisTemplate.getExpire(RANKING_DAILY) == null || redisTemplate.getExpire(RANKING_DAILY) < 0) {
                redisTemplate.expire(RANKING_DAILY, 25, TimeUnit.HOURS);
            }
            if (redisTemplate.getExpire(RANKING_WEEKLY) == null || redisTemplate.getExpire(RANKING_WEEKLY) < 0) {
                redisTemplate.expire(RANKING_WEEKLY, 8, TimeUnit.DAYS);
            }
            if (redisTemplate.getExpire(RANKING_MONTHLY) == null || redisTemplate.getExpire(RANKING_MONTHLY) < 0) {
                redisTemplate.expire(RANKING_MONTHLY, 32, TimeUnit.DAYS);
            }

            logger.debug("排行榜分数已更新: artworkId={}, score={}", artworkId, score);
        } catch (Exception e) {
            logger.warn("更新排行榜分数失败: artworkId={}, error={}", artworkId, e.getMessage());
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
     * 判断排行榜缓存是否存在且有效
     */
    public boolean isRankingAvailable(String period) {
        String key = getKeyByPeriod(period);
        try {
            Long size = redisTemplate.opsForZSet().zCard(key);
            return size != null && size > 0;
        } catch (Exception e) {
            return false;
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
     * 根据时间范围从数据库筛选作品并写入对应的 Redis ZSet。
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void rebuildPeriodRankings() {
        logger.info("开始重建日/周/月榜...");
        try {
            rebuildPeriodRanking("day", RANKING_DAILY, 25, TimeUnit.HOURS);
            rebuildPeriodRanking("week", RANKING_WEEKLY, 8, TimeUnit.DAYS);
            rebuildPeriodRanking("month", RANKING_MONTHLY, 32, TimeUnit.DAYS);
            logger.info("日/周/月榜重建完成");
        } catch (Exception e) {
            logger.error("重建周期排行榜失败: {}", e.getMessage(), e);
        }
    }

    private void rebuildPeriodRanking(String period, String key, long ttl, TimeUnit unit) {
        LocalDateTime startTime = getStartTime(period);
        LocalDateTime endTime = LocalDateTime.now();

        // 删除旧数据
        redisTemplate.delete(key);

        int page = 0;
        int batchSize = 500;
        int count = 0;

        while (true) {
            List<Artwork> artworks = artworkRepository.findByCreatedAtBetween(
                    startTime, endTime, ArtworkStatus.PUBLISHED,
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

    private LocalDateTime getStartTime(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period.toLowerCase()) {
            case "day":
                return now.minusDays(1);
            case "week":
                return now.minusWeeks(1);
            case "month":
                return now.minusMonths(1);
            default:
                return now.minusDays(1);
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
