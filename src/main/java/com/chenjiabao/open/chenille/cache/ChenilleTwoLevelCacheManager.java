package com.chenjiabao.open.chenille.cache;

import com.chenjiabao.open.chenille.core.ChenilleJsonUtils;
import com.chenjiabao.open.chenille.model.property.ChenilleCache;
import lombok.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * 二级缓存管理器
 */
public class ChenilleTwoLevelCacheManager implements CacheManager {

    private final CacheManager primaryCacheManager;
    private final CacheManager secondaryCacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<String, Cache> cacheMap = new ConcurrentHashMap<>();
    private final ChenilleCache chenilleCache;
    private final Executor asyncExecutor;
    private final ChenilleJsonUtils jsonUtils;

    public ChenilleTwoLevelCacheManager(CacheManager primaryCacheManager,
                                        CacheManager secondaryCacheManager,
                                        RedisTemplate<String, Object> redisTemplate,
                                        ChenilleCache chenilleCache,
                                        Executor asyncExecutor, ChenilleJsonUtils jsonUtils) {
        this.primaryCacheManager = primaryCacheManager;
        this.secondaryCacheManager = secondaryCacheManager;
        this.redisTemplate = redisTemplate;
        this.chenilleCache = chenilleCache;
        this.asyncExecutor = asyncExecutor;
        this.jsonUtils = jsonUtils;
    }

    @Override
    public Cache getCache(@NonNull String name) {
        return cacheMap.computeIfAbsent(name, cacheName ->
                new ChenilleTwoLevelCache(
                        Objects.requireNonNull(primaryCacheManager.getCache(cacheName)),
                        secondaryCacheManager.getCache(cacheName),
                        redisTemplate,
                        jsonUtils,
                        chenilleCache,
                        asyncExecutor
                ));
    }

    /**
     * 获取缓存
     */
    public ChenilleTwoLevelCache getTwoLevelCache(@NonNull String name) {
        return (ChenilleTwoLevelCache) getCache(name);
    }

    @Override
    @NonNull
    public Collection<String> getCacheNames() {
        return primaryCacheManager.getCacheNames();
    }

}
