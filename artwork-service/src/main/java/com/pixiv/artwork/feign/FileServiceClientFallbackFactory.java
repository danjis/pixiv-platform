package com.pixiv.artwork.feign;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务 Feign 客户端降级处理工厂
 * 
 * 当文件服务不可用或调用失败时，提供降级处理逻辑
 * 
 * 降级场景：
 * 1. 服务不可用（连接超时、服务宕机）
 * 2. 调用超时（文件上传时间过长）
 * 3. 触发 Sentinel 熔断规则（慢调用比例、异常比例、异常数）
 * 4. 触发 Sentinel 限流规则（QPS 超过阈值）
 * 
 * @author Pixiv Platform Team
 */
@Component
public class FileServiceClientFallbackFactory implements FallbackFactory<FileServiceClient> {

    private static final Logger log = LoggerFactory.getLogger(FileServiceClientFallbackFactory.class);

    @Override
    public FileServiceClient create(Throwable cause) {
        // 判断是否为 Sentinel 熔断或限流
        boolean isSentinelBlock = cause instanceof BlockException;
        String errorType = isSentinelBlock ? "Sentinel 熔断/限流" : "服务调用异常";

        return new FileServiceClient() {
            @Override
            public Result<UploadResponse> uploadImage(MultipartFile file) {
                String fileName = file != null ? file.getOriginalFilename() : "null";

                log.error("[降级处理] 调用文件服务失败 - 上传图片, 文件名: {}, 错误类型: {}, 错误信息: {}",
                        fileName,
                        errorType,
                        cause.getMessage());

                if (isSentinelBlock) {
                    log.warn("[熔断降级] 文件服务触发 Sentinel 保护机制，文件名: {}", fileName);
                    return Result.error(503, "文件服务繁忙，请稍后重试");
                }

                return Result.error(503, "文件服务暂时不可用，请稍后重试");
            }

            @Override
            public Result<Integer> deleteFiles(List<String> fileUrls) {
                log.error("[降级处理] 调用文件服务失败 - 批量删除文件, 文件数: {}, 错误类型: {}, 错误信息: {}",
                        fileUrls != null ? fileUrls.size() : 0,
                        errorType,
                        cause.getMessage());
                return Result.error(503, "文件服务暂时不可用，OSS 文件删除失败");
            }
        };
    }
}
