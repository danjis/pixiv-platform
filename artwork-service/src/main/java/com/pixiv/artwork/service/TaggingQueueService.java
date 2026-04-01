package com.pixiv.artwork.service;

import com.pixiv.artwork.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;

/**
 * 智能打标调度服务
 * 
 * 在事务提交后通过 RabbitMQ 发送打标消息，由 TaggingConsumer 异步消费处理。
 * 
 * 关键设计：调用方（createArtwork / updateArtwork）都在 @Transactional 内，
 * 如果直接发送消息，消费者可能因为事务未提交而查不到作品记录。
 * 因此必须注册 TransactionSynchronization，在 afterCommit 回调中才发送消息。
 */
@Service
public class TaggingQueueService {

    private static final Logger logger = LoggerFactory.getLogger(TaggingQueueService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 提交智能打标任务（事务提交后发送 RabbitMQ 消息）
     * 
     * @param artworkId 作品 ID
     * @param imageUrl  图片 URL
     */
    public void sendTaggingRequest(Long artworkId, String imageUrl) {
        logger.info("注册智能打标任务（等待事务提交）: artworkId={}", artworkId);

        // 如果当前在事务中，等事务提交后再发送消息
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendToQueue(artworkId, imageUrl);
                }
            });
        } else {
            // 没有活跃事务（理论上不会走到这里），直接发送
            sendToQueue(artworkId, imageUrl);
        }
    }

    private void sendToQueue(Long artworkId, String imageUrl) {
        try {
            TaggingRequest request = new TaggingRequest(artworkId, imageUrl);
            rabbitTemplate.convertAndSend(RabbitMQConfig.TAGGING_QUEUE, request);
            logger.info("智能打标消息已发送至队列: artworkId={}", artworkId);
        } catch (Exception e) {
            logger.error("发送智能打标消息失败（已降级）: artworkId={}, error={}", artworkId, e.getMessage(), e);
        }
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
