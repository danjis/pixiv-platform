package com.pixiv.artwork.service;

import com.pixiv.artwork.entity.Artwork;
import com.pixiv.artwork.entity.ArtworkStatus;
import com.pixiv.artwork.entity.BrowsingHistory;
import com.pixiv.artwork.repository.ArtworkRepository;
import com.pixiv.artwork.repository.BrowsingHistoryRepository;
import com.pixiv.common.dto.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 浏览记录服务
 * 提供浏览记录的记录、查询、删除等功能
 */
@Service
public class BrowsingHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(BrowsingHistoryService.class);

    @Autowired
    private BrowsingHistoryRepository browsingHistoryRepository;

    @Autowired
    private ArtworkRepository artworkRepository;

    /**
     * 异步记录浏览历史（upsert 模式：存在则更新时间，不存在则插入）
     */
    @Async
    @Transactional
    public void recordView(Long userId, Long artworkId) {
        if (userId == null || artworkId == null)
            return;
        try {
            Optional<BrowsingHistory> existing = browsingHistoryRepository.findByUserIdAndArtworkId(userId, artworkId);
            if (existing.isPresent()) {
                BrowsingHistory history = existing.get();
                history.setViewedAt(LocalDateTime.now());
                browsingHistoryRepository.save(history);
            } else {
                BrowsingHistory history = new BrowsingHistory();
                history.setUserId(userId);
                history.setArtworkId(artworkId);
                history.setViewedAt(LocalDateTime.now());
                browsingHistoryRepository.save(history);
            }
            logger.debug("记录浏览历史: userId={}, artworkId={}", userId, artworkId);
        } catch (Exception e) {
            // 浏览记录失败不应影响主流程
            logger.warn("记录浏览历史失败: userId={}, artworkId={}, error={}", userId, artworkId, e.getMessage());
        }
    }

    /**
     * 获取用户浏览记录（分页，附带作品信息）
     */
    public PageResult<Map<String, Object>> getUserHistory(Long userId, int page, int size) {
        Page<BrowsingHistory> historyPage = browsingHistoryRepository
                .findByUserIdOrderByViewedAtDesc(userId, PageRequest.of(page, size));

        if (historyPage.isEmpty()) {
            return PageResult.empty(page + 1, size);
        }

        // 批量获取作品信息
        List<Long> artworkIds = historyPage.getContent().stream()
                .map(BrowsingHistory::getArtworkId)
                .collect(Collectors.toList());

        Map<Long, Artwork> artworkMap = artworkRepository.findAllById(artworkIds).stream()
                .collect(Collectors.toMap(Artwork::getId, a -> a, (a, b) -> a));

        // 组装结果
        List<Map<String, Object>> records = historyPage.getContent().stream()
                .map(h -> {
                    Map<String, Object> record = new LinkedHashMap<>();
                    record.put("id", h.getId());
                    record.put("artworkId", h.getArtworkId());
                    record.put("viewedAt", h.getViewedAt());

                    Artwork artwork = artworkMap.get(h.getArtworkId());
                    if (artwork != null && artwork.getStatus() == ArtworkStatus.PUBLISHED) {
                        record.put("title", artwork.getTitle());
                        record.put("thumbnailUrl", artwork.getThumbnailUrl());
                        record.put("imageUrl", artwork.getImageUrl());
                        record.put("artistId", artwork.getArtistId());
                        record.put("artistName", artwork.getArtistName());
                        record.put("artistAvatar", artwork.getArtistAvatar());
                        record.put("likeCount", artwork.getLikeCount());
                        record.put("viewCount", artwork.getViewCount());
                        record.put("isAvailable", true);
                    } else {
                        record.put("title", "作品已删除或不可用");
                        record.put("isAvailable", false);
                    }
                    return record;
                })
                .collect(Collectors.toList());

        return new PageResult<>(records, historyPage.getTotalElements(), page + 1, size);
    }

    /**
     * 删除单条浏览记录
     */
    @Transactional
    public void deleteHistory(Long userId, Long historyId) {
        browsingHistoryRepository.deleteByIdAndUserId(historyId, userId);
    }

    /**
     * 清空用户所有浏览记录
     */
    @Transactional
    public void clearAllHistory(Long userId) {
        browsingHistoryRepository.deleteAllByUserId(userId);
        logger.info("清空用户浏览记录: userId={}", userId);
    }

    /**
     * 获取用户浏览记录数
     */
    public long getHistoryCount(Long userId) {
        return browsingHistoryRepository.countByUserId(userId);
    }
}
