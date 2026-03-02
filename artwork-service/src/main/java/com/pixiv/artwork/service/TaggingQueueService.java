package com.pixiv.artwork.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

/**
 * 智能打标调度服务
 * 
 * 在事务提交后异步调用 AutoTaggingService 进行智能打标。
 * 
 * 关键设计：调用方（createArtwork / updateArtwork）都在 @Transactional 内，
 * 如果直接 runAsync，异步线程会因为事务未提交而查不到作品记录。
 * 因此必须注册 TransactionSynchronization，在 afterCommit 回调中才启动异步任务。
 */
@Service
public class TaggingQueueService {

    private static final Logger logger = LoggerFactory.getLogger(TaggingQueueService.class);

    @Autowired
    private AutoTaggingService autoTaggingService;

    /**
     * 提交智能打标任务（事务提交后异步执行）
     * 
     * @param artworkId 作品 ID
     * @param imageUrl  图片 URL
     */
    public void sendTaggingRequest(Long artworkId, String imageUrl) {
        logger.info("注册智能打标任务（等待事务提交）: artworkId={}", artworkId);

        // 如果当前在事务中，等事务提交后再执行
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    executeTaggingAsync(artworkId, imageUrl);
                }
            });
        } else {
            // 没有活跃事务（理论上不会走到这里），直接异步执行
            executeTaggingAsync(artworkId, imageUrl);
        }
    }

    private void executeTaggingAsync(Long artworkId, String imageUrl) {
        logger.info("事务已提交，启动异步智能打标: artworkId={}", artworkId);
        CompletableFuture.runAsync(() -> {
            try {
                autoTaggingService.processTagging(artworkId, imageUrl);
                logger.info("智能打标完成: artworkId={}", artworkId);
            } catch (Exception e) {
                logger.error("智能打标失败（已降级）: artworkId={}, error={}", artworkId, e.getMessage(), e);
            }
        });
    }

    /**
     * 智能打标请求 DTO
     * 
     * 用于在消息队列中传递打标请求
     */
    public static class TaggingRequest implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long artworkId;
        private String imageUrl;

        public TaggingRequest() {
        }

        public TaggingRequest(Long artworkId, String imageUrl) {
            this.artworkId = artworkId;
            this.imageUrl = imageUrl;
        }

        // Getters and Setters

        public Long getArtworkId() {
            return artworkId;
        }

        public void setArtworkId(Long artworkId) {
            this.artworkId = artworkId;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        @Override
        public String toString() {
            return "TaggingRequest{" +
                    "artworkId=" + artworkId +
                    ", imageUrl='" + imageUrl + '\'' +
                    '}';
        }
    }
}
