package com.chenjiabao.open.chenille.config;

import com.chenjiabao.open.chenille.aspect.ChenilleCacheAspect;
import com.chenjiabao.open.chenille.cache.ChenilleCaffeineCacheRemovalListener;
import com.chenjiabao.open.chenille.cache.ChenilleRedisCacheMessageListener;
import com.chenjiabao.open.chenille.cache.ChenilleTwoLevelCacheManager;
import com.chenjiabao.open.chenille.core.ChenilleCacheUtils;
import com.chenjiabao.open.chenille.core.ChenilleHardwareUtils;
import com.chenjiabao.open.chenille.core.ChenilleJsonUtils;
import com.chenjiabao.open.chenille.core.ChenilleStringUtils;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.model.property.ChenilleCacheCaffeine;
import com.chenjiabao.open.chenille.model.property.ChenilleCacheRedis;
import com.chenjiabao.open.chenille.model.property.ChenilleProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@ConditionalOnWebApplication
@EnableConfigurationProperties(ChenilleProperties.class)
@ConditionalOnProperty(prefix = "chenille.config.cache", name = "enabled", havingValue = "true")
@AutoConfigureAfter(ChenilleAutoConfigWeb.class)
public class ChenilleAutoConfigCache {

    @AutoConfiguration(before = ChenilleAutoConfigCache.class)
    @ConditionalOnClass(name = {
            "com.github.benmanes.caffeine.cache.Caffeine",
            "org.springframework.cache.caffeine.CaffeineCacheManager",
            "com.github.benmanes.caffeine.cache.RemovalListener"
    })
    @ConditionalOnProperty(prefix = "chenille.config.cache.caffeine", name = "enabled", havingValue = "true")
    static class CaffeineCacheConfig {

        @Bean
        @ConditionalOnMissingBean
        public CaffeineCacheManager caffeineCacheManager(ChenilleProperties chenilleProperties,
                                                         @Autowired(required = false) @Qualifier("chenilleIoExecutor") ThreadPoolTaskExecutor ioExecutor,
                                                         @Autowired(required = false) ChenilleHardwareUtils chenilleHardwareUtils) {
            if (chenilleHardwareUtils == null) {
                chenilleHardwareUtils = new ChenilleHardwareUtils();
            }

            ChenilleCacheCaffeine caffeine = chenilleProperties.getCache().getCaffeine();
            CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(caffeine.getName());

            // 估算最大缓存条目数（默认 10% 的可用内存，假设平均对象大小 ~1KB，最多 100w 条）
            int maximumSize = caffeine.getMaximumSize() == null
                    ? (int) Math.min(((double) chenilleHardwareUtils.maxMemory() / 1024) * 0.1, 1_000_000)
                    : caffeine.getMaximumSize();

            // 初始容量：默认 10% 的 maximumSize，至少 64，不超过 maximumSize
            int initialCapacity = caffeine.getInitialCapacity() == null
                    ? Math.max(64, (int) (maximumSize * 0.1))
                    : caffeine.getInitialCapacity();

            initialCapacity = Math.min(initialCapacity, maximumSize);

            caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                    // 设置初始缓存大小
                    .initialCapacity(initialCapacity)
                    // 设置最大缓存
                    .maximumSize(maximumSize)
                    .executor(ioExecutor == null ? ForkJoinPool.commonPool() : ioExecutor)
                    .removalListener(new ChenilleCaffeineCacheRemovalListener())
                    .expireAfterAccess(Duration.ofMillis(caffeine.getExpire()))
                    .recordStats()
            );
            return caffeineCacheManager;
        }

    }

    @AutoConfiguration(before = ChenilleAutoConfigCache.class)
    @ConditionalOnClass(name = {
            "org.springframework.data.redis.serializer.RedisSerializer",
            "org.springframework.data.redis.serializer.StringRedisSerializer",
            "org.springframework.data.redis.serializer.RedisSerializationContext",
            "org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer",
            "org.springframework.data.redis.listener.Topic",
            "org.springframework.data.redis.core.RedisTemplate",
            "org.springframework.data.redis.connection.RedisConnectionFactory",
            "org.springframework.data.redis.listener.RedisMessageListenerContainer",
            "org.springframework.data.redis.listener.PatternTopic"
    })
    @EnableConfigurationProperties(ChenilleProperties.class)
    @ConditionalOnProperty(prefix = "chenille.config.cache.redis", name = "enabled", havingValue = "true")
    static class RedisCacheConfig {

        @Bean
        @ConditionalOnMissingBean
        public ReactiveRedisTemplate<String,Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory){
            // Key 序列化器
            RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder =
                    RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

            // Value 序列化器
            RedisSerializationContext<String, Object> context = builder
                    .key(new StringRedisSerializer())
                    .value(new GenericJackson2JsonRedisSerializer())
                    .hashKey(new StringRedisSerializer())
                    .hashValue(new GenericJackson2JsonRedisSerializer())
                    .build();

            return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, context);
        }

        @Bean
        @ConditionalOnMissingBean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
            RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
            redisTemplate.setHashKeySerializer(new StringRedisSerializer());
            redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
            return redisTemplate;
        }

        @Bean
        @ConditionalOnMissingBean
        public RedisCacheManager redisCacheManager(ChenilleProperties chenilleProperties,
                                                   @Autowired(required = false) RedisConnectionFactory redisConnectionFactory) {
            if (redisConnectionFactory == null) {
                throw ChenilleChannelException.builder()
                        .logMessage("注入 RedisCacheManager Bean 失败 -> 启用 chenille.config.cache.redis 时，需要同时启用 spring.redis")
                        .build()
                        .logError();
            }
            ChenilleCacheRedis redis = chenilleProperties.getCache().getRedis();

            RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .serializeKeysWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                    .entryTtl(Duration.ofMillis(redis.getExpire()));

            // 未来可增加多个缓存
            Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
            cacheConfigs.put(redis.getName(), defaultConfig);

            return RedisCacheManager.builder(redisConnectionFactory)
                    .cacheDefaults(defaultConfig)
                    .withInitialCacheConfigurations(cacheConfigs)
                    .transactionAware() // 开启事务支持
                    .build();
        }

        @Bean
        public RedisMessageListenerContainer redisMessageListenerContainer(ChenilleProperties chenilleProperties,
                                                                           @Autowired(required = false) RedisConnectionFactory connectionFactory,
                                                                           @Autowired(required = false) ChenilleTwoLevelCacheManager twoLevelCacheManager,
                                                                           @Autowired(required = false) ChenilleJsonUtils chenilleJsonUtils) {

            if(chenilleJsonUtils == null) {
                chenilleJsonUtils = new ChenilleJsonUtils();
            }

            RedisMessageListenerContainer container = new RedisMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);

            // 订阅主题
            container.addMessageListener(
                    new ChenilleRedisCacheMessageListener(
                            chenilleProperties.getCache(),
                            twoLevelCacheManager,
                            chenilleJsonUtils),
                    new PatternTopic(chenilleProperties.getCache().getTopic())
            );

            return container;
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleTwoLevelCacheManager twoLevelCacheManager(
            ChenilleProperties chenilleProperties,
            @Autowired(required = false) CaffeineCacheManager caffeineCacheManager,
            @Autowired(required = false) RedisCacheManager redisCacheManager,
            @Autowired(required = false) RedisTemplate<String, Object> redisTemplate,
            @Autowired(required = false) ChenilleJsonUtils jsonUtils,
            @Autowired(required = false) @Qualifier("chenilleIoExecutor") ThreadPoolTaskExecutor ioExecutor) {

        if(jsonUtils == null) {
            jsonUtils = new ChenilleJsonUtils();
        }

        return new ChenilleTwoLevelCacheManager(caffeineCacheManager,
                redisCacheManager,
                redisTemplate,
                chenilleProperties.getCache(),
                ioExecutor == null ? ForkJoinPool.commonPool() : ioExecutor,
                jsonUtils);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleCacheUtils chenilleCacheUtils(ChenilleProperties chenilleProperties,
                                         ChenilleTwoLevelCacheManager cacheManager,
                                         @Autowired(required = false) ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                         @Autowired(required = false) ChenilleJsonUtils jsonUtils) {
        if (jsonUtils == null) {
            throw ChenilleChannelException.builder()
                    .logMessage("注入 ChenilleCacheUtils Bean 失败 -> 启用 chenille.config.cache 时，需要同时启用 chenille.config.jackson.json")
                    .build()
                    .logError();
        }
        return new ChenilleCacheUtils(chenilleProperties.getCache(),
                cacheManager,
                reactiveRedisTemplate,
                jsonUtils);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleCacheAspect chenilleCacheAspect(ChenilleProperties chenilleProperties,
                                           @Autowired(required = false) ChenilleCacheUtils cacheUtils,
                                           ChenilleStringUtils stringUtils) {
        return new ChenilleCacheAspect(chenilleProperties.getCache(), cacheUtils, stringUtils);
    }

}
