package com.pixiv.commission.feign;

import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UploadResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务 Feign 客户端
 * 
 * 用于约稿服务调用文件服务的 API，主要用于：
 * - 约稿消息附件上传
 * - 约稿作品图片上传
 * 
 * @author Pixiv Platform Team
 */
@FeignClient(
    name = "file-service",           // 服务名称（在 Nacos 中注册的名称）
    path = "/api/files",              // 基础路径
    fallbackFactory = FileServiceClientFallbackFactory.class  // 降级处理工厂
)
public interface FileServiceClient {

    /**
     * 上传图片
     * 
     * @param file 图片文件
     * @return 上传响应（包含原图和缩略图 URL）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<UploadResponse> uploadImage(@RequestPart("file") MultipartFile file);
}
