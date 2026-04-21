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
            @RequestBody Map<String, Object> body) {
        try {
            String type = body.get("type") != null ? body.get("type").toString() : null;
            String title = body.get("title") != null ? body.get("title").toString() : null;
            String content = body.get("content") != null ? body.get("content").toString() : null;
            String contactInfo = body.get("contactInfo") != null ? body.get("contactInfo").toString() : null;

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
     * 用户提交售后/申诉申请
     */
    @PostMapping("/after-sales")
    public ResponseEntity<Map<String, Object>> submitAfterSale(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> body) {
        try {
            Long commissionId = body.get("commissionId") != null
                    ? Long.valueOf(body.get("commissionId").toString())
                    : null;
            Long paymentId = body.get("paymentId") != null
                    ? Long.valueOf(body.get("paymentId").toString())
                    : null;
            String requestedAction = body.get("requestedAction") != null
                    ? body.get("requestedAction").toString()
                    : null;
            String title = body.get("title") != null ? body.get("title").toString() : null;
            String content = body.get("content") != null ? body.get("content").toString() : null;
            String contactInfo = body.get("contactInfo") != null ? body.get("contactInfo").toString() : null;

            Map<String, Object> result = feedbackService.submitAfterSale(
                    userId, commissionId, paymentId, requestedAction, title, content, contactInfo);
            return ResponseEntity.ok(wrapSuccess(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(wrapError(e.getMessage()));
        } catch (Exception e) {
            logger.error("提交售后申请失败", e);
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
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "commissionId", required = false) Long commissionId) {
        Map<String, Object> result = feedbackService.getUserFeedbacks(userId, page, size, type, commissionId);
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
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(wrapError("无权限操作"));
        }
        Map<String, Object> result = feedbackService.getAllFeedbacks(page, size, status, type);
        return ResponseEntity.ok(wrapSuccess(result));
    }

    /**
     * 管理员回复反馈
     */
    @PutMapping("/admin/{feedbackId}/reply")
    public ResponseEntity<Map<String, Object>> replyFeedback(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable("feedbackId") Long feedbackId,
            @RequestBody Map<String, Object> body) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(wrapError("无权限操作"));
        }
        try {
            String reply = body.get("reply") != null ? body.get("reply").toString() : null;
            String newStatus = body.get("status") != null ? body.get("status").toString() : "RESOLVED";
            Map<String, Object> result = feedbackService.replyFeedback(feedbackId, reply, newStatus);
            return ResponseEntity.ok(wrapSuccess(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(wrapError(e.getMessage()));
        }
    }

    /**
     * 管理员处理售后单
     */
    @PutMapping("/admin/{feedbackId}/after-sale")
    public ResponseEntity<Map<String, Object>> processAfterSale(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) Long adminId,
            @PathVariable("feedbackId") Long feedbackId,
            @RequestBody Map<String, Object> body) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(wrapError("无权限操作"));
        }
        try {
            String action = body.get("action") != null ? body.get("action").toString() : null;
            String reply = body.get("reply") != null ? body.get("reply").toString() : null;
            String refundMode = body.get("refundMode") != null ? body.get("refundMode").toString() : null;
            Map<String, Object> result = feedbackService.processAfterSale(feedbackId, adminId, action, reply, refundMode);
            return ResponseEntity.ok(wrapSuccess(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(wrapError(e.getMessage()));
        } catch (Exception e) {
            logger.error("处理售后单失败", e);
            return ResponseEntity.internalServerError().body(wrapError("处理失败，请稍后重试"));
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
