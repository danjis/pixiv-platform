package com.pixiv.commission.feign;

import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务 Feign 客户端降级处理工厂
 * 
 * 当文件服务不可用或调用失败时，提供降级处理逻辑
 * 
 * @author Pixiv Platform Team
 */
@Slf4j
@Component
public class FileServiceClientFallbackFactory implements FallbackFactory<FileServiceClient> {

    @Override
    public FileServiceClient create(Throwable cause) {
        return new FileServiceClient() {
            @Override
            public Result<UploadResponse> uploadImage(MultipartFile file) {
                log.error("调用文件服务失败，文件名: {}, 错误信息: {}", 
                    file != null ? file.getOriginalFilename() : "null", 
                    cause.getMessage(), 
                    cause);
                
                return Result.error(503, "文件服务暂时不可用，请稍后重试");
            }
        };
    }
}
