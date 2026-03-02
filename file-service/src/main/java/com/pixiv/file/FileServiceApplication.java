package com.pixiv.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 文件服务启动类
 * 
 * 功能：
 * - 文件上传（支持 JPG、PNG、GIF 格式）
 * - 图片处理（生成缩略图）
 * - 阿里云 OSS 存储
 * 
 * 注意：
 * - 本服务无独立数据库，直接操作阿里云 OSS
 * - 文件大小限制：单个文件最大 10MB
 * - 排除了数据库和 JPA 的自动配置
 * 
 * @author Pixiv Platform Team
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@EnableDiscoveryClient  // 启用 Nacos 服务注册与发现
public class FileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("文件服务启动成功！");
        System.out.println("端口: 8085");
        System.out.println("Swagger 文档: http://localhost:8085/swagger-ui.html");
        System.out.println("========================================");
    }
}
