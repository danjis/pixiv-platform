package com.pixiv.artwork.service;

import com.pixiv.artwork.entity.Artwork;
import com.pixiv.artwork.repository.ArtworkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 浏览计数服务（独立 Service 解决 @Async 自调用失效问题）
 *
 * 设计思路：
 * 1. 浏览时通过 Redis INCR 原子累加，而非直接写 DB + 删缓存
 * 2. 定时任务每 5 分钟批量回写 DB，避免每次浏览都触发数据库写入
 * 3. 作品详情缓存不再因浏览而被删除，缓存命中率大幅提升
 *
 * Redis Key：
 * - artwork:views:delta:{artworkId} → 自上次回写以来的增量浏览数（TTL 1小时）
 * - artwork:views:dirty → Set，记录有增量的 artworkId（用于批量回写时遍历）
 */
@Service
public class ViewCountService {

    private static final Logger logger = LoggerFactory.getLogger(ViewCountService.class);

    private static final String VIEW_DELTA_PREFIX = "artwork:views:delta:";
    private static final String VIEW_DIRTY_SET = "artwork:views:dirty";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private RankingService rankingService;

    /**
     * 异步增加浏览计数（通过 Redis INCR，不触发 DB 写入也不删缓存）
     */
    @Async
    public void incrementViewCountAsync(Long artworkId) {
        try {
            String deltaKey = VIEW_DELTA_PREFIX + artworkId;

            // 原子递增
            Long newDelta = redisTemplate.opsForValue().increment(deltaKey);

            // 首次设置 TTL（兜底，防止某次回写失败后 key 永不过期）
            if (newDelta != null && newDelta == 1) {
                redisTemplate.expire(deltaKey, 1, TimeUnit.HOURS);
            }

            // 记录到脏集合，回写时遍历
            redisTemplate.opsForSet().add(VIEW_DIRTY_SET, String.valueOf(artworkId));

            logger.debug("浏览计数已累加(Redis): artworkId={}, delta={}", artworkId, newDelta);
        } catch (Exception e) {
            logger.warn("Redis 浏览计数累加失败, 降级为直接写 DB: artworkId={}", artworkId);
            // 降级：直接写数据库（极少走到这里）
            fallbackIncrementInDB(artworkId);
        }
    }

    /**
     * 定时任务：每 5 分钟将 Redis 中的浏览增量批量回写到 DB
     */
    @Scheduled(fixedRate = 300_000) // 5 分钟
    public void flushViewCountsToDB() {
        try {
            Set<Object> dirtyIds = redisTemplate.opsForSet().members(VIEW_DIRTY_SET);
            if (dirtyIds == null || dirtyIds.isEmpty()) {
                return;
            }

            int flushed = 0;
            for (Object idObj : dirtyIds) {
                Long artworkId = Long.parseLong(String.valueOf(idObj));
                String deltaKey = VIEW_DELTA_PREFIX + artworkId;

                // 读取并重置增量（getAndDelete 原子操作）
                Object deltaObj = redisTemplate.opsForValue().getAndDelete(deltaKey);
                if (deltaObj == null) {
                    redisTemplate.opsForSet().remove(VIEW_DIRTY_SET, String.valueOf(artworkId));
                    continue;
                }

                long delta = Long.parseLong(String.valueOf(deltaObj));
                if (delta <= 0) {
                    redisTemplate.opsForSet().remove(VIEW_DIRTY_SET, String.valueOf(artworkId));
                    continue;
                }

                // 写入 DB
                Artwork artwork = artworkRepository.findById(artworkId).orElse(null);
                if (artwork != null) {
                    artwork.setViewCount(artwork.getViewCount() + (int) delta);
                    double score = HotnessCalculator.calculate(artwork);
                    artwork.setHotnessScore(score);
                    artworkRepository.save(artwork);

                    // 更新排行榜
                    rankingService.updateScore(artworkId, score);
                    flushed++;
                }

                // 从脏集合移除
                redisTemplate.opsForSet().remove(VIEW_DIRTY_SET, String.valueOf(artworkId));
            }

            if (flushed > 0) {
                logger.info("浏览计数批量回写完成: {} 件作品", flushed);
            }
        } catch (Exception e) {
            logger.error("浏览计数批量回写失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 降级：直接写数据库（Redis 不可用时）
     */
    private void fallbackIncrementInDB(Long artworkId) {
        try {
            Artwork artwork = artworkRepository.findById(artworkId).orElse(null);
            if (artwork != null) {
                artwork.incrementViewCount();
                double score = HotnessCalculator.calculate(artwork);
                artwork.setHotnessScore(score);
                artworkRepository.save(artwork);
                rankingService.updateScore(artworkId, score);
            }
        } catch (Exception e) {
            logger.error("降级写 DB 浏览计数失败: artworkId={}", artworkId, e);
        }
    }

    /**
     * 获取尚未回写到 DB 的实时浏览增量（Redis 中的 delta）
     *
     * @param artworkId 作品 ID
     * @return 未刷入 DB 的浏览增量，Redis 不可用时返回 0
     */
    public int getUnflushedDelta(Long artworkId) {
        try {
            String deltaKey = VIEW_DELTA_PREFIX + artworkId;
            Object deltaObj = redisTemplate.opsForValue().get(deltaKey);
            if (deltaObj != null) {
                return Integer.parseInt(String.valueOf(deltaObj));
            }
        } catch (Exception e) {
            logger.debug("读取浏览增量失败: artworkId={}", artworkId);
        }
        return 0;
    }
}
