package com.pixiv.artwork.feign;

import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UploadResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务 Feign 客户端
 * 
 * 用于作品服务调用文件服务的 API，主要用于：
 * - 作品图片上传
 * - 作品图片删除（从 OSS 删除）
 * 
 * @author Pixiv Platform Team
 */
@FeignClient(name = "file-service", path = "/api/files", fallbackFactory = FileServiceClientFallbackFactory.class)
public interface FileServiceClient {

    /**
     * 上传图片
     * 
     * @param file 图片文件
     * @return 上传响应（包含原图和缩略图 URL）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<UploadResponse> uploadImage(@RequestPart("file") MultipartFile file);

    /**
     * 批量删除文件
     * 
     * @param fileUrls 文件 URL 列表
     * @return 成功删除的数量
     */
    @PostMapping("/delete-batch")
    Result<Integer> deleteFiles(@RequestBody List<String> fileUrls);
}
