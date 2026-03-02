package com.pixiv.commission.controller;

import com.pixiv.commission.dto.*;
import com.pixiv.commission.service.ConversationService;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 私信/对话控制器
 */
@Tag(name = "私信管理", description = "私信对话、消息收发接口")
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private static final Logger logger = LoggerFactory.getLogger(ConversationController.class);

    @Autowired
    private ConversationService conversationService;

    /**
     * 创建或获取与目标用户的对话
     */
    @Operation(summary = "创建/获取对话", description = "如果已存在对话则直接返回，否则新建")
    @PostMapping
    public ResponseEntity<Result<ConversationDTO>> createOrGetConversation(
            @Valid @RequestBody CreateConversationRequest request,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            ConversationDTO dto = conversationService.createOrGetConversation(userId, request.getTargetUserId());
            return ResponseEntity.ok(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("创建对话失败: userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("创建对话失败"));
        }
    }

    /**
     * 获取我的对话列表
     */
    @Operation(summary = "我的对话列表")
    @GetMapping
    public ResponseEntity<Result<List<ConversationDTO>>> getMyConversations(
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            List<ConversationDTO> list = conversationService.getMyConversations(userId);
            return ResponseEntity.ok(Result.success(list));
        } catch (Exception e) {
            logger.error("获取对话列表失败: userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取对话列表失败"));
        }
    }

    /**
     * 获取对话详情
     */
    @Operation(summary = "获取对话详情")
    @GetMapping("/{conversationId}")
    public ResponseEntity<Result<ConversationDTO>> getConversation(
            @PathVariable(value = "conversationId") Long conversationId,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            ConversationDTO dto = conversationService.getConversation(userId, conversationId);
            return ResponseEntity.ok(Result.success(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 获取对话内的消息列表（分页）
     */
    @Operation(summary = "获取对话消息")
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<Result<Page<PrivateMessageDTO>>> getMessages(
            @PathVariable(value = "conversationId") Long conversationId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            Page<PrivateMessageDTO> messages = conversationService.getMessages(userId, conversationId, page, size);
            return ResponseEntity.ok(Result.success(messages));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    /**
     * 发送私信
     */
    @Operation(summary = "发送私信")
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<Result<PrivateMessageDTO>> sendMessage(
            @PathVariable(value = "conversationId") Long conversationId,
            @Valid @RequestBody SendPrivateMessageRequest request,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            PrivateMessageDTO msg = conversationService.sendMessage(userId, conversationId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(msg));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("发送私信失败: convId={}, userId={}", conversationId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("发送消息失败"));
        }
    }

    /**
     * 标记对话已读
     */
    @Operation(summary = "标记对话已读")
    @PutMapping("/{conversationId}/read")
    public ResponseEntity<Result<Void>> markAsRead(
            @PathVariable(value = "conversationId") Long conversationId,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            conversationService.markAsRead(userId, conversationId);
            return ResponseEntity.ok(Result.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }
}
