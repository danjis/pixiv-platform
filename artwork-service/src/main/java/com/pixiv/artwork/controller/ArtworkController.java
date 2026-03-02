package com.pixiv.artwork.controller;

import com.pixiv.artwork.dto.ArtworkDTO;
import com.pixiv.artwork.dto.ArtworkDetailDTO;
import com.pixiv.artwork.dto.CreateArtworkRequest;
import com.pixiv.artwork.dto.UpdateArtworkRequest;
import com.pixiv.artwork.service.ArtworkService;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作品控制器
 * 
 * 处理作品相关的 HTTP 请求
 */
@Tag(name = "作品管理", description = "作品发布、查询、编辑、删除等接口")
@RestController
@RequestMapping("/api/artworks")
public class ArtworkController {

        private static final Logger logger = LoggerFactory.getLogger(ArtworkController.class);

        @Autowired
        private ArtworkService artworkService;

        /**
         * 创建作品（发布作品）
         * 
         * @param request  创建作品请求
         * @param artistId 画师 ID（从 JWT 令牌中获取，这里暂时用请求头模拟）
         * @return 创建的作品信息
         */
        @Operation(summary = "发布作品", description = "画师发布新作品。需要画师身份认证。\n\n" +
                        "**X-User-Id 说明：**\n" +
                        "- 这是当前用户的 ID（必须是画师）\n" +
                        "- 可以从登录响应的 user.id 字段获取\n" +
                        "- 或者调用用户服务的 GET /api/users/me 接口查看\n\n" +
                        "**发布流程：**\n" +
                        "1. 先调用文件服务上传图片，获取 imageUrl 和 thumbnailUrl\n" +
                        "2. 填写作品信息（标题、描述、图片URL、标签等）\n" +
                        "3. 提交发布请求\n" +
                        "4. 系统会自动发送到智能打标队列进行 AI 标签识别")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "作品创建成功"),
                        @ApiResponse(responseCode = "400", description = "参数错误或用户不是画师"),
                        @ApiResponse(responseCode = "401", description = "未认证"),
                        @ApiResponse(responseCode = "500", description = "服务器错误")
        })
        @PostMapping
        public ResponseEntity<Result<ArtworkDTO>> createArtwork(
                        @Parameter(description = "作品信息", required = true) @Valid @RequestBody CreateArtworkRequest request,

                        @Parameter(description = "当前用户 ID（必须是画师）\n\n" +
                                        "**如何获取：**\n" +
                                        "1. 登录后从响应的 user.id 获取\n" +
                                        "2. 或调用 GET /api/users/me 查看\n\n" +
                                        "**示例值：** 1", required = true, example = "1") @RequestHeader("X-User-Id") Long artistId) {

                logger.info("收到创建作品请求: artistId={}, title={}", artistId, request.getTitle());

                try {
                        ArtworkDTO artwork = artworkService.createArtwork(artistId, request);
                        logger.info("作品创建成功: artworkId={}", artwork.getId());

                        return ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .body(Result.success(artwork));

                } catch (Exception e) {
                        logger.error("作品创建失败: artistId={}, error={}", artistId, e.getMessage(), e);
                        return ResponseEntity
                                        .status(HttpStatus.BAD_REQUEST)
                                        .body(Result.error(e.getMessage()));
                }
        }

        /**
         * 根据 ID 获取作品详情
         * 
         * @param artworkId 作品 ID
         * @param userId    当前用户 ID（可选，用于查询点赞收藏状态）
         * @return 作品详情
         */
        @Operation(summary = "获取作品详情", description = "根据作品 ID 查询作品的详细信息，包括标签、统计数据等。\n\n" +
                        "**返回信息包括：**\n" +
                        "- 作品基本信息（标题、描述、图片等）\n" +
                        "- 画师信息\n" +
                        "- 标签列表（包含手动标签和 AI 自动标签）\n" +
                        "- 统计数据（浏览量、点赞数、收藏数等）\n\n" +
                        "**缓存机制：**\n" +
                        "- 作品详情会缓存 1 小时\n" +
                        "- 浏览计数异步更新，不影响响应速度")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "查询成功"),
                        @ApiResponse(responseCode = "404", description = "作品不存在"),
                        @ApiResponse(responseCode = "500", description = "服务器错误")
        })
        @GetMapping("/{artworkId:\\d+}")
        public ResponseEntity<Result<ArtworkDetailDTO>> getArtwork(
                        @Parameter(description = "作品 ID", required = true, example = "1") @PathVariable("artworkId") Long artworkId,

                        @Parameter(description = "当前用户 ID（可选，用于查询点赞收藏状态）", example = "1") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
                logger.info("收到获取作品详情请求: artworkId={}, userId={}", artworkId, userId);

                try {
                        ArtworkDetailDTO artwork = artworkService.getArtwork(artworkId, userId);
                        return ResponseEntity.ok(Result.success(artwork));

                } catch (Exception e) {
                        logger.error("获取作品详情失败: artworkId={}, error={}", artworkId, e.getMessage(), e);
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(Result.error(e.getMessage()));
                }
        }

        /**
         * 获取作品列表（支持分页、搜索、筛选、排序）
         * 
         * @param keyword 搜索关键词（可选）
         * @param tags    标签列表（可选）
         * @param sortBy  排序方式（可选）
         * @param page    页码（从 1 开始）
         * @param size    每页大小
         * @return 作品列表
         */
        @Operation(summary = "获取作品列表", description = "查询作品列表，支持分页、搜索、筛选和排序。\n\n" +
                        "**搜索功能：**\n" +
                        "- keyword: 搜索标题和描述中包含关键词的作品\n\n" +
                        "**筛选功能：**\n" +
                        "- tags: 按标签筛选，支持多个标签（交集）\n\n" +
                        "**排序方式：**\n" +
                        "- latest: 最新发布（默认）\n" +
                        "- hottest: 最热门（按热度分数）\n" +
                        "- most_liked: 最多点赞\n" +
                        "- most_favorited: 最多收藏\n" +
                        "- most_viewed: 最多浏览\n\n" +
                        "**示例：**\n" +
                        "- 搜索关键词: /api/artworks?keyword=风景&page=1&size=20\n" +
                        "- 按标签筛选: /api/artworks?tags=动漫,少女&page=1&size=20\n" +
                        "- 按热度排序: /api/artworks?sortBy=hottest&page=1&size=20")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "查询成功"),
                        @ApiResponse(responseCode = "400", description = "参数错误"),
                        @ApiResponse(responseCode = "500", description = "服务器错误")
        })
        @GetMapping
        public ResponseEntity<Result<PageResult<ArtworkDTO>>> getArtworks(
                        @Parameter(description = "搜索关键词（搜索标题、描述和画师名）", example = "风景") @RequestParam(value = "keyword", required = false) String keyword,

                        @Parameter(description = "标签列表（多个标签用逗号分隔，取交集）", example = "动漫,少女") @RequestParam(value = "tags", required = false) List<String> tags,

                        @Parameter(description = "排序方式", example = "latest", schema = @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {
                                        "latest", "hottest", "most_liked", "most_favorited",
                                        "most_viewed" })) @RequestParam(value = "sortBy", required = false, defaultValue = "latest") String sortBy,

                        @Parameter(description = "画师ID（按画师筛选）", example = "1") @RequestParam(value = "artistId", required = false) Long artistId,

                        @Parameter(description = "是否AI生成（true=仅AI, false=仅人工, 不传=全部）") @RequestParam(value = "isAigc", required = false) Boolean isAigc,

                        @Parameter(description = "开始日期（yyyy-MM-dd）", example = "2024-01-01") @RequestParam(value = "dateFrom", required = false) String dateFrom,

                        @Parameter(description = "结束日期（yyyy-MM-dd）", example = "2024-12-31") @RequestParam(value = "dateTo", required = false) String dateTo,

                        @Parameter(description = "最低点赞数", example = "10") @RequestParam(value = "minLikes", required = false) Integer minLikes,

                        @Parameter(description = "页码（从 1 开始）", required = true, example = "1") @RequestParam(value = "page", defaultValue = "1") int page,

                        @Parameter(description = "每页大小", required = true, example = "20") @RequestParam(value = "size", defaultValue = "20") int size) {

                logger.info("收到获取作品列表请求: keyword={}, tags={}, sortBy={}, artistId={}, isAigc={}, dateFrom={}, dateTo={}, minLikes={}, page={}, size={}",
                                keyword, tags, sortBy, artistId, isAigc, dateFrom, dateTo, minLikes, page, size);

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

                        PageResult<ArtworkDTO> result = artworkService.getArtworks(
                                        keyword, tags, sortBy, page, size, artistId, isAigc, dateFrom, dateTo,
                                        minLikes);
                        return ResponseEntity.ok(Result.success(result));

                } catch (Exception e) {
                        logger.error("获取作品列表失败: error={}", e.getMessage(), e);
                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Result.error("查询失败: " + e.getMessage()));
                }
        }

        /**
         * 获取关注动态流（Feed）
         * 
         * @param userId 当前用户 ID（从请求头获取）
         * @param page   页码（从 1 开始）
         * @param size   每页大小
         * @return 关注画师的作品列表
         */
        @Operation(summary = "获取关注动态", description = "获取当前用户关注的画师发布的作品，按时间倒序。\n\n" +
                        "**功能说明：**\n" +
                        "- 需要登录\n" +
                        "- 返回关注画师的最新作品\n" +
                        "- 按发布时间倒序排列\n" +
                        "- 支持分页查询")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "查询成功"),
                        @ApiResponse(responseCode = "401", description = "未认证"),
                        @ApiResponse(responseCode = "500", description = "服务器错误")
        })
        @GetMapping("/feed")
        public ResponseEntity<Result<PageResult<ArtworkDTO>>> getFeedArtworks(
                        @Parameter(description = "当前用户 ID", required = true, example = "1") @RequestHeader(value = "X-User-Id") Long userId,

                        @Parameter(description = "页码（从 1 开始）", example = "1") @RequestParam(value = "page", defaultValue = "1") int page,

                        @Parameter(description = "每页大小", example = "20") @RequestParam(value = "size", defaultValue = "20") int size) {

                logger.info("收到获取关注动态请求: userId={}, page={}, size={}", userId, page, size);

                try {
                        if (page < 1) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("页码必须大于 0"));
                        }
                        if (size < 1 || size > 100) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("每页大小必须在 1-100 之间"));
                        }

                        PageResult<ArtworkDTO> result = artworkService.getFeedArtworks(userId, page, size);
                        return ResponseEntity.ok(Result.success(result));

                } catch (Exception e) {
                        logger.error("获取关注动态失败: userId={}, error={}", userId, e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Result.error("查询失败: " + e.getMessage()));
                }
        }

        /**
         * 获取排行榜
         * 
         * @param sortBy 排序方式
         * @param period 时间范围
         * @param page   页码（从 1 开始）
         * @param size   每页大小
         * @return 排行榜作品列表
         */
        @Operation(summary = "获取排行榜", description = "获取作品排行榜，支持多种排序和时间范围。\n\n" +
                        "**排序方式：**\n" +
                        "- hottest: 最热门（按热度分数，默认）\n" +
                        "- most_liked: 最多点赞\n" +
                        "- most_favorited: 最多收藏\n" +
                        "- most_viewed: 最多浏览\n\n" +
                        "**时间范围：**\n" +
                        "- day: 24小时内\n" +
                        "- week: 一周内\n" +
                        "- month: 一个月内\n" +
                        "- all: 全部时间（默认）")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "查询成功"),
                        @ApiResponse(responseCode = "400", description = "参数错误"),
                        @ApiResponse(responseCode = "500", description = "服务器错误")
        })
        @GetMapping("/ranking")
        public ResponseEntity<Result<PageResult<ArtworkDTO>>> getRankingArtworks(
                        @Parameter(description = "排序方式", example = "hottest", schema = @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {
                                        "hottest", "most_liked", "most_favorited",
                                        "most_viewed" })) @RequestParam(value = "sortBy", defaultValue = "hottest") String sortBy,

                        @Parameter(description = "时间范围", example = "all", schema = @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {
                                        "day", "week", "month",
                                        "all" })) @RequestParam(value = "period", defaultValue = "all") String period,

                        @Parameter(description = "页码（从 1 开始）", example = "1") @RequestParam(value = "page", defaultValue = "1") int page,

                        @Parameter(description = "每页大小", example = "50") @RequestParam(value = "size", defaultValue = "50") int size) {

                logger.info("收到获取排行榜请求: sortBy={}, period={}, page={}, size={}", sortBy, period, page, size);

                try {
                        if (page < 1) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("页码必须大于 0"));
                        }
                        if (size < 1 || size > 100) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("每页大小必须在 1-100 之间"));
                        }

                        PageResult<ArtworkDTO> result = artworkService.getRankingArtworks(sortBy, period, page, size);
                        return ResponseEntity.ok(Result.success(result));

                } catch (Exception e) {
                        logger.error("获取排行榜失败: error={}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Result.error("查询失败: " + e.getMessage()));
                }
        }

        /**
         * 获取用户收藏的作品列表
         * 
         * @param userId 用户 ID（从请求头获取）
         * @param page   页码（从 1 开始）
         * @param size   每页大小
         * @return 收藏的作品列表
         */
        @Operation(summary = "获取用户收藏的作品列表", description = "查询当前用户收藏的所有作品，按收藏时间倒序排列。\n\n" +
                        "**功能说明：**\n" +
                        "- 只能查询当前登录用户自己的收藏列表\n" +
                        "- 按收藏时间倒序排列（最近收藏的在前面）\n" +
                        "- 支持分页查询\n\n" +
                        "**示例：**\n" +
                        "- GET /api/artworks/favorites?page=1&size=20")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "查询成功"),
                        @ApiResponse(responseCode = "400", description = "参数错误"),
                        @ApiResponse(responseCode = "401", description = "未认证"),
                        @ApiResponse(responseCode = "500", description = "服务器错误")
        })
        @GetMapping("/favorites")
        public ResponseEntity<Result<PageResult<ArtworkDTO>>> getUserFavorites(
                        @Parameter(description = "当前用户 ID", required = true, example = "1") @RequestHeader(value = "X-User-Id") Long userId,

                        @Parameter(description = "页码（从 1 开始）", required = true, example = "1") @RequestParam(value = "page", defaultValue = "1") int page,

                        @Parameter(description = "每页大小", required = true, example = "20") @RequestParam(value = "size", defaultValue = "20") int size) {

                logger.info("收到获取用户收藏列表请求: userId={}, page={}, size={}", userId, page, size);

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

                        PageResult<ArtworkDTO> result = artworkService.getUserFavoriteArtworks(userId, page, size);
                        return ResponseEntity.ok(Result.success(result));

                } catch (Exception e) {
                        logger.error("获取用户收藏列表失败: userId={}, error={}", userId, e.getMessage(), e);
                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Result.error("查询失败: " + e.getMessage()));
                }
        }

        /**
         * 获取画师的作品数量（内部服务调用）
         *
         * @param artistId 画师 ID
         * @return 作品数量
         */
        @Operation(summary = "获取画师作品数量", description = "查询指定画师的已发布作品数量。\n\n" +
                        "**功能说明：**\n" +
                        "- 仅统计已发布状态的作品\n" +
                        "- 用于内部服务调用\n\n" +
                        "**示例：**\n" +
                        "- GET /api/artworks/count?artistId=1")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "查询成功"),
                        @ApiResponse(responseCode = "400", description = "参数错误"),
                        @ApiResponse(responseCode = "500", description = "服务器错误")
        })
        @GetMapping("/count")
        public ResponseEntity<Result<Long>> getArtworkCount(
                        @Parameter(description = "画师 ID", required = true, example = "1") @RequestParam(value = "artistId", required = true) Long artistId) {

                logger.info("收到获取作品数量请求: artistId={}", artistId);

                try {
                        // 参数验证
                        if (artistId == null || artistId <= 0) {
                                logger.warn("参数错误: artistId={}", artistId);
                                return ResponseEntity
                                                .status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("画师 ID 不能为空或小于等于 0"));
                        }

                        long count = artworkService.getArtworkCountByArtist(artistId);
                        logger.info("获取作品数量成功: artistId={}, count={}", artistId, count);
                        return ResponseEntity.ok(Result.success(count));
                } catch (Exception e) {
                        logger.error("获取作品数量失败: artistId={}, error={}", artistId, e.getMessage(), e);
                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Result.error("查询失败: " + e.getMessage()));
                }
        }

        /**
         * 更新作品信息
         *
         * @param artworkId 作品 ID
         * @param artistId  当前用户 ID（画师）
         * @param request   更新请求
         * @return 更新后的作品信息
         */
        @Operation(summary = "编辑作品", description = "编辑已发布的作品信息，包括标题、描述和标签。\n\n" +
                        "**权限要求：**\n" +
                        "- 必须是作品的创建者\n\n" +
                        "**可编辑字段：**\n" +
                        "- title: 作品标题\n" +
                        "- description: 作品描述\n" +
                        "- tags: 手动标签列表（会替换原有手动标签，AI标签保持不变）")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "更新成功"),
                        @ApiResponse(responseCode = "400", description = "参数错误"),
                        @ApiResponse(responseCode = "403", description = "无权编辑此作品"),
                        @ApiResponse(responseCode = "404", description = "作品不存在"),
                        @ApiResponse(responseCode = "500", description = "服务器错误")
        })
        @PutMapping("/{artworkId:\\d+}")
        public ResponseEntity<Result<ArtworkDTO>> updateArtwork(
                        @Parameter(description = "作品 ID", required = true, example = "1") @PathVariable("artworkId") Long artworkId,
                        @Parameter(description = "当前用户 ID（画师）", required = true, example = "1") @RequestHeader(value = "X-User-Id") Long artistId,
                        @Valid @RequestBody UpdateArtworkRequest request) {

                logger.info("收到更新作品请求: artworkId={}, artistId={}", artworkId, artistId);

                try {
                        ArtworkDTO artwork = artworkService.updateArtwork(artworkId, artistId, request);
                        logger.info("作品更新成功: artworkId={}", artworkId);
                        return ResponseEntity.ok(Result.success(artwork));

                } catch (Exception e) {
                        logger.error("更新作品失败: artworkId={}, error={}", artworkId, e.getMessage(), e);

                        if (e.getMessage().contains("不存在")) {
                                return ResponseEntity
                                                .status(HttpStatus.NOT_FOUND)
                                                .body(Result.error(e.getMessage()));
                        } else if (e.getMessage().contains("无权")) {
                                return ResponseEntity
                                                .status(HttpStatus.FORBIDDEN)
                                                .body(Result.error(e.getMessage()));
                        } else {
                                return ResponseEntity
                                                .status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("更新失败: " + e.getMessage()));
                        }
                }
        }

        /**
         * 删除作品
         *
         * @param artworkId 作品 ID
         * @param artistId  当前用户 ID（画师）
         * @return 删除结果
         */
        @Operation(summary = "删除作品", description = "删除指定的作品。只有作品的创建者才能删除。\n\n" +
                        "**权限要求：**\n" +
                        "- 必须是作品的创建者\n" +
                        "- 需要画师身份认证\n\n" +
                        "**注意事项：**\n" +
                        "- 删除操作不可恢复\n" +
                        "- 会同时删除作品的所有相关数据（标签、评论、点赞、收藏等）")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "删除成功"),
                        @ApiResponse(responseCode = "403", description = "无权删除此作品"),
                        @ApiResponse(responseCode = "404", description = "作品不存在"),
                        @ApiResponse(responseCode = "500", description = "服务器错误")
        })
        @DeleteMapping("/{artworkId:\\d+}")
        public ResponseEntity<Result<Void>> deleteArtwork(
                        @Parameter(description = "作品 ID", required = true, example = "1") @PathVariable("artworkId") Long artworkId,

                        @Parameter(description = "当前用户 ID（画师）", required = true, example = "1") @RequestHeader(value = "X-User-Id") Long artistId) {

                logger.info("收到删除作品请求: artworkId={}, artistId={}", artworkId, artistId);

                try {
                        artworkService.deleteArtwork(artworkId, artistId);
                        logger.info("作品删除成功: artworkId={}", artworkId);
                        return ResponseEntity.ok(Result.success(null));

                } catch (Exception e) {
                        logger.error("删除作品失败: artworkId={}, error={}", artworkId, e.getMessage(), e);

                        if (e.getMessage().contains("不存在")) {
                                return ResponseEntity
                                                .status(HttpStatus.NOT_FOUND)
                                                .body(Result.error(e.getMessage()));
                        } else if (e.getMessage().contains("无权")) {
                                return ResponseEntity
                                                .status(HttpStatus.FORBIDDEN)
                                                .body(Result.error(e.getMessage()));
                        } else {
                                return ResponseEntity
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(Result.error("删除失败: " + e.getMessage()));
                        }
                }
        }

        /**
         * 获取用户草稿列表
         */
        @Operation(summary = "获取草稿列表", description = "获取当前用户的所有草稿作品")
        @GetMapping("/drafts")
        public ResponseEntity<Result<PageResult<ArtworkDTO>>> getDrafts(
                        @RequestHeader("X-User-Id") Long artistId,
                        @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "size", defaultValue = "20") int size) {
                try {
                        PageResult<ArtworkDTO> result = artworkService.getDrafts(artistId, page, size);
                        return ResponseEntity.ok(Result.success(result));
                } catch (Exception e) {
                        logger.error("获取草稿列表失败: error={}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Result.error("查询失败: " + e.getMessage()));
                }
        }

        /**
         * 发布草稿
         */
        @Operation(summary = "发布草稿", description = "将草稿状态的作品发布为正式作品")
        @PutMapping("/{artworkId:\\d+}/publish")
        public ResponseEntity<Result<ArtworkDTO>> publishDraft(
                        @PathVariable("artworkId") Long artworkId,
                        @RequestHeader("X-User-Id") Long artistId) {
                try {
                        ArtworkDTO artwork = artworkService.publishDraft(artworkId, artistId);
                        return ResponseEntity.ok(Result.success(artwork));
                } catch (Exception e) {
                        logger.error("发布草稿失败: artworkId={}, error={}", artworkId, e.getMessage(), e);
                        if (e.getMessage().contains("不存在")) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body(Result.error(e.getMessage()));
                        }
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Result.error(e.getMessage()));
                }
        }

        /**
         * 搜索自动补全建议
         */
        @Operation(summary = "搜索建议", description = "根据关键词返回匹配的标签和作品标题建议")
        @GetMapping("/suggestions")
        public ResponseEntity<Result<java.util.List<ArtworkService.SearchSuggestionDTO>>> getSearchSuggestions(
                        @RequestParam("keyword") String keyword,
                        @RequestParam(value = "limit", defaultValue = "8") int limit) {
                try {
                        var suggestions = artworkService.getSearchSuggestions(keyword, limit);
                        return ResponseEntity.ok(Result.success(suggestions));
                } catch (Exception e) {
                        logger.error("搜索建议失败: keyword={}, error={}", keyword, e.getMessage());
                        return ResponseEntity.ok(Result.success(java.util.Collections.emptyList()));
                }
        }

}
