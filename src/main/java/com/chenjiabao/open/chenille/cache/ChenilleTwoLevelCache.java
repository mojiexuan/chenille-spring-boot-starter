package com.chenjiabao.open.chenille.cache;

import com.chenjiabao.open.chenille.core.ChenilleJsonUtils;
import com.chenjiabao.open.chenille.model.property.ChenilleCache;
import io.jsonwebtoken.lang.Assert;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * 二级缓存
 * <p>
 * 支持一级缓存（如 Caffeine）+ 二级缓存（如 Redis）
 *
 * @param primaryCache   一级缓存（一般用本地缓存，如 Caffeine）
 * @param secondaryCache 二级缓存（一般用分布式缓存，如 Redis）
 * @param asyncExecutor  异步执行器，用于异步写二级缓存 用于异步写入二级缓存
 * @param redisTemplate  Redis 发布订阅用
 */
@Slf4j
public record ChenilleTwoLevelCache(Cache primaryCache,
                                    Cache secondaryCache,
                                    RedisTemplate<String, Object> redisTemplate,
                                    ChenilleJsonUtils  jsonUtils,
                                    ChenilleCache chenilleCache,
                                    Executor asyncExecutor) implements Cache {

    // null 占位符，避免缓存穿透
    private static final Object NULL_PLACEHOLDER = new Object();

    @Override
    @NonNull
    public String getName() {
        return chenilleCache.getName();
    }

    @Override
    public Object getNativeCache() {
        if (chenilleCache.getCaffeine().isEnabled()) {
            return primaryCache.getNativeCache();
        }
        if (chenilleCache.getRedis().isEnabled()) {
            return secondaryCache.getNativeCache();
        }
        return null;
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        Assert.notNull(key, "key不可为空");
        ValueWrapper value;
        if (chenilleCache.getCaffeine().isEnabled() && primaryCache != null) {
            value = primaryCache.get(key);
            if (value != null) {
                return value;
            }
        }
        if (chenilleCache.getRedis().isEnabled() && secondaryCache != null) {
            value = secondaryCache.get(key);
            if (value != null) {
                // 回填
                ValueWrapper finalValue = value;
                asyncExecutor.execute(() -> {
                    if (chenilleCache.getCaffeine().isEnabled() && primaryCache != null) {
                        primaryCache.put(key, finalValue.get());
                    }
                });
                return value;
            }
        }
        return null;
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        // 一级缓存
        T value;
        if (chenilleCache.getCaffeine().isEnabled() && primaryCache != null) {
            value = primaryCache.get(key, type);
            if (value != null) {
                return value;
            }
        }
        if (chenilleCache.getRedis().isEnabled() && secondaryCache != null) {
            value = secondaryCache.get(key, type);
            if (value != null) {
                // 回填
                T finalValue = value;
                asyncExecutor.execute(() -> {
                    if (chenilleCache.getCaffeine().isEnabled() && primaryCache != null) {
                        primaryCache.put(key, finalValue);
                    }
                });
                return value;
            }
        }
        return null;
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        // 缓存尝试（无锁）
        T value = tryGetFromCache(key);
        if (value != null) {
            return unwrap(value);
        }
        if ((chenilleCache.getCaffeine().isEnabled() && primaryCache != null) ||
                (chenilleCache.getRedis().isEnabled() && secondaryCache != null)) {
            synchronized (ChenilleLockRegistry.getLock(key)) {
                // 第二次缓存尝试（可能有其他线程已经加载）
                value = tryGetFromCache(key);
                if (value != null) {
                    return unwrap(value);
                }

                // 执行加载
                try {
                    value = valueLoader.call();
                    Object toCache = (value == null ? NULL_PLACEHOLDER : value);
                    asyncExecutor.execute(() -> {
                        put(key, toCache);
                    });
                    return value;
                } catch (Exception e) {
                    throw new ValueRetrievalException(key, valueLoader, e);
                }
            }
        }
        return null;
    }

    /**
     * 尝试从缓存中获取值
     */
    private <T> T tryGetFromCache(Object key) {
        try {
            // 一级缓存
            ValueWrapper wrapper = null;
            if (chenilleCache.getCaffeine().isEnabled() && primaryCache != null) {
                wrapper = primaryCache.get(key);
            }
            if (wrapper == null && chenilleCache.getRedis().isEnabled() && secondaryCache != null) {
                // 一级缓存未命中，查二级缓存
                wrapper = secondaryCache.get(key);
                if (wrapper != null) {
                    ValueWrapper finalWrapper = wrapper;
                    asyncExecutor.execute(() -> {
                        if (chenilleCache.getCaffeine().isEnabled() && primaryCache != null) {
                            primaryCache.put(key, finalWrapper.get());
                        }
                    });
                }
            }

            if (wrapper != null) {
                @SuppressWarnings("unchecked")
                T value = (T) wrapper.get();
                return value;
            }
        } catch (Exception e) {
            log.error("从缓存中获取值失败 -> {}", e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T unwrap(Object value) {
        return value == NULL_PLACEHOLDER ? null : (T) value;
    }

    /**
     * 缓存数据（一级+二级）
     */
    @Override
    public void put(@NonNull Object key, Object value) {
        putLocal(key, value);
        putRemote(key, value);
    }

    /**
     * 只缓存一级缓存
     */
    public void putLocal(@NonNull Object key, Object value){
        if (chenilleCache.getCaffeine().isEnabled() && primaryCache != null) {
            Object toCache = (value == null ? NULL_PLACEHOLDER : value);
            primaryCache.put(key, toCache);
        }
    }

    /**
     * 只缓存二级缓存,触发缓存变更通知
     */
    public void putRemote(@NonNull Object key, Object value){
        if (chenilleCache.getRedis().isEnabled() && secondaryCache != null) {
            Object toCache = (value == null ? NULL_PLACEHOLDER : value);
            asyncExecutor.execute(()->{
                secondaryCache.put(key, toCache);
                asyncPublish(key, toCache);
            });
        }
    }

    /**
     * 清理缓存（一级+二级）
     */
    @Override
    public void evict(@NonNull Object key) {
        evictLocal(key);
        evictRemote(key);
    }

    /**
     * 只清理本地一级缓存，不触发二级缓存和广播
     */
    public void evictLocal(Object key) {
        if (chenilleCache.getCaffeine().isEnabled() && primaryCache != null) {
            primaryCache.evict(key);
        }
    }

    /**
     * 只清理远程二级缓存，不触发一级缓存
     */
    public void evictRemote(Object key) {
        if (chenilleCache.getRedis().isEnabled() && secondaryCache != null) {
            asyncExecutor.execute(()->{
                secondaryCache.evict(key);
                asyncPublish(key, null);
            });
        }
    }

    /**
     * 清理一级缓存和二级缓存
     */
    @Override
    public void clear() {
        clearLocal();
        clearRemote();
    }
    /**
     * 清理一级缓存
     */
    public void clearLocal() {
        if (chenilleCache.getCaffeine().isEnabled() && primaryCache != null) {
            primaryCache.clear();
        }
    }

    /**
     * 清理二级缓存
     */
    public void clearRemote() {
        if (chenilleCache.getRedis().isEnabled() && secondaryCache != null) {
            asyncExecutor.execute(secondaryCache::clear);
        }
    }

    /**
     * 缓存变更时通知其他节点清理本地缓存
     * 异步通过发布订阅主题消息，其他节点监听到之后进行相关本地缓存操作，防止本地缓存脏数据
     */
    void asyncPublish(Object key, Object value){
        asyncExecutor.execute(() -> {
            ChenilleCacheMessage message = new ChenilleCacheMessage();
            message.setCacheName(chenilleCache.getName());
            message.setKey(key);
            message.setValue(value);
            redisTemplate.convertAndSend(chenilleCache.getTopic(), message);
        });
    }
}
