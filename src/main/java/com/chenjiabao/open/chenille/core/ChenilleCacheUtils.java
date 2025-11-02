package com.chenjiabao.open.chenille.core;

import com.chenjiabao.open.chenille.cache.ChenilleTwoLevelCache;
import com.chenjiabao.open.chenille.cache.ChenilleTwoLevelCacheManager;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.model.property.ChenilleCache;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存工具
 */
@Slf4j
public record ChenilleCacheUtils(ChenilleCache chenilleCache,
                                 ChenilleTwoLevelCacheManager cacheManager,
                                 ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                 ChenilleJsonUtils  jsonUtils) {

    /**
     * 放入缓存（默认缓存）
     */
    public Mono<Void> put(@NonNull String key, @NonNull Object value) {
        return put(chenilleCache.getName(), key, value);
    }

    /**
     * 放入缓存
     */
    public Mono<Void> put(@NonNull String cacheName,@NonNull String key, @NonNull Object value) {
        return executeOnCache(cacheName, cache -> cache.put(key, value));
    }

    /**
     * 放入缓存，仅放入一级缓存
     */
    public Mono<Void> putLocal(@NonNull Object key, Object value){
        return putLocal(chenilleCache.getName(), key, value);
    }

    /**
     * 放入缓存，仅放入一级缓存
     */
    public Mono<Void> putLocal(@NonNull String cacheName,@NonNull Object key, Object value){
        return executeOnCache(cacheName, cache -> cache.putLocal(key, value));
    }

    /**
     * 放入缓存，仅放入二级缓存
     */
    public Mono<Void> putRemote(@NonNull Object key, Object value){
        return putRemote(chenilleCache.getName(), key, value);
    }

    /**
     * 放入缓存，仅放入二级缓存
     */
    public Mono<Void> putRemote(@NonNull String cacheName,@NonNull Object key, Object value){
        return executeOnCache(cacheName, cache -> cache.putRemote(key, value));
    }

    /**
     * 获取缓存值（默认缓存）
     */
    public <K, V> Mono<V> get(@NonNull K key, @NonNull Class<V> type) {
        return get(chenilleCache.getName(), key, type);
    }

    /**
     * 获取缓存值
     */
    public <K, V> Mono<V> get(@NonNull String cacheName, @NonNull K key, @NonNull Class<V> type) {
        return executeOnCacheWithResult(cacheName, cache -> Mono.fromCallable(() -> cache.get(key, type))
                .flatMap(v -> v == null ? Mono.empty() : Mono.just(v))
                .onErrorResume(Mono::error));
    }

    /**
     * 获取缓存值（默认缓存）
     */
    public <K, V> Mono<V> get(@NonNull K key, @NonNull Callable<V> valueLoader) {
        return get(chenilleCache.getName(), key, valueLoader);
    }

    /**
     * 获取缓存值（带加载器）
     */
    public <K, V> Mono<V> get(@NonNull String cacheName, @NonNull K key, @NonNull Callable<V> loader) {
        return executeOnCacheWithResult(cacheName, cache ->
                Mono.fromCallable(() -> {
                    Cache.ValueWrapper wrapper = cache.get(key);
                    if (wrapper != null && wrapper.get() != null) {
                        return (V) wrapper.get();
                    }
                    V v = loader.call();
                    cache.put(key, v);
                    return v;
                })
        );
    }

    /**
     * 删除缓存（默认缓存）
     */
    public Mono<Void> evict(@NonNull Object key) {
        return evict(chenilleCache.getName(), key);
    }

    /**
     * 删除缓存
     */
    public Mono<Void> evict(@NonNull String cacheName,@NonNull Object key) {
        return executeOnCache(cacheName, cache -> cache.evict(key));
    }

    /**
     * 删除缓存（本地一级缓存）
     */
    public Mono<Void> evictLocal(@NonNull Object key) {
        return evictLocal(chenilleCache.getName(), key);
    }

    /**
     * 删除缓存（本地一级缓存）
     */
    public Mono<Void> evictLocal(@NonNull String cacheName,@NonNull Object key) {
        return executeOnCache(cacheName, cache -> cache.evictLocal(key));
    }

    /**
     * 删除缓存（二级缓存）
     */
    public Mono<Void> evictRemote(@NonNull Object key) {
        return evictRemote(chenilleCache.getName(), key);
    }

    /**
     * 删除缓存（二级缓存）
     */
    public Mono<Void> evictRemote(@NonNull String cacheName,@NonNull Object key) {
        return executeOnCache(cacheName, cache -> cache.evictRemote(key));
    }

    /**
     * 清空缓存
     */
    public Mono<Void> clear(){
        return clear(chenilleCache.getName());
    }

    /**
     * 清空缓存
     */
    public Mono<Void> clear(@NonNull String cacheName) {
        return executeOnCache(cacheName, Cache::clear);
    }

    /**
     * 清空缓存（本地一级缓存）
     */
    public Mono<Void> clearLocal() {
        return clearLocal(chenilleCache.getName());
    }

    /**
     * 清空缓存（本地一级缓存）
     */
    public Mono<Void> clearLocal(@NonNull String cacheName) {
        return executeOnCache(cacheName, ChenilleTwoLevelCache::clearLocal);
    }

    /**
     * 清空缓存（二级缓存）
     */
    public Mono<Void> clearRemote() {
        return clearRemote(chenilleCache.getName());
    }

    /**
     * 清空缓存（二级缓存）
     */
    public Mono<Void> clearRemote(@NonNull String cacheName) {
        return executeOnCache(cacheName, ChenilleTwoLevelCache::clearRemote);
    }

    /**
     * 获取或计算并缓存（如果不存在则存入）
     */
    public <K, V> Mono<V> computeIfAbsent(@NonNull K key,
                                    @NonNull Function<K, Mono<V>> mappingFunction) {
        return computeIfAbsent(chenilleCache.getName(), key, mappingFunction);
    }

    /**
     * 获取或计算并缓存（如果不存在则存入）
     */
    @SuppressWarnings("unchecked")
    public <K, V> Mono<V> computeIfAbsent(@NonNull String cacheName, @NonNull K key,
                                    @NonNull Function<K, Mono<V>> mappingFunction) {
        return executeOnCacheWithResult(cacheName, cache ->
                Mono.defer(() -> {
                    Cache.ValueWrapper wrapper = cache.get(key);
                    if (wrapper != null && wrapper.get() != null) {
                        return Mono.just((V) Objects.requireNonNull(wrapper.get()));
                    }
                    // 通过 Reactive Supplier 获取数据
                    return mappingFunction.apply(key)
                            .doOnNext(v -> cache.put(key, v));
                })
        );
    }

    /**
     * 执行缓存操作
     */
    private <T> Mono<T> executeOnCacheWithResult(@NonNull String cacheName,
                                           @NonNull Function<ChenilleTwoLevelCache, Mono<T>> function) {
        return Mono.defer(() -> {
            ChenilleTwoLevelCache cache = getCache(cacheName);
            if (cache == null) {
                return ChenilleChannelException.builder()
                        .logMessage("缓存 '" + cacheName + "' 不存在")
                        .build()
                        .logError()
                        .toMono();
            }
            return function.apply(cache);
        });
    }

    /**
     * 执行缓存操作
     */
    private Mono<Void> executeOnCache(@NonNull String cacheName,
                                @NonNull Consumer<ChenilleTwoLevelCache> consumer) {
        ChenilleTwoLevelCache cache = getCache(cacheName);
        if (cache != null) {
            consumer.accept(cache);
        }
        return Mono.empty();
    }

    @Nullable
    private ChenilleTwoLevelCache getCache(@NonNull String cacheName) {
        try {
            return cacheManager.getTwoLevelCache(cacheName);
        } catch (Exception e) {
            throw ChenilleChannelException.builder()
                    .logMessage("获取缓存 '" + cacheName + "' 失败")
                    .build();
        }
    }

    @FunctionalInterface
    private interface Consumer<T> {
        void accept(T t);
    }

    // ========================== Redis 工具 ====================================================

    /**
     * 检查 Redis 是否未启用
     */
    private boolean isRedisNotEnabled() {
        boolean notEnabled = reactiveRedisTemplate == null;
        if (notEnabled) log.warn("Redis 未启用，请检查 chenille.config.cache.redis 配置！");
        return notEnabled;
    }

    /**
     * 获取 Redis 中的字符串值
     */
    public Mono<String> getRedisString(@NonNull String key) {
        if (isRedisNotEnabled()) return Mono.empty();

        return reactiveRedisTemplate.opsForValue().get(key)
                .flatMap(value -> {
                    if (value == null) return Mono.empty();
                    if (value instanceof String str) return Mono.just(str);
                    if (value instanceof Number || value instanceof Boolean) return Mono.just(value.toString());
                    try {
                        return Mono.just(jsonUtils.toJson(value));
                    } catch (Exception e) {
                        log.warn("Redis 值转换 JSON 失败 key={} value={}", key, value, e);
                        return Mono.just(value.toString());
                    }
                });
    }

    /**
     * 获取 Redis 中的 List 值
     */
    public <T> Flux<T> getRedisList(@NonNull String key, @NonNull Class<T> clazz) {
        if (isRedisNotEnabled()) return Flux.empty();
        return reactiveRedisTemplate.opsForList().range(key, 0, -1)
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }

    /**
     * 获取 Redis 中的 Set 值
     */
    public <T> Flux<T> getRedisSet(@NonNull String key, @NonNull Class<T> clazz) {
        if (isRedisNotEnabled()) return Flux.empty();
        return reactiveRedisTemplate.opsForSet().members(key)
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }

    /**
     * 获取 Redis 中的 Map 值
     */
    public <K, V> Mono<Map<K, V>> getRedisMap(@NonNull String key,
                                        @NonNull Class<K> keyClazz,
                                        @NonNull Class<V> valueClazz) {
        if (isRedisNotEnabled()) return Mono.empty();

        return reactiveRedisTemplate.opsForHash().entries(key)
                // 过滤类型
                .filter(entry -> keyClazz.isInstance(entry.getKey()) && valueClazz.isInstance(entry.getValue()))
                // 转换类型
                .map(entry -> Map.entry(keyClazz.cast(entry.getKey()), valueClazz.cast(entry.getValue())))
                // 收集为 Map
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    /**
     * 获取 Redis 中的 JSON 值
     *
     * @param key           Redis 键
     * @param typeReference 类型引用
     * @return JSON 反序列化后的对象
     */
    public <T> Mono<T> getRedisJson(@NonNull String key, @NonNull TypeReference<T> typeReference) {
        return getRedisString(key)
                .flatMap(json -> {
                    try {
                        return Mono.just(jsonUtils.fromJson(json, typeReference));
                    } catch (Exception e) {
                        log.warn("Redis JSON 反序列化失败 key={} value={}", key, json, e);
                        return Mono.empty();
                    }
                });
    }

    /**
     * redis 缓存
     * <p>
     * 不必启用 chenille.config.cache.redis 配置
     * <p>
     * 不被二级缓存管理器管理，直接走 RedisTemplate
     *
     * @param ttl 过期时间
     * @param timeUnit 时间单位
     */
    public Mono<Void> putRedis(@NonNull String key, Object value,long ttl, @NonNull TimeUnit timeUnit){
        if (isRedisNotEnabled()) return Mono.empty();

        Mono<Boolean> putMono;

        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            putMono = reactiveRedisTemplate.opsForValue().set(key, value, Duration.ofMillis(timeUnit.toMillis(ttl)));
        } else if (value instanceof Map<?, ?> map) {
            putMono = reactiveRedisTemplate.opsForHash().putAll(key, map)
                    .then(reactiveRedisTemplate.expire(key, Duration.ofMillis(timeUnit.toMillis(ttl))));
        } else if (value instanceof List<?> list) {
            putMono = reactiveRedisTemplate.opsForList().rightPushAll(key, list)
                    .then(reactiveRedisTemplate.expire(key, Duration.ofMillis(timeUnit.toMillis(ttl))));
        } else if (value instanceof Set<?> set) {
            putMono = reactiveRedisTemplate.opsForSet().add(key, set.toArray())
                    .then(reactiveRedisTemplate.expire(key, Duration.ofMillis(timeUnit.toMillis(ttl))));
        } else {
            putMono = reactiveRedisTemplate.opsForValue().set(key, jsonUtils.toJson(value), Duration.ofMillis(timeUnit.toMillis(ttl)));
        }

        return putMono.then();
    }


    /**
     * 判断 Redis 中是否存在指定的 key
     */
    public Mono<Boolean> containsRedisKey(@NonNull String key) {
        if (isRedisNotEnabled()) return Mono.just(false);
        return reactiveRedisTemplate.hasKey(key);
    }

    /**
     * 删除 Redis 中的指定 key
     */
    public Mono<Boolean> deleteRedisKey(@NonNull String key) {
        if (isRedisNotEnabled()) return Mono.just(false);
        return reactiveRedisTemplate.delete(key).then(Mono.just(true));
    }

    /**
     * 更新 Redis 中指定 key 的过期时间
     */
    public Mono<Boolean> expireRedisKey(@NonNull String key, long ttl, @NonNull TimeUnit timeUnit) {
        if (isRedisNotEnabled()) return Mono.just(false);
        return reactiveRedisTemplate.expire(key, Duration.ofMillis(timeUnit.toMillis(ttl)));
    }
}
