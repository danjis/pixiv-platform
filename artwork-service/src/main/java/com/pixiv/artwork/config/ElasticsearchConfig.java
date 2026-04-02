package com.pixiv.artwork.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Elasticsearch 配置类
 * 
 * 分离 JPA 和 Elasticsearch 仓库扫描路径，
 * 避免 Spring Data 自动配置时将 ES Repository 误识别为 JPA Repository
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.pixiv.artwork.repository")
@EnableElasticsearchRepositories(basePackages = "com.pixiv.artwork.search")
public class ElasticsearchConfig {
    // Spring Boot 自动配置 ElasticsearchClient 和 ElasticsearchOperations
    // 连接信息从 Nacos shared-elasticsearch.yml 加载
}
