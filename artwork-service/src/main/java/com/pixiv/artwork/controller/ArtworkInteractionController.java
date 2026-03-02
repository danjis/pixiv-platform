package com.pixiv.artwork.controller;

import com.pixiv.artwork.dto.AddCommentRequest;
import com.pixiv.artwork.dto.CommentDTO;
import com.pixiv.artwork.service.ArtworkInteractionService;
import com.pixiv.artwork.service.CommentService;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 作品互动控制器
 * 处理作品的点赞、收藏、评价等社交互动功能
 */
@Tag(name = "作品互动", description = "作品点赞、收藏、评价等互动接口")
@RestController
@RequestMapping("/api/artworks/{artworkId}")
public class ArtworkInteractionController {
    
    private static final Logger logger = LoggerFactory.getLogger(ArtworkInteractionController.class);
    
    @Autowired
    private ArtworkInteractionService artworkInteractionService;
    
    @Autowired
    private CommentService commentService;
    
    /**
     * 点赞作品
     */
    @Operation(
        summary = "点赞作品",
        description = "用户点赞作品。需要用户认证。\n\n" +
                "**功能说明：**\n" +
                "- 每个用户对同一作品只能点赞一次\n" +
                "- 点赞后会增加作品的点赞计数\n" +
                "- 会通知作品的画师\n\n" +
                "**示例：**\n" +
                "- POST /api/artworks/1/like"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "点赞成功"),
        @ApiResponse(responseCode = "400", description = "参数错误或已点赞"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "404", description = "作品不存在")
    })
    @PostMapping("/like")
    public ResponseEntity<Result<Void>> likeArtwork(
            @Parameter(description = "作品 ID", required = true, example = "1")
            @PathVariable("artworkId") Long artworkId,
            
            @Parameter(description = "当前用户 ID", required = true, example = "1")
            @RequestHeader(value = "X-User-Id") Long userId) {
        
        logger.info("收到点赞作品请求: userId={}, artworkId={}", userId, artworkId);
        
        try {
            artworkInteractionService.likeArtwork(userId, artworkId);
            return ResponseEntity.ok(Result.success("点赞成功", null));
        } catch (IllegalArgumentException e) {
            logger.warn("点赞作品失败: userId={}, artworkId={}, error={}", 
                    userId, artworkId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("点赞作品失败: userId={}, artworkId={}, error={}", 
                    userId, artworkId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("点赞失败: " + e.getMessage()));
        }
    }
    
    /**
     * 取消点赞作品
     */
    @Operation(
        summary = "取消点赞作品",
        description = "用户取消点赞作品。需要用户认证。\n\n" +
                "**功能说明：**\n" +
                "- 只能取消自己的点赞\n" +
                "- 取消点赞后会减少作品的点赞计数\n\n" +
                "**示例：**\n" +
                "- DELETE /api/artworks/1/like"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "取消点赞成功"),
        @ApiResponse(responseCode = "400", description = "参数错误或未点赞"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "404", description = "作品不存在")
    })
    @DeleteMapping("/like")
    public ResponseEntity<Result<Void>> unlikeArtwork(
            @Parameter(description = "作品 ID", required = true, example = "1")
            @PathVariable("artworkId") Long artworkId,
            
            @Parameter(description = "当前用户 ID", required = true, example = "1")
            @RequestHeader(value = "X-User-Id") Long userId) {
        
        logger.info("收到取消点赞作品请求: userId={}, artworkId={}", userId, artworkId);
        
        try {
            artworkInteractionService.unlikeArtwork(userId, artworkId);
            return ResponseEntity.ok(Result.success("取消点赞成功", null));
        } catch (IllegalArgumentException e) {
            logger.warn("取消点赞作品失败: userId={}, artworkId={}, error={}", 
                    userId, artworkId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("取消点赞作品失败: userId={}, artworkId={}, error={}", 
                    userId, artworkId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("取消点赞失败: " + e.getMessage()));
        }
    }
    
    /**
     * 收藏作品
     */
    @Operation(
        summary = "收藏作品",
        description = "用户收藏作品。需要用户认证。\n\n" +
                "**功能说明：**\n" +
                "- 每个用户对同一作品只能收藏一次\n" +
                "- 收藏后会增加作品的收藏计数\n" +
                "- 会通知作品的画师\n" +
                "- 收藏的作品会显示在用户的收藏列表中\n\n" +
                "**示例：**\n" +
                "- POST /api/artworks/1/favorite"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "收藏成功"),
        @ApiResponse(responseCode = "400", description = "参数错误或已收藏"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "404", description = "作品不存在")
    })
    @PostMapping("/favorite")
    public ResponseEntity<Result<Void>> favoriteArtwork(
            @Parameter(description = "作品 ID", required = true, example = "1")
            @PathVariable("artworkId") Long artworkId,
            
            @Parameter(description = "当前用户 ID", required = true, example = "1")
            @RequestHeader(value = "X-User-Id") Long userId) {
        
        logger.info("收到收藏作品请求: userId={}, artworkId={}", userId, artworkId);
        
        try {
            artworkInteractionService.favoriteArtwork(userId, artworkId);
            return ResponseEntity.ok(Result.success("收藏成功", null));
        } catch (IllegalArgumentException e) {
            logger.warn("收藏作品失败: userId={}, artworkId={}, error={}", 
                    userId, artworkId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("收藏作品失败: userId={}, artworkId={}, error={}", 
                    userId, artworkId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("收藏失败: " + e.getMessage()));
        }
    }
    
    /**
     * 取消收藏作品
     */
    @Operation(
        summary = "取消收藏作品",
        description = "用户取消收藏作品。需要用户认证。\n\n" +
                "**功能说明：**\n" +
                "- 只能取消自己的收藏\n" +
                "- 取消收藏后会减少作品的收藏计数\n" +
                "- 作品会从用户的收藏列表中移除\n\n" +
                "**示例：**\n" +
                "- DELETE /api/artworks/1/favorite"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "取消收藏成功"),
        @ApiResponse(responseCode = "400", description = "参数错误或未收藏"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "404", description = "作品不存在")
    })
    @DeleteMapping("/favorite")
    public ResponseEntity<Result<Void>> unfavoriteArtwork(
            @Parameter(description = "作品 ID", required = true, example = "1")
            @PathVariable("artworkId") Long artworkId,
            
            @Parameter(description = "当前用户 ID", required = true, example = "1")
            @RequestHeader(value = "X-User-Id") Long userId) {
        
        logger.info("收到取消收藏作品请求: userId={}, artworkId={}", userId, artworkId);
        
        try {
            artworkInteractionService.unfavoriteArtwork(userId, artworkId);
            return ResponseEntity.ok(Result.success("取消收藏成功", null));
        } catch (IllegalArgumentException e) {
            logger.warn("取消收藏作品失败: userId={}, artworkId={}, error={}", 
                    userId, artworkId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("取消收藏作品失败: userId={}, artworkId={}, error={}", 
                    userId, artworkId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("取消收藏失败: " + e.getMessage()));
        }
    }
    
    /**
     * 发表评价
     */
    @Operation(
        summary = "发表评价",
        description = "用户对作品发表评价。需要用户认证。\n\n" +
                "**功能说明：**\n" +
                "- 评价内容不能为空，最多 1000 个字符\n" +
                "- 评价后会增加作品的评价计数\n" +
                "- 会通知作品的画师\n" +
                "- 评价会显示在作品详情页\n\n" +
                "**示例：**\n" +
                "- POST /api/artworks/1/comments\n" +
                "- Body: {\"content\": \"画得真好！\"}"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "评价成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "404", description = "作品不存在")
    })
    @PostMapping("/comments")
    public ResponseEntity<Result<CommentDTO>> addComment(
            @Parameter(description = "作品 ID", required = true, example = "1")
            @PathVariable("artworkId") Long artworkId,
            
            @Parameter(description = "评价请求", required = true)
            @Valid @RequestBody AddCommentRequest request,
            
            @Parameter(description = "当前用户 ID", required = true, example = "1")
            @RequestHeader(value = "X-User-Id") Long userId) {
        
        logger.info("收到发表评价请求: userId={}, artworkId={}, content={}", 
                userId, artworkId, request.getContent());
        
        try {
            CommentDTO comment = commentService.addComment(userId, artworkId, request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Result.success("评价成功", comment));
        } catch (IllegalArgumentException e) {
            logger.warn("发表评价失败: userId={}, artworkId={}, error={}", 
                    userId, artworkId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("发表评价失败: userId={}, artworkId={}, error={}", 
                    userId, artworkId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("评价失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取评价列表
     */
    @Operation(
        summary = "获取评价列表",
        description = "获取作品的评价列表，支持分页。\n\n" +
                "**功能说明：**\n" +
                "- 按评价时间倒序排列（最新的在前）\n" +
                "- 返回评价内容、用户信息、时间等\n" +
                "- 支持分页查询\n\n" +
                "**示例：**\n" +
                "- GET /api/artworks/1/comments?page=1&size=20"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "404", description = "作品不存在")
    })
    @GetMapping("/comments")
    public ResponseEntity<Result<PageResult<CommentDTO>>> getComments(
            @Parameter(description = "作品 ID", required = true, example = "1")
            @PathVariable("artworkId") Long artworkId,
            
            @Parameter(description = "页码（从 1 开始）", required = true, example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            
            @Parameter(description = "每页大小", required = true, example = "20")
            @RequestParam(value = "size", defaultValue = "20") int size) {
        
        logger.info("收到获取评价列表请求: artworkId={}, page={}, size={}", artworkId, page, size);
        
        try {
            PageResult<CommentDTO> result = commentService.getComments(artworkId, page, size);
            return ResponseEntity.ok(Result.success(result));
        } catch (IllegalArgumentException e) {
            logger.warn("获取评价列表失败: artworkId={}, error={}", artworkId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("获取评价列表失败: artworkId={}, error={}", artworkId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("查询失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除评价
     */
    @Operation(
        summary = "删除评价",
        description = "用户删除自己的评价。需要用户认证。\n\n" +
                "**功能说明：**\n" +
                "- 只能删除自己发表的评价\n" +
                "- 删除后会减少作品的评价计数\n" +
                "- 画师和管理员也可以删除不当评价\n\n" +
                "**示例：**\n" +
                "- DELETE /api/artworks/1/comments/123"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "400", description = "参数错误或无权删除"),
        @ApiResponse(responseCode = "401", description = "未认证"),
        @ApiResponse(responseCode = "404", description = "评价不存在")
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Result<Void>> deleteComment(
            @Parameter(description = "作品 ID", required = true, example = "1")
            @PathVariable("artworkId") Long artworkId,
            
            @Parameter(description = "评价 ID", required = true, example = "123")
            @PathVariable("commentId") Long commentId,
            
            @Parameter(description = "当前用户 ID", required = true, example = "1")
            @RequestHeader(value = "X-User-Id") Long userId) {
        
        logger.info("收到删除评价请求: userId={}, artworkId={}, commentId={}", 
                userId, artworkId, commentId);
        
        try {
            commentService.deleteComment(userId, commentId);
            return ResponseEntity.ok(Result.success("删除成功", null));
        } catch (IllegalArgumentException e) {
            logger.warn("删除评价失败: userId={}, commentId={}, error={}", 
                    userId, commentId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("删除评价失败: userId={}, commentId={}, error={}", 
                    userId, commentId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("删除失败: " + e.getMessage()));
        }
    }
}
