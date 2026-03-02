package com.pixiv.artwork.listener;

import com.pixiv.artwork.service.AutoTaggingService;
import com.pixiv.artwork.service.TaggingQueueService.TaggingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 智能打标消息消费者
 * 
 * 监听 RabbitMQ 队列，接收作品打标请求并调用 AI 服务进行处理
 */
@Component
public class TaggingConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(TaggingConsumer.class);
    
    @Autowired
    private AutoTaggingService autoTaggingService;
    
    /**
     * 处理智能打标请求
     * 
     * 从 RabbitMQ 队列中接收消息，调用 AI 服务获取标签并保存到数据库
     * 
     * 降级策略：
     * - AI 服务不可用时不重试，避免阻塞队列
     * - 失败只记录错误日志，不影响其他消息处理
     * - 用户可以手动添加标签作为补偿
     * 
     * @param request 打标请求（包含作品 ID 和图片 URL）
     */
    @RabbitListener(queues = "artwork.tagging")
    public void handleTaggingRequest(TaggingRequest request) {
        logger.info("收到智能打标请求: artworkId={}, imageUrl={}", 
                request.getArtworkId(), request.getImageUrl());
        
        try {
            // 调用自动打标服务处理请求
            // AutoTaggingService 内部已实现降级策略
            autoTaggingService.processTagging(request.getArtworkId(), request.getImageUrl());
            
            logger.info("智能打标处理成功: artworkId={}", request.getArtworkId());
            
        } catch (Exception e) {
            // 降级策略：失败不重试，避免阻塞队列
            // 记录详细错误日志，便于排查问题
            logger.error("智能打标处理失败（已降级，不重试）: artworkId={}, imageUrl={}, error={}", 
                    request.getArtworkId(), request.getImageUrl(), e.getMessage(), e);
            
            // 不抛出异常，消息会被确认并从队列中移除
            // 这样不会阻塞后续消息的处理
            // 用户可以手动添加标签作为补偿
        }
    }
}
