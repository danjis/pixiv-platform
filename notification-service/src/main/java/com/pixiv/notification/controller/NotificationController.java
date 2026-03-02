package com.pixiv.notification.controller;

import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import com.pixiv.notification.dto.NotificationDTO;
import com.pixiv.notification.dto.UnreadCountDTO;
import com.pixiv.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 * 
 * 处理通知相关的 HTTP 请求
 */
@Tag(name = "通知管理", description = "通知查询、标记已读、删除等接口")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 获取通知列表
     */
    @Operation(
        summary = "获取通知列表",
        description = "查询当前用户的通知列表，支持分页。\n\n" +
                "**功能说明：**\n" +
                "- 按创建时间倒序排列（最新的在前）\n" +
                "- 支持分页查询\n" +
                "- 返回通知的详细信息（类型、内容、链接、已读状态等）\n\n" +
                "**示例：**\n" +
                "- 获取第一页: /api/notifications?page=1&size=20"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping
    public ResponseEntity<Result<PageResult<NotificationDTO>>> getNotifications(
            @Parameter(description = "当前用户 ID", required = true, example = "1")
            @RequestHeader(value = "X-User-Id") Long userId,
            
            @Parameter(description = "页码（从 1 开始）", required = true, example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            
            @Parameter(description = "每页大小", required = true, example = "20")
            @RequestParam(value = "size", defaultValue = "20") int size) {
        
        logger.info("收到获取通知列表请求: userId={}, page={}, size={}", userId, page, size);
        
        try {
            // 参数验证
            if (page < 1) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Result.error("页码必须大于 0"));
            }
            if (size < 1 || size > 100) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Result.error("每页大小必须在 1-100 之间"));
            }
            
            PageResult<NotificationDTO> result = notificationService.getNotifications(userId, page, size);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("获取通知列表失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("查询失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取未读通知数量
     */
    @Operation(
        summary = "获取未读通知数量",
        description = "查询当前用户的未读通知数量。\n\n" +
                "**功能说明：**\n" +
                "- 返回未读通知的总数\n" +
                "- 用于在导航栏显示未读消息提示\n\n" +
                "**示例：**\n" +
                "- 获取未读数量: /api/notifications/unread-count"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping("/unread-count")
    public ResponseEntity<Result<UnreadCountDTO>> getUnreadCount(
            @Parameter(description = "当前用户 ID", required = true, example = "1")
            @RequestHeader(value = "X-User-Id") Long userId) {
        
        logger.info("收到获取未读通知数量请求: userId={}", userId);
        
        try {
            long count = notificationService.getUnreadCount(userId);
            UnreadCountDTO dto = new UnreadCountDTO(count);
            return ResponseEntity.ok(Result.success(dto));
        } catch (Exception e) {
            logger.error("获取未读通知数量失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("查询失败: " + e.getMessage()));
        }
    }
    
    /**
     * 标记通知为已读
     */
    @Operation(
        summary = "标记通知为已读",
        description = "将指定的通知标记为已读状态。\n\n" +
                "**功能说明：**\n" +
                "- 只能标记自己的通知\n" +
                "- 标记后未读数量会减少\n\n" +
                "**示例：**\n" +
                "- 标记通知 1 为已读: PUT /api/notifications/1/read"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "标记成功"),
        @ApiResponse(responseCode = "404", description = "通知不存在"),
        @ApiResponse(responseCode = "403", description = "无权操作此通知"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Result<Void>> markAsRead(
            @Parameter(description = "通知 ID", required = true, example = "1")
            @PathVariable(value = "notificationId") Long notificationId,
            
            @Parameter(description = "当前用户 ID", required = true, example = "1")
            @RequestHeader(value = "X-User-Id") Long userId) {
        
        logger.info("收到标记通知为已读请求: notificationId={}, userId={}", notificationId, userId);
        
        try {
            notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok(Result.success(null));
        } catch (IllegalArgumentException e) {
            logger.error("标记通知为已读失败: notificationId={}, error={}", notificationId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("标记通知为已读失败: notificationId={}, error={}", notificationId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("操作失败: " + e.getMessage()));
        }
    }
    
    /**
     * 标记所有通知为已读
     */
    @Operation(
        summary = "标记所有通知为已读",
        description = "将当前用户的所有未读通知标记为已读。\n\n" +
                "**功能说明：**\n" +
                "- 批量标记所有未读通知\n" +
                "- 标记后未读数量变为 0\n\n" +
                "**示例：**\n" +
                "- 标记所有为已读: PUT /api/notifications/read-all"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "标记成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @PutMapping("/read-all")
    public ResponseEntity<Result<Void>> markAllAsRead(
            @Parameter(description = "当前用户 ID", required = true, example = "1")
            @RequestHeader(value = "X-User-Id") Long userId) {
        
        logger.info("收到标记所有通知为已读请求: userId={}", userId);
        
        try {
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(Result.success(null));
        } catch (Exception e) {
            logger.error("标记所有通知为已读失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("操作失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除通知
     */
    @Operation(
        summary = "删除通知",
        description = "删除指定的通知。\n\n" +
                "**功能说明：**\n" +
                "- 只能删除自己的通知\n" +
                "- 删除后无法恢复\n\n" +
                "**示例：**\n" +
                "- 删除通知 1: DELETE /api/notifications/1"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "404", description = "通知不存在"),
        @ApiResponse(responseCode = "403", description = "无权操作此通知"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Result<Void>> deleteNotification(
            @Parameter(description = "通知 ID", required = true, example = "1")
            @PathVariable(value = "notificationId") Long notificationId,
            
            @Parameter(description = "当前用户 ID", required = true, example = "1")
            @RequestHeader(value = "X-User-Id") Long userId) {
        
        logger.info("收到删除通知请求: notificationId={}, userId={}", notificationId, userId);
        
        try {
            notificationService.deleteNotification(notificationId, userId);
            return ResponseEntity.ok(Result.success(null));
        } catch (IllegalArgumentException e) {
            logger.error("删除通知失败: notificationId={}, error={}", notificationId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("删除通知失败: notificationId={}, error={}", notificationId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("操作失败: " + e.getMessage()));
        }
    }
}
