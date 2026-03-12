package com.pixiv.artwork.service;

import com.pixiv.artwork.dto.AddCommentRequest;
import com.pixiv.artwork.dto.CommentDTO;
import com.pixiv.artwork.entity.Artwork;
import com.pixiv.artwork.entity.ArtworkStatus;
import com.pixiv.artwork.entity.Comment;
import com.pixiv.artwork.feign.UserServiceClient;
import com.pixiv.artwork.feign.MembershipServiceClient;
import com.pixiv.artwork.repository.ArtworkRepository;
import com.pixiv.artwork.repository.CommentRepository;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UserDTO;
import com.pixiv.common.exception.BusinessException;
import com.pixiv.common.constant.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评价服务
 * 处理作品评价功能
 */
@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private MembershipServiceClient membershipServiceClient;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String NOTIFICATION_QUEUE = "notification.create";

    /**
     * 添加评价
     * 
     * @param userId    用户 ID
     * @param artworkId 作品 ID
     * @param request   评价请求
     * @return 评价 DTO
     * @throws IllegalArgumentException 如果作品不存在或评价内容为空
     */
    @Transactional
    public CommentDTO addComment(Long userId, Long artworkId, AddCommentRequest request) {
        logger.info("用户添加评价: userId={}, artworkId={}, parentId={}, content={}",
                userId, artworkId, request.getParentId(), request.getContent());

        // 1. 验证评价内容非空
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评价内容不能为空");
        }

        // 2. 验证作品存在
        Artwork artwork = artworkRepository.findByIdAndStatus(artworkId, ArtworkStatus.PUBLISHED)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTWORK_NOT_FOUND, "作品不存在或未发布"));

        // 3. 如果是回复，验证父评论存在
        Long replyToUserId = null;
        if (request.getParentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND, "父评论不存在"));
            // 确保父评论属于同一作品
            if (!parentComment.getArtworkId().equals(artworkId)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "父评论不属于该作品");
            }
            replyToUserId = parentComment.getUserId();
        }

        // 4. 创建评价记录
        Comment comment = new Comment();
        comment.setArtworkId(artworkId);
        comment.setUserId(userId);
        comment.setContent(request.getContent().trim());
        comment.setParentId(request.getParentId());
        comment.setReplyToUserId(replyToUserId);
        comment = commentRepository.save(comment);

        // 5. 增加作品评价计数
        artwork.incrementCommentCount();
        artworkRepository.save(artwork);

        logger.info("评价添加成功: commentId={}, userId={}, artworkId={}, parentId={}, newCommentCount={}",
                comment.getId(), userId, artworkId, request.getParentId(), artwork.getCommentCount());

        // 6. 获取用户信息并转换为 DTO
        CommentDTO dto = convertToDTO(comment);

        // 7. 发送通知
        if (request.getParentId() != null && replyToUserId != null && !userId.equals(replyToUserId)) {
            // 回复评论 → 通知被回复的用户
            sendNotification(replyToUserId, "COMMENT_REPLIED",
                    "你的评论收到了一条回复",
                    "/artworks/" + artworkId);
        }
        // 通知作品画师（不通知自己评论自己的作品，且不重复通知）
        if (!userId.equals(artwork.getArtistId())
                && (replyToUserId == null || !artwork.getArtistId().equals(replyToUserId))) {
            sendNotification(artwork.getArtistId(), "ARTWORK_COMMENTED",
                    "你的作品「" + artwork.getTitle() + "」收到了一条新评论",
                    "/artworks/" + artworkId);
        }

        // 8. 更新热度分数
        updateHotnessScore(artwork);

        return dto;
    }

    /**
     * 删除评价
     * 
     * @param userId    用户 ID
     * @param commentId 评价 ID
     * @throws IllegalArgumentException 如果评价不存在或无权删除
     */
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        logger.info("用户删除评价: userId={}, commentId={}", userId, commentId);

        // 1. 查询评价
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND, "评价不存在"));

        // 2. 验证评价所有权（只能删除自己的评价）
        if (!comment.getUserId().equals(userId)) {
            logger.warn("用户无权删除评价: userId={}, commentId={}, commentUserId={}",
                    userId, commentId, comment.getUserId());
            throw new BusinessException(ErrorCode.NOT_COMMENT_OWNER, "您无权删除该评价");
        }

        // 3. 删除评价记录
        commentRepository.delete(comment);

        // 4. 减少作品评价计数
        Artwork artwork = artworkRepository.findById(comment.getArtworkId())
                .orElse(null);
        if (artwork != null) {
            artwork.decrementCommentCount();
            artworkRepository.save(artwork);
        }

        logger.info("评价删除成功: commentId={}, userId={}, artworkId={}",
                commentId, userId, comment.getArtworkId());

        // 5. 更新热度分数
        if (artwork != null) {
            updateHotnessScore(artwork);
        }
    }

    /**
     * 获取作品的评价列表
     * 
     * @param artworkId 作品 ID
     * @param page      页码（从 1 开始）
     * @param size      每页大小
     * @return 评价分页结果
     */
    public PageResult<CommentDTO> getComments(Long artworkId, int page, int size) {
        logger.info("获取作品评价列表: artworkId={}, page={}, size={}", artworkId, page, size);

        // 1. 验证作品存在
        if (!artworkRepository.existsById(artworkId)) {
            throw new BusinessException(ErrorCode.ARTWORK_NOT_FOUND, "作品不存在");
        }

        // 2. 查询顶级评论（parentId IS NULL），按创建时间倒序
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Comment> topLevelPage = commentRepository.findTopLevelComments(artworkId, pageable);

        List<CommentDTO> topLevelDTOs = topLevelPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 3. 批量加载子回复
        List<Long> topLevelIds = topLevelPage.getContent().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        if (!topLevelIds.isEmpty()) {
            List<Comment> allReplies = commentRepository.findByParentIdIn(topLevelIds);

            // 按父评论 ID 分组
            Map<Long, List<CommentDTO>> repliesMap = allReplies.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.groupingBy(CommentDTO::getParentId));

            // 设置每条顶级评论的回复列表
            for (CommentDTO dto : topLevelDTOs) {
                dto.setReplies(repliesMap.getOrDefault(dto.getId(), java.util.Collections.emptyList()));
            }

            // 批量获取所有回复的用户信息
            List<CommentDTO> allReplyDTOs = repliesMap.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            enrichWithUserInfo(allReplyDTOs);

            // 填充 replyToUsername
            enrichReplyToUsername(allReplyDTOs);
        }

        // 4. 批量获取顶级评论的用户信息
        enrichWithUserInfo(topLevelDTOs);

        return new PageResult<>(
                topLevelDTOs,
                topLevelPage.getTotalElements(),
                page,
                size);
    }

    /**
     * 补充回复评论的 replyToUsername 字段
     */
    private void enrichReplyToUsername(List<CommentDTO> replyDTOs) {
        List<Long> replyToUserIds = replyDTOs.stream()
                .map(CommentDTO::getReplyToUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (replyToUserIds.isEmpty())
            return;

        Map<Long, String> usernameMap = new HashMap<>();
        try {
            Result<java.util.List<UserDTO>> batchResult = userServiceClient.getUsersByIds(replyToUserIds);
            if (batchResult != null && batchResult.isSuccess() && batchResult.getData() != null) {
                for (UserDTO u : batchResult.getData()) {
                    usernameMap.put(u.getId(), u.getUsername());
                }
            }
        } catch (Exception e) {
            logger.warn("批量获取被回复用户名失败: {}", e.getMessage());
        }
        // 批量接口未返回全部结果时，逐个补查缺失的用户
        if (usernameMap.size() < replyToUserIds.size()) {
            for (Long uid : replyToUserIds) {
                if (usernameMap.containsKey(uid))
                    continue;
                try {
                    Result<UserDTO> result = userServiceClient.getUserById(uid);
                    if (result != null && result.isSuccess() && result.getData() != null) {
                        usernameMap.put(uid, result.getData().getUsername());
                    }
                } catch (Exception ex) {
                    logger.warn("获取被回复用户名失败: userId={}", uid);
                }
            }
        }

        for (CommentDTO dto : replyDTOs) {
            if (dto.getReplyToUserId() != null) {
                dto.setReplyToUsername(usernameMap.getOrDefault(dto.getReplyToUserId(), "未知用户"));
            }
        }
    }

    /**
     * 转换评价实体为 DTO
     * 
     * @param comment 评价实体
     * @return 评价 DTO
     */
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setArtworkId(comment.getArtworkId());
        dto.setUserId(comment.getUserId());
        dto.setContent(comment.getContent());
        dto.setParentId(comment.getParentId());
        dto.setReplyToUserId(comment.getReplyToUserId());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }

    /**
     * 批量填充用户信息
     * 
     * @param dtos 评价 DTO 列表
     */
    private void enrichWithUserInfo(List<CommentDTO> dtos) {
        if (dtos.isEmpty()) {
            return;
        }

        // 获取所有用户 ID
        List<Long> userIds = dtos.stream()
                .map(CommentDTO::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息（使用批量接口避免 N+1 问题）
        Map<Long, UserDTO> userMap = new HashMap<>();
        try {
            Result<java.util.List<UserDTO>> batchResult = userServiceClient.getUsersByIds(userIds);
            if (batchResult != null && batchResult.isSuccess() && batchResult.getData() != null) {
                for (UserDTO u : batchResult.getData()) {
                    userMap.put(u.getId(), u);
                }
            }
        } catch (Exception e) {
            logger.warn("批量获取用户信息失败, 降级为逐个查询: {}", e.getMessage());
        }
        // 批量接口未返回全部结果时，逐个补查缺失的用户
        if (userMap.size() < userIds.size()) {
            for (Long userId : userIds) {
                if (userMap.containsKey(userId))
                    continue;
                try {
                    Result<UserDTO> result = userServiceClient.getUserById(userId);
                    if (result != null && result.isSuccess() && result.getData() != null) {
                        userMap.put(userId, result.getData());
                    }
                } catch (Exception ex) {
                    logger.warn("获取用户信息失败: userId={}, error={}", userId, ex.getMessage());
                }
            }
        }

        // 填充用户信息
        for (CommentDTO dto : dtos) {
            UserDTO user = userMap.get(dto.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setAvatarUrl(user.getAvatarUrl());
            } else {
                dto.setUsername("未知用户");
                dto.setAvatarUrl(null);
            }
        }

        // 批量获取会员等级信息（用于 VIP 徽章和评论高亮）
        enrichWithMembershipLevel(dtos, userIds);
    }

    /**
     * 批量填充用户会员等级
     */
    private void enrichWithMembershipLevel(List<CommentDTO> dtos, List<Long> userIds) {
        Map<Long, String> membershipMap = new HashMap<>();
        for (Long userId : userIds) {
            try {
                Result<Map<String, Object>> result = membershipServiceClient.getMembership(userId);
                if (result != null && result.isSuccess() && result.getData() != null) {
                    Map<String, Object> data = result.getData();
                    String level = (String) data.get("level");
                    boolean expired = data.get("expired") != null && (Boolean) data.get("expired");
                    if (level != null && !expired) {
                        membershipMap.put(userId, level);
                    } else {
                        membershipMap.put(userId, "NORMAL");
                    }
                } else {
                    membershipMap.put(userId, "NORMAL");
                }
            } catch (Exception e) {
                logger.debug("获取用户会员等级失败: userId={}", userId);
                membershipMap.put(userId, "NORMAL");
            }
        }

        for (CommentDTO dto : dtos) {
            dto.setMembershipLevel(membershipMap.getOrDefault(dto.getUserId(), "NORMAL"));
        }
    }

    /**
     * 更新作品热度分数
     */
    private void updateHotnessScore(Artwork artwork) {
        try {
            double score = HotnessCalculator.calculate(artwork);
            artwork.setHotnessScore(score);
            artworkRepository.save(artwork);
            logger.debug("热度分数已更新: artworkId={}, score={}", artwork.getId(), score);

            // 同步更新排行榜
            rankingService.updateScore(artwork.getId(), score);
        } catch (Exception e) {
            logger.warn("更新热度分数失败: artworkId={}, error={}", artwork.getId(), e.getMessage());
        }
    }

    /**
     * 发送通知消息到 RabbitMQ
     */
    private void sendNotification(Long userId, String type, String content, String linkUrl) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("type", type);
            message.put("content", content);
            message.put("linkUrl", linkUrl);
            rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            logger.warn("发送通知失败: userId={}, type={}, error={}", userId, type, e.getMessage());
        }
    }
}
