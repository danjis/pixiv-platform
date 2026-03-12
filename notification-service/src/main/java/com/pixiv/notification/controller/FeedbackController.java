package com.pixiv.notification.controller;

import com.pixiv.notification.service.FeedbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用户反馈控制器
 * 提供反馈提交、查询、管理接口
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 用户提交反馈
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> submitFeedback(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> body) {
        try {
            String type = body.get("type");
            String title = body.get("title");
            String content = body.get("content");
            String contactInfo = body.get("contactInfo");

            Map<String, Object> result = feedbackService.submitFeedback(userId, type, title, content, contactInfo);
            return ResponseEntity.ok(wrapSuccess(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(wrapError(e.getMessage()));
        } catch (Exception e) {
            logger.error("提交反馈失败", e);
            return ResponseEntity.internalServerError().body(wrapError("提交失败，请稍后重试"));
        }
    }

    /**
     * 用户查看自己的反馈列表
     */
    @GetMapping("/mine")
    public ResponseEntity<Map<String, Object>> getMyFeedbacks(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Map<String, Object> result = feedbackService.getUserFeedbacks(userId, page, size);
        return ResponseEntity.ok(wrapSuccess(result));
    }

    /**
     * 管理员查看所有反馈
     */
    @GetMapping("/admin/list")
    public ResponseEntity<Map<String, Object>> getAllFeedbacks(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "status", required = false) String status) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(wrapError("无权限操作"));
        }
        Map<String, Object> result = feedbackService.getAllFeedbacks(page, size, status);
        return ResponseEntity.ok(wrapSuccess(result));
    }

    /**
     * 管理员回复反馈
     */
    @PutMapping("/admin/{feedbackId}/reply")
    public ResponseEntity<Map<String, Object>> replyFeedback(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable("feedbackId") Long feedbackId,
            @RequestBody Map<String, String> body) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(wrapError("无权限操作"));
        }
        try {
            String reply = body.get("reply");
            String newStatus = body.getOrDefault("status", "RESOLVED");
            Map<String, Object> result = feedbackService.replyFeedback(feedbackId, reply, newStatus);
            return ResponseEntity.ok(wrapSuccess(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(wrapError(e.getMessage()));
        }
    }

    private Map<String, Object> wrapSuccess(Object data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", data);
        return result;
    }

    private Map<String, Object> wrapError(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 400);
        result.put("message", message);
        result.put("data", null);
        return result;
    }
}
