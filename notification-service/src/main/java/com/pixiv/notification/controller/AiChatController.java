package com.pixiv.notification.controller;

import com.pixiv.common.dto.Result;
import com.pixiv.notification.dto.AiChatMessageDTO;
import com.pixiv.notification.dto.AiChatRequest;
import com.pixiv.notification.dto.AiChatSessionDTO;
import com.pixiv.notification.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "AI 智能客服", description = "AI 聊天会话、消息管理")
@RestController
@RequestMapping("/api/ai-chat")
public class AiChatController {

    private static final Logger logger = LoggerFactory.getLogger(AiChatController.class);

    @Autowired
    private AiChatService aiChatService;

    @Operation(summary = "创建新会话")
    @PostMapping("/sessions")
    public ResponseEntity<Result<AiChatSessionDTO>> createSession(
            @RequestHeader("X-User-Id") Long userId) {
        AiChatSessionDTO session = aiChatService.createSession(userId);
        return ResponseEntity.ok(Result.success(session));
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/sessions")
    public ResponseEntity<Result<List<AiChatSessionDTO>>> getSessions(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        List<AiChatSessionDTO> sessions = aiChatService.getUserSessions(userId, page, size);
        return ResponseEntity.ok(Result.success(sessions));
    }

    @Operation(summary = "获取会话消息记录")
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Result<List<AiChatMessageDTO>>> getMessages(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long sessionId) {
        List<AiChatMessageDTO> messages = aiChatService.getSessionMessages(userId, sessionId);
        return ResponseEntity.ok(Result.success(messages));
    }

    @Operation(summary = "发送消息并获取AI回复")
    @PostMapping("/send")
    public ResponseEntity<Result<Map<String, Object>>> sendMessage(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AiChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Result.error("消息内容不能为空"));
        }
        if (request.getSessionId() == null) {
            return ResponseEntity.badRequest().body(Result.error("会话ID不能为空"));
        }

        logger.info("AI 客服消息: userId={}, sessionId={}, message={}",
                userId, request.getSessionId(), request.getMessage());

        Map<String, Object> result = aiChatService.sendMessageWithSuggestions(
                userId, request.getSessionId(), request.getMessage().trim());
        return ResponseEntity.ok(Result.success(result));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Result<Void>> deleteSession(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long sessionId) {
        aiChatService.deleteSession(userId, sessionId);
        return ResponseEntity.ok(Result.success());
    }
}
