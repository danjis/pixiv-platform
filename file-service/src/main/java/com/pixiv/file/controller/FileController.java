package com.pixiv.file.controller;

import com.pixiv.common.dto.Result;
import com.pixiv.file.dto.UploadResponse;
import com.pixiv.file.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件管理控制器
 * 
 * 提供文件上传、图片处理等功能
 * 
 * @author Pixiv Platform Team
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传、图片处理等功能")
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * 上传图片
     * 
     * 支持的格式: JPG, PNG, GIF
     * 文件大小限制: 最大 10MB
     * 
     * @param file 上传的图片文件
     * @return 上传响应（包含原图和缩略图 URL）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传图片", description = "上传图片文件，支持 JPG、PNG、GIF 格式，最大 10MB。系统会自动生成缩略图。")
    public Result<UploadResponse> uploadImage(
            @Parameter(description = "图片文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            log.info("收到文件上传请求，文件名: {}", file.getOriginalFilename());

            UploadResponse response = fileStorageService.uploadImage(file);

            log.info("文件上传成功: {}", response.getFileName());
            return Result.success(response);

        } catch (IllegalArgumentException e) {
            log.warn("文件上传失败（参数错误）: {}", e.getMessage());
            return Result.error(400, e.getMessage());

        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像
     *
     * 头像存储到 OSS avatars/ 目录，不生成缩略图
     * 文件大小限制: 最大 5MB
     *
     * @param file 上传的头像文件
     * @return 上传响应（包含头像 URL）
     */
    @PostMapping(value = "/upload/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传头像", description = "上传头像文件，支持 JPG、PNG、GIF 格式，最大 5MB。存储到独立的 avatars 目录。")
    public Result<UploadResponse> uploadAvatar(
            @Parameter(description = "头像文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            log.info("收到头像上传请求，文件名: {}", file.getOriginalFilename());

            UploadResponse response = fileStorageService.uploadAvatar(file);

            log.info("头像上传成功: {}", response.getFileName());
            return Result.success(response);

        } catch (IllegalArgumentException e) {
            log.warn("头像上传失败（参数错误）: {}", e.getMessage());
            return Result.error(400, e.getMessage());

        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error(500, "头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传图片
     *
     * 支持一次上传多张图片，最多 10 张
     *
     * @param files 上传的图片文件列表
     * @return 上传响应列表
     */
    @PostMapping(value = "/upload-batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "批量上传图片", description = "批量上传图片文件，最多 10 张，支持 JPG、PNG、GIF 格式，每张最大 10MB。")
    public Result<List<UploadResponse>> uploadBatch(
            @Parameter(description = "图片文件列表", required = true) @RequestParam("files") List<MultipartFile> files) {
        try {
            if (files == null || files.isEmpty()) {
                return Result.error(400, "请至少上传一张图片");
            }
            if (files.size() > 10) {
                return Result.error(400, "一次最多上传 10 张图片");
            }

            log.info("收到批量文件上传请求，文件数量: {}", files.size());

            List<UploadResponse> responses = new ArrayList<>();
            for (MultipartFile file : files) {
                UploadResponse response = fileStorageService.uploadImage(file);
                responses.add(response);
            }

            log.info("批量文件上传成功，成功数量: {}", responses.size());
            return Result.success(responses);

        } catch (IllegalArgumentException e) {
            log.warn("批量文件上传失败（参数错误）: {}", e.getMessage());
            return Result.error(400, e.getMessage());

        } catch (Exception e) {
            log.error("批量文件上传失败", e);
            return Result.error(500, "批量文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除文件
     *
     * 从阿里云 OSS 上删除指定的文件列表
     *
     * @param fileUrls 要删除的文件 URL 列表
     * @return 成功删除的数量
     */
    @PostMapping("/delete-batch")
    @Operation(summary = "批量删除文件", description = "批量删除阿里云 OSS 上的文件，传入文件完整 URL 列表")
    public Result<Integer> deleteBatch(
            @Parameter(description = "文件 URL 列表", required = true) @RequestBody java.util.List<String> fileUrls) {
        try {
            if (fileUrls == null || fileUrls.isEmpty()) {
                return Result.success(0);
            }
            log.info("收到批量文件删除请求，文件数量: {}", fileUrls.size());
            int deletedCount = fileStorageService.deleteFiles(fileUrls);
            log.info("批量文件删除完成，成功删除: {} 个", deletedCount);
            return Result.success(deletedCount);
        } catch (Exception e) {
            log.error("批量文件删除失败", e);
            return Result.error(500, "文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查文件服务是否正常运行")
    public Result<String> health() {
        return Result.success("文件服务运行正常");
    }
}
