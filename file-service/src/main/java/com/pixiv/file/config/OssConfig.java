package com.pixiv.file.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置类
 * 
 * 创建 OSS 客户端 Bean，用于文件上传和管理
 * 
 * @author Pixiv Platform Team
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OssConfig {

    private final OssProperties ossProperties;

    /**
     * 创建 OSS 客户端 Bean
     * 
     * @return OSS 客户端实例
     */
    @Bean
    public OSS ossClient() {
        log.info("初始化阿里云 OSS 客户端");
        log.info("OSS Endpoint: {}", ossProperties.getEndpoint());
        log.info("OSS Bucket: {}", ossProperties.getBucketName());
        
        // 创建 OSSClient 实例
        OSS ossClient = new OSSClientBuilder().build(
            ossProperties.getEndpoint(),
            ossProperties.getAccessKeyId(),
            ossProperties.getAccessKeySecret()
        );
        
        log.info("阿里云 OSS 客户端初始化成功");
        return ossClient;
    }
}
