package com.chenjiabao.open.chenille.config;

import com.chenjiabao.open.chenille.aspect.ChenilleCacheAspect;
import com.chenjiabao.open.chenille.aspect.ChenilleResponseAspect;
import com.chenjiabao.open.chenille.cache.ChenilleCaffeineCacheRemovalListener;
import com.chenjiabao.open.chenille.cache.ChenilleRedisCacheMessageListener;
import com.chenjiabao.open.chenille.cache.ChenilleTwoLevelCacheManager;
import com.chenjiabao.open.chenille.common.ChenilleWeChatCommon;
import com.chenjiabao.open.chenille.controller.ChenilleDocController;
import com.chenjiabao.open.chenille.controller.ChenilleAssetsController;
import com.chenjiabao.open.chenille.core.*;
import com.chenjiabao.open.chenille.docs.scanner.ChenilleApiScanner;
import com.chenjiabao.open.chenille.enums.ChenilleInternalEnum;
import com.chenjiabao.open.chenille.filter.ChenilleAuthFilter;
import com.chenjiabao.open.chenille.filter.ChenilleCorsFilter;
import com.chenjiabao.open.chenille.filter.ChenilleExchangeContextFilter;
import com.chenjiabao.open.chenille.handler.ChenilleVersionedRMHM;
import com.chenjiabao.open.chenille.model.property.*;
import com.chenjiabao.open.chenille.resolver.ChenilleRequestAttrParamArgumentResolver;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

/**
 * @author ChenJiaBao
 */
@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableConfigurationProperties(ChenilleProperties.class)
public class ChenilleAutoConfig implements WebFluxConfigurer {

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(requestAttributeParamArgumentResolver());
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleXmlUtils chenilleXmlUtils() {
        return new ChenilleXmlUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleJsonUtils chenilleJsonUtils() {
        return new ChenilleJsonUtils();
    }

    @Bean
    @Primary
    public RequestMappingHandlerMapping chenilleRequestMappingHandlerMapping(
            ChenilleProperties chenilleProperties) {
        ChenilleVersionedRMHM versionedRMHM = new ChenilleVersionedRMHM(chenilleProperties.getApi());
        versionedRMHM.setOrder(0);
        return versionedRMHM;
    }

    /**
     * CPU 密集型线程池
     */
    @Bean(name = "chenilleCpuExecutor")
    @ConditionalOnProperty(prefix = "chenille.config.executor.cpu", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor cpuExecutor(ChenilleProperties chenilleProperties,
                                              ChenilleHardwareUtils chenilleHardwareUtils) {
        ChenilleExecutorCPU cpu = chenilleProperties.getExecutor().getCpu();
        return chenilleHardwareUtils.buildExecutor(ChenilleInternalEnum.ExecutorType.CPU,
                cpu.getName(),
                cpu.getCorePoolSize(),
                cpu.getMaxPoolSize(),
                cpu.getQueueCapacity(),
                cpu.getRejectedPolicy());
    }

    /**
     * IO 密集型线程池
     */
    @Bean(name = "chenilleIoExecutor")
    @ConditionalOnProperty(prefix = "chenille.config.executor.io", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor ioExecutor(ChenilleProperties chenilleProperties,
                                             ChenilleHardwareUtils chenilleHardwareUtils) {
        ChenilleExecutorIO io = chenilleProperties.getExecutor().getIo();
        return chenilleHardwareUtils.buildExecutor(ChenilleInternalEnum.ExecutorType.IO,
                io.getName(),
                io.getCorePoolSize(),
                io.getMaxPoolSize(),
                io.getQueueCapacity(),
                io.getRejectedPolicy());
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleExchangeContextFilter chenilleExchangeContextFilter() {
        return new ChenilleExchangeContextFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleResponseAspect chenilleResponseAspect() {
        return new ChenilleResponseAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleHardwareUtils chenilleHardwareUtils() {
        return new ChenilleHardwareUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenillePriceUtils priceUtils() {
        return new ChenillePriceUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.cache.redis", name = "enabled", havingValue = "true")
    public RedisMessageListenerContainer redisMessageListenerContainer(ChenilleProperties chenilleProperties,
                                                                       RedisConnectionFactory connectionFactory,
                                                                       ChenilleTwoLevelCacheManager twoLevelCacheManager,
                                                                       ChenilleJsonUtils chenilleJsonUtils) {
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

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.cache.caffeine", name = "enabled", havingValue = "true")
    public CaffeineCacheManager caffeineCacheManager(ChenilleProperties chenilleProperties,
                                                     @Autowired(required = false) @Qualifier("chenilleIoExecutor") ThreadPoolTaskExecutor ioExecutor,
                                                     ChenilleHardwareUtils chenilleHardwareUtils) {
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

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.cache.redis", name = "enabled", havingValue = "true")
    public RedisCacheManager redisCacheManager(ChenilleProperties chenilleProperties,
                                               RedisConnectionFactory redisConnectionFactory) {
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
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.cache", name = "enabled", havingValue = "true")
    public ChenilleTwoLevelCacheManager twoLevelCacheManager(
            ChenilleProperties chenilleProperties,
            @Autowired(required = false) CaffeineCacheManager caffeineCacheManager,
            @Autowired(required = false) RedisCacheManager redisCacheManager,
            @Autowired(required = false) RedisTemplate<String, Object> redisTemplate,
            ChenilleJsonUtils jsonUtils,
            @Autowired(required = false) @Qualifier("chenilleIoExecutor") ThreadPoolTaskExecutor ioExecutor) {
        return new ChenilleTwoLevelCacheManager(caffeineCacheManager,
                redisCacheManager,
                redisTemplate,
                chenilleProperties.getCache(),
                ioExecutor == null ? ForkJoinPool.commonPool() : ioExecutor,
                jsonUtils);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.cache", name = "enabled", havingValue = "true")
    public ChenilleCacheUtils cacheUtils(ChenilleProperties chenilleProperties,
                                         ChenilleTwoLevelCacheManager cacheManager,
                                         @Autowired(required = false) RedisTemplate<String, Object> redisTemplate,
                                         ChenilleJsonUtils jsonUtils) {
        return new ChenilleCacheUtils(chenilleProperties.getCache(),
                cacheManager,
                redisTemplate,
                jsonUtils);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.cache", name = "enabled", havingValue = "true")
    public ChenilleCacheAspect cacheAspect(ChenilleProperties chenilleProperties,
                                           ChenilleCacheUtils cacheUtils,
                                           ChenilleStringUtils stringUtils) {
        return new ChenilleCacheAspect(chenilleProperties.getCache(), cacheUtils, stringUtils);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.cache.redis", name = "enabled", havingValue = "true")
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
    @ConditionalOnProperty(prefix = "chenille.config.wechat", name = "enabled", havingValue = "true")
    public ChenilleWeChatCommon weChatCommon(ChenilleProperties chenilleProperties) {
        return new ChenilleWeChatCommon(chenilleProperties.getWechat());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.auth", name = "enabled", havingValue = "true")
    public ChenilleAuthFilter authFilterRegistration(
            ChenilleProperties chenilleProperties,
            @Autowired(required = false) ChenilleAuth chenilleAuth,
            ChenilleCheckUtils chenilleCheckUtils,
            ChenilleJwtUtils chenilleJwtUtils) {
        return new ChenilleAuthFilter(
                chenilleProperties.getAuth(),
                chenilleAuth,
                chenilleCheckUtils,
                chenilleJwtUtils
        );
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(0)
    public ChenilleCorsFilter corsWebFilter(ChenilleProperties properties) {
        return new ChenilleCorsFilter(properties.getApi());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.docs", name = "enabled", havingValue = "true")
    public ChenilleDocController jiaBaoDocController() {
        return new ChenilleDocController();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.docs", name = "enabled", havingValue = "true")
    public ChenilleApiScanner apiScanner() {
        return new ChenilleApiScanner();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.assets", name = "enabled", havingValue = "true")
    public ChenilleAssetsController chenilleAssetsController(ChenilleProperties properties) {
        return new ChenilleAssetsController(properties.getAssets());
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleRequestAttrParamArgumentResolver requestAttributeParamArgumentResolver() {
        return new ChenilleRequestAttrParamArgumentResolver();
    }

    @Bean
    @ConditionalOnMissingBean // 仅当不存在该类型的bean时，才会创建该bean
    @ConditionalOnProperty(prefix = "chenille.config.hash", name = "enabled", havingValue = "true")
    public ChenilleHashUtils hashUtils(ChenilleProperties properties) {
        ChenilleHashUtils chenilleHashUtils = new ChenilleHashUtils();
        if (properties.getHash().getPepper() != null) {
            chenilleHashUtils.setHashPepper(properties.getHash().getPepper());
        }
        return chenilleHashUtils;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.jwt", name = "enabled", havingValue = "true")
    public ChenilleJwtUtils jwtUtils(ChenilleProperties properties) {
        ChenilleJwtUtils chenilleJwtUtils = new ChenilleJwtUtils();
        if (properties.getJwt().getSecret() != null) {
            chenilleJwtUtils.setJwtSecret(properties.getJwt().getSecret());
        }
        chenilleJwtUtils.setExpires(properties.getJwt().getExpires());
        return chenilleJwtUtils;
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleDelayedTaskExecutor delayedTaskExecutor() {
        return new ChenilleDelayedTaskExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleSnowflakeUtils snowflakeUtils(ChenilleProperties properties) {
        return new ChenilleSnowflakeUtils(properties.getMachine().getId());
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleSensitiveWordUtils sensitiveWordUtils() {
        return ChenilleSensitiveWordUtils.builder();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.mail", name = "enabled", havingValue = "true")
    public ChenilleMailUtils.Builder mailUtilsBuilder(ChenilleCheckUtils chenilleCheckUtils, ChenilleProperties properties) {
        ChenilleMailUtils.Builder builder = new ChenilleMailUtils.Builder(chenilleCheckUtils);
        if (properties.getMail().getHost() != null) {
            builder = builder.setHost(properties.getMail().getHost());
        }
        builder = builder.setPort(properties.getMail().getPort());
        builder = builder.setSsl(properties.getMail().getSsl());
        builder = builder.setAuth(properties.getMail().getAuth());
        if (properties.getMail().getUsername() != null) {
            builder = builder.setUsername(properties.getMail().getUsername());
        }
        if (properties.getMail().getPassword() != null) {
            builder = builder.setPassword(properties.getMail().getPassword());
        }
        builder = builder.setProtocol(properties.getMail().getProtocol());

        return builder;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.mail", name = "enabled", havingValue = "true")
    public ChenilleMailUtils mailUtils(ChenilleMailUtils.Builder builder, ChenilleProperties properties) {
        if (properties.getMail().getUsername() != null) {
            return builder.build().setFrom(properties.getMail().getUsername());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.check", name = "enabled", havingValue = "true")
    public ChenilleCheckUtils checkUtils(ChenilleProperties properties) {
        return new ChenilleCheckUtils(properties.getCheck());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "chenille.config.file", name = "enabled", havingValue = "true")
    public ChenilleFilesUtils filesUtils(ChenilleProperties properties,
                                         ChenilleTimeUtils chenilleTimeUtils,
                                         ChenilleStringUtils chenilleStringUtils) {
        return new ChenilleFilesUtils(properties.getFile(), chenilleTimeUtils, chenilleStringUtils);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleTimeUtils timeUtils(ChenilleProperties properties) {
        return new ChenilleTimeUtils(properties.getTime());
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleStringUtils stringUtils() {
        return new ChenilleStringUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChenilleCollectionUtils collectionUtils() {
        return new ChenilleCollectionUtils();
    }

}
