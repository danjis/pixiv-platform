package com.pixiv.artwork.listener;

import com.pixiv.artwork.entity.Artwork;
import com.pixiv.artwork.entity.ArtworkStatus;
import com.pixiv.artwork.repository.ArtworkRepository;
import com.pixiv.artwork.service.ArtworkSearchService;
import com.pixiv.artwork.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Elasticsearch 数据同步消费者
 *
 * 监听 artwork.es.sync 队列，根据消息中的操作类型进行 ES 索引的增删改：
 * - INDEX: 索引/更新作品到 ES
 * - DELETE: 从 ES 删除作品索引
 *
 * 降级策略：ES 同步失败只记录错误日志，不影响主业务流程
 */
@Component
public class EsSyncConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EsSyncConsumer.class);

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private ArtworkSearchService artworkSearchService;

    /**
     * 处理 ES 同步消息
     *
     * 消息格式：{"artworkId": 123, "action": "INDEX"} 或 {"artworkId": 123, "action":
     * "DELETE"}
     *
     * @param message 同步消息
     */
    @RabbitListener(queues = RabbitMQConfig.ES_SYNC_QUEUE)
    public void handleEsSync(Map<String, Object> message) {
        Long artworkId = null;
        String action = null;

        try {
            artworkId = ((Number) message.get("artworkId")).longValue();
            action = (String) message.get("action");

            logger.info("收到ES同步消息: artworkId={}, action={}", artworkId, action);

            switch (action) {
                case "INDEX":
                    handleIndex(artworkId);
                    break;
                case "DELETE":
                    handleDelete(artworkId);
                    break;
                default:
                    logger.warn("未知的ES同步操作: action={}", action);
            }
        } catch (Exception e) {
            // ES 同步失败不应阻塞消息队列，只记录错误
            logger.error("ES同步处理失败: artworkId={}, action={}, error={}",
                    artworkId, action, e.getMessage(), e);
        }
    }

    /**
     * 索引/更新作品到 ES
     */
    private void handleIndex(Long artworkId) {
        Artwork artwork = artworkRepository.findById(artworkId).orElse(null);
        if (artwork == null) {
            logger.warn("作品不存在，跳过索引: artworkId={}", artworkId);
            return;
        }

        // 只索引已发布的作品
        if (artwork.getStatus() != ArtworkStatus.PUBLISHED) {
            logger.info("作品非发布状态，跳过索引: artworkId={}, status={}", artworkId, artwork.getStatus());
            // 如果 ES 中有旧索引则删除
            artworkSearchService.deleteIndex(artworkId);
            return;
        }

        artworkSearchService.indexArtwork(artwork);
        logger.info("ES索引同步成功: artworkId={}", artworkId);
    }

    /**
     * 从 ES 删除作品索引
     */
    private void handleDelete(Long artworkId) {
        artworkSearchService.deleteIndex(artworkId);
        logger.info("ES索引删除成功: artworkId={}", artworkId);
    }
}
