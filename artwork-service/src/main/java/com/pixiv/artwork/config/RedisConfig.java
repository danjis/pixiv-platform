package com.pixiv.artwork.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis + Caffeine 多级缓存配置
 *
 * L1: Caffeine 本地缓存 — 高频热点数据（排行榜、标签列表、搜索建议），响应极快
 * L2: Redis 分布式缓存 — 作品详情、列表等共享数据，跨实例一致
 */
@Configuration
@EnableCaching
public class RedisConfig {

        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

                ObjectMapper mapper = new ObjectMapper();
                mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
                mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL);
                mapper.registerModule(new JavaTimeModule());
                serializer.setObjectMapper(mapper);

                StringRedisSerializer stringSerializer = new StringRedisSerializer();

                template.setKeySerializer(stringSerializer);
                template.setHashKeySerializer(stringSerializer);
                template.setValueSerializer(serializer);
                template.setHashValueSerializer(serializer);

                template.afterPropertiesSet();
                return template;
        }

        /**
         * L1 Caffeine 本地缓存管理器
         * 适合高频读取、变更不频繁的数据
         */
        @Bean("caffeineCacheManager")
        public CaffeineCacheManager caffeineCacheManager() {
                CaffeineCacheManager manager = new CaffeineCacheManager(
                                "searchSuggestions", "tagList", "artworkCount", "followCount");
                manager.setCaffeine(Caffeine.newBuilder()
                                .initialCapacity(64)
                                .maximumSize(1024)
                                .expireAfterWrite(3, TimeUnit.MINUTES));
                return manager;
        }

        /**
         * L2 Redis 分布式缓存管理器
         */
        @Bean("redisCacheManager")
        public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30))
                                .prefixCacheNameWith("pixiv:artwork:")
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                                .disableCachingNullValues();

                Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
                cacheConfigs.put("artworkList", defaultConfig.entryTtl(Duration.ofMinutes(5)));
                cacheConfigs.put("tagList", defaultConfig.entryTtl(Duration.ofHours(2)));
                cacheConfigs.put("artworkCount", defaultConfig.entryTtl(Duration.ofMinutes(10)));
                cacheConfigs.put("searchSuggestions", defaultConfig.entryTtl(Duration.ofMinutes(10)));
                cacheConfigs.put("artistProfile", defaultConfig.entryTtl(Duration.ofMinutes(15)));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigs)
                                .build();
        }

        /**
         * 组合缓存管理器：Caffeine → Redis 二级查找
         * Spring @Cacheable 会按顺序查找：先查 Caffeine（L1），未命中再查 Redis（L2）
         */
        @Bean
        @Primary
        public CacheManager cacheManager(CaffeineCacheManager caffeineCacheManager,
                        RedisCacheManager redisCacheManager) {
                CompositeCacheManager composite = new CompositeCacheManager();
                composite.setCacheManagers(java.util.List.of(caffeineCacheManager, redisCacheManager));
                composite.setFallbackToNoOpCache(false);
                return composite;
        }

}
