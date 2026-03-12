package com.pixiv.notification.service;

import com.pixiv.notification.entity.Feedback;
import com.pixiv.notification.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户反馈服务
 */
@Service
public class FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);

    @Autowired
    private FeedbackRepository feedbackRepository;

    private static final List<String> VALID_TYPES = Arrays.asList(
            "BUG_REPORT", "FEATURE_REQUEST", "COMPLAINT", "CONSULTATION", "OTHER");

    /**
     * 提交反馈
     */
    @Transactional
    public Map<String, Object> submitFeedback(Long userId, String type, String title, String content,
            String contactInfo) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("反馈标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("反馈内容不能为空");
        }
        if (type == null || !VALID_TYPES.contains(type)) {
            type = "OTHER";
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setType(type);
        feedback.setTitle(title.trim());
        feedback.setContent(content.trim());
        feedback.setContactInfo(contactInfo);
        feedback.setStatus("PENDING");
        feedbackRepository.save(feedback);

        logger.info("用户反馈提交成功: userId={}, type={}, title={}", userId, type, title);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", feedback.getId());
        result.put("title", feedback.getTitle());
        result.put("status", feedback.getStatus());
        result.put("createdAt", feedback.getCreatedAt());
        return result;
    }

    /**
     * 获取用户的反馈列表
     */
    public Map<String, Object> getUserFeedbacks(Long userId, int page, int size) {
        Page<Feedback> pageResult = feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId,
                PageRequest.of(page, size));
        return buildPageResult(pageResult, page, size);
    }

    /**
     * 获取所有反馈（管理端）
     */
    public Map<String, Object> getAllFeedbacks(int page, int size, String status) {
        Page<Feedback> pageResult;
        if (status != null && !status.isEmpty()) {
            pageResult = feedbackRepository.findByStatusOrderByCreatedAtDesc(status, PageRequest.of(page, size));
        } else {
            pageResult = feedbackRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        }

        Map<String, Object> result = buildPageResult(pageResult, page, size);
        result.put("pendingCount", feedbackRepository.countByStatus("PENDING"));
        result.put("processingCount", feedbackRepository.countByStatus("PROCESSING"));
        return result;
    }

    /**
     * 管理员回复反馈
     */
    @Transactional
    public Map<String, Object> replyFeedback(Long feedbackId, String reply, String newStatus) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("反馈不存在: " + feedbackId));

        if (reply != null && !reply.trim().isEmpty()) {
            feedback.setAdminReply(reply.trim());
            feedback.setRepliedAt(LocalDateTime.now());
        }
        if (newStatus != null) {
            feedback.setStatus(newStatus);
        }
        feedbackRepository.save(feedback);

        logger.info("管理员回复反馈: feedbackId={}, status={}", feedbackId, newStatus);
        return convertToMap(feedback);
    }

    private Map<String, Object> buildPageResult(Page<Feedback> pageResult, int page, int size) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (Feedback f : pageResult.getContent()) {
            records.add(convertToMap(f));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", pageResult.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", pageResult.getTotalPages());
        return result;
    }

    private Map<String, Object> convertToMap(Feedback f) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", f.getId());
        map.put("userId", f.getUserId());
        map.put("type", f.getType());
        map.put("title", f.getTitle());
        map.put("content", f.getContent());
        map.put("contactInfo", f.getContactInfo());
        map.put("status", f.getStatus());
        map.put("adminReply", f.getAdminReply());
        map.put("repliedAt", f.getRepliedAt());
        map.put("createdAt", f.getCreatedAt());
        map.put("updatedAt", f.getUpdatedAt());
        return map;
    }
}
