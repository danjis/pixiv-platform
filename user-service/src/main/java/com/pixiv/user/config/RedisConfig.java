package com.pixiv.user.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

/**
 * Redis 配置类
 * 
 * 配置 RedisTemplate 的序列化方式 + Spring Cache 缓存管理器
 * 
 * @author Pixiv Platform Team
 */
@Configuration
@EnableCaching
public class RedisConfig {

        /**
         * 配置 RedisTemplate
         * 使用 Jackson2JsonRedisSerializer 进行序列化
         */
        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                // 使用 Jackson2JsonRedisSerializer 来序列化和反序列化 redis 的 value 值
                Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
                mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL);
                serializer.setObjectMapper(mapper);

                // 使用 StringRedisSerializer 来序列化和反序列化 redis 的 key 值
                StringRedisSerializer stringSerializer = new StringRedisSerializer();

                // key 采用 String 的序列化方式
                template.setKeySerializer(stringSerializer);
                // hash 的 key 也采用 String 的序列化方式
                template.setHashKeySerializer(stringSerializer);
                // value 序列化方式采用 jackson
                template.setValueSerializer(serializer);
                // hash 的 value 序列化方式采用 jackson
                template.setHashValueSerializer(serializer);

                template.afterPropertiesSet();
                return template;
        }

        /**
         * 配置 RedisCacheManager
         */
        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(15))
                                .prefixCacheNameWith("pixiv:user:")
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(createJsonRedisSerializer()))
                                .disableCachingNullValues();

                Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
                cacheConfigs.put("userProfile", defaultConfig.entryTtl(Duration.ofMinutes(10))); // 用户资料 10 分钟
                cacheConfigs.put("artistProfile", defaultConfig.entryTtl(Duration.ofMinutes(15))); // 画师资料 15 分钟
                cacheConfigs.put("followCount", defaultConfig.entryTtl(Duration.ofMinutes(5))); // 关注数 5 分钟

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigs)
                                .build();
        }

        /**
         * 创建支持 Java 8 日期时间类型的 JSON Redis 序列化器
         */
        private GenericJackson2JsonRedisSerializer createJsonRedisSerializer() {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
                mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL);
                return new GenericJackson2JsonRedisSerializer(mapper);
        }

}
