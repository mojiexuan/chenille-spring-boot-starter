package com.chenjiabao.open.chenille.core;

import com.chenjiabao.open.chenille.cache.ChenilleTwoLevelCache;
import com.chenjiabao.open.chenille.cache.ChenilleTwoLevelCacheManager;
import com.chenjiabao.open.chenille.enums.ChenilleCacheType;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.model.property.ChenilleCache;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;

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
                                 RedisTemplate<String, Object> redisTemplate,
                                 ChenilleJsonUtils  jsonUtils) {

    /**
     * 放入缓存（默认缓存）
     */
    public void put(@NonNull String key, @NonNull Object value) {
        put(chenilleCache.getName(), key, value);
    }

    /**
     * 放入缓存
     */
    public void put(@NonNull String cacheName,@NonNull String key, @NonNull Object value) {
        executeOnCache(cacheName, cache -> cache.put(key, value));
    }

    /**
     * 放入缓存，仅放入一级缓存
     */
    public void putLocal(@NonNull Object key, Object value){
        putLocal(chenilleCache.getName(), key, value);
    }

    /**
     * 放入缓存，仅放入一级缓存
     */
    public void putLocal(@NonNull String cacheName,@NonNull Object key, Object value){
        executeOnCache(cacheName, cache -> cache.putLocal(key, value));
    }

    /**
     * 放入缓存，仅放入二级缓存
     */
    public void putRemote(@NonNull Object key, Object value){
        putRemote(chenilleCache.getName(), key, value);
    }

    /**
     * 放入缓存，仅放入二级缓存
     */
    public void putRemote(@NonNull String cacheName,@NonNull Object key, Object value){
        executeOnCache(cacheName, cache -> cache.putRemote(key, value));
    }

    /**
     * 获取缓存值（默认缓存）
     */
    @Nullable
    public <K, V> V get(@NonNull K key, @NonNull Class<V> type) {
        return get(chenilleCache.getName(), key, type);
    }

    /**
     * 获取缓存值
     */
    @Nullable
    public <K, V> V get(@NonNull String cacheName, @NonNull K key, @NonNull Class<V> type) {
        return executeOnCacheWithResult(cacheName, cache -> cache.get(key, type));
    }

    /**
     * 获取缓存值（默认缓存）
     */
    @Nullable
    public <K, V> V get(@NonNull K key, @NonNull Callable<V> valueLoader) {
        return get(chenilleCache.getName(), key, valueLoader);
    }

    /**
     * 获取缓存值（带加载器）
     */
    @Nullable
    public <K, V> V get(@NonNull String cacheName, @NonNull K key, @NonNull Callable<V> valueLoader) {
        return executeOnCacheWithResult(cacheName, cache -> cache.get(key, valueLoader));
    }

    /**
     * 删除缓存（默认缓存）
     */
    public void evict(@NonNull Object key) {
        evict(chenilleCache.getName(), key);
    }

    /**
     * 删除缓存
     */
    public void evict(@NonNull String cacheName,@NonNull Object key) {
        executeOnCache(cacheName, cache -> cache.evict(key));
    }

    /**
     * 删除缓存（本地一级缓存）
     */
    public void evictLocal(@NonNull Object key) {
        evictLocal(chenilleCache.getName(), key);
    }

    /**
     * 删除缓存（本地一级缓存）
     */
    public void evictLocal(@NonNull String cacheName,@NonNull Object key) {
        executeOnCache(cacheName, cache -> cache.evictLocal(key));
    }

    /**
     * 删除缓存（二级缓存）
     */
    public void evictRemote(@NonNull Object key) {
        evictRemote(chenilleCache.getName(), key);
    }

    /**
     * 删除缓存（二级缓存）
     */
    public void evictRemote(@NonNull String cacheName,@NonNull Object key) {
        executeOnCache(cacheName, cache -> cache.evictRemote(key));
    }

    /**
     * 清空缓存
     */
    public void clear(){
        clear(chenilleCache.getName());
    }

    /**
     * 清空缓存
     */
    public void clear(@NonNull String cacheName) {
        executeOnCache(cacheName, Cache::clear);
    }

    /**
     * 清空缓存（本地一级缓存）
     */
    public void clearLocal() {
        clearLocal(chenilleCache.getName());
    }

    /**
     * 清空缓存（本地一级缓存）
     */
    public void clearLocal(@NonNull String cacheName) {
        executeOnCache(cacheName, ChenilleTwoLevelCache::clearLocal);
    }

    /**
     * 清空缓存（二级缓存）
     */
    public void clearRemote() {
        clearRemote(chenilleCache.getName());
    }

    /**
     * 清空缓存（二级缓存）
     */
    public void clearRemote(@NonNull String cacheName) {
        executeOnCache(cacheName, ChenilleTwoLevelCache::clearRemote);
    }

    /**
     * 获取或计算并缓存（如果不存在则存入）
     */
    @Nullable
    public <K, V> V computeIfAbsent(@NonNull K key,
                                    @NonNull Function<K, V> mappingFunction) {
        return computeIfAbsent(chenilleCache.getName(), key, mappingFunction);
    }

    /**
     * 获取或计算并缓存（如果不存在则存入）
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <K, V> V computeIfAbsent(@NonNull String cacheName, @NonNull K key,
                                    @NonNull Function<K, V> mappingFunction) {
        return executeOnCacheWithResult(cacheName,cache -> {
            Cache.ValueWrapper value = cache.get(key);
            if (value == null) {
                V v = mappingFunction.apply(key);
                cache.put(key, v);
                return v;
            }
            return (V) value.get();
        });
    }

    /**
     * 执行缓存操作
     */
    @Nullable
    private <T> T executeOnCacheWithResult(@NonNull String cacheName,
                                           @NonNull Function<ChenilleTwoLevelCache, T> function) {
        ChenilleTwoLevelCache cache = getCache(cacheName);
        return cache != null ? function.apply(cache) : null;
    }

    /**
     * 执行缓存操作
     */
    private void executeOnCache(@NonNull String cacheName,
                                @NonNull Consumer<ChenilleTwoLevelCache> consumer) {
        ChenilleTwoLevelCache cache = getCache(cacheName);
        if (cache != null) {
            consumer.accept(cache);
        }
    }

    @Nullable
    private ChenilleTwoLevelCache getCache(@NonNull String cacheName) {
        try {
            return cacheManager.getTwoLevelCache(cacheName);
        } catch (Exception e) {
            throw new ChenilleChannelException("获取缓存 '" + cacheName + "' 失败");
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
        if(redisTemplate == null){
            log.warn("你需要启用chenille.config.cache.redis配置!");
            return true;
        }
        return false;
    }

    /**
     * 获取 Redis 中的字符串值
     */
    public String getRedisString(@NonNull String key) {
        if(isRedisNotEnabled()){
            return null;
        }
        Object object = redisTemplate.opsForValue().get(key);
        if (object == null) {
            return null;
        }
        if (object instanceof String str) {
            return str;
        }
        if (object instanceof Number || object instanceof Boolean) {
            return object.toString(); // 基础类型安全 toString
        }
        try {
            return jsonUtils.toJson(object);
        } catch (Exception e) {
            log.warn("Redis String 转换失败 -> {}", object, e);
            return object.toString(); // 最后兜底
        }
    }

    /**
     * 获取 Redis 中的 List 值
     */
    public <T> List<T> getRedisList(@NonNull String key, @NonNull Class<T> clazz) {
        if(isRedisNotEnabled()){
            return null;
        }
        List<Object> objects = redisTemplate.opsForList().range(key, 0, -1);
        if (objects == null || objects.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> list = new ArrayList<>();
        for (Object obj : objects) {
            if (clazz.isInstance(obj)) {
                list.add(clazz.cast(obj));  // 安全转换
            }
        }
        return list;
    }

    /**
     * 获取 Redis 中的 Set 值
     */
    public <T> Set<T> getRedisSet(@NonNull String key, @NonNull Class<T> clazz) {
        if(isRedisNotEnabled()){
            return null;
        }
        Set<T> set = new HashSet<>();
        Set<Object> objects = redisTemplate.opsForSet().members(key);
        if (objects == null || objects.isEmpty()) {
            return Collections.emptySet();
        }
        for (Object obj : objects) {
            if (clazz.isInstance(obj)) {
                set.add(clazz.cast(obj));  // 安全转换
            }
        }
        return set;
    }

    /**
     * 获取 Redis 中的 Map 值
     */
    public <K, V> Map<K, V> getRedisMap(@NonNull String key,
                                        @NonNull Class<K> keyClazz,
                                        @NonNull Class<V> valueClazz) {
        if(isRedisNotEnabled()){
            return null;
        }
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        if (map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            if (keyClazz.isInstance(entry.getKey()) && valueClazz.isInstance(entry.getValue())) {
                result.put(keyClazz.cast(entry.getKey()), valueClazz.cast(entry.getValue()));
            }
        }
        return result;
    }

    /**
     * 获取 Redis 中的 JSON 值
     *
     * @param key           Redis 键
     * @param typeReference 类型引用
     * @return JSON 反序列化后的对象
     */
    public <T> T getRedisJson(@NonNull String key, @NonNull TypeReference<T> typeReference) {
        if(isRedisNotEnabled()){
            return null;
        }
        String json = getRedisString(key);
        if (json == null) {
            return null;
        }
        try {
            return jsonUtils.fromJson(json, typeReference);
        } catch (Exception e) {
            log.warn("Redis JSON 反序列化失败 key={} value={}", key, json, e);
            return null;
        }
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
    public void putRedis(@NonNull String key, Object value,long ttl, @NonNull TimeUnit timeUnit){
        if(isRedisNotEnabled()){
            return;
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            // 直接存为 String
            redisTemplate.opsForValue().set(key, value, ttl, timeUnit);
        } else if (value instanceof Map<?, ?>) {
            // 存为 Hash
            redisTemplate.opsForHash().putAll(key, (Map<?, ?>) value);
            redisTemplate.expire(key, ttl, timeUnit);
        } else if (value instanceof List<?>) {
            // 存为 List
            redisTemplate.opsForList().rightPushAll(key, value);
            redisTemplate.expire(key, ttl, timeUnit);
        } else if (value instanceof Set<?>) {
            // 存为 Set
            redisTemplate.opsForSet().add(key, ((Set<?>) value).toArray());
            redisTemplate.expire(key, ttl, timeUnit);
        } else {
            // 其他对象 → 转 JSON 存 String
            redisTemplate.opsForValue().set(key, jsonUtils.toJson(value), ttl, timeUnit);
        }
    }

    /**
     * redis 缓存
     * <p>
     * 不必启用 chenille.config.cache.redis 配置
     * <p>
     * 不被二级缓存管理器管理，直接走 RedisTemplate
     *
     * @param type 缓存类型（STRING / HASH / LIST / SET / JSON）
     * @param ttl 过期时间
     * @param timeUnit 时间单位
     */
    public void putRedis(@NonNull String key,
                         Object value,
                         @NonNull ChenilleCacheType type,
                         long ttl,
                         @NonNull TimeUnit timeUnit){
        if(isRedisNotEnabled()){
            return;
        }
        switch (type) {
            case STRING -> {
                // 普通类型：String / Number / Boolean，否则走 JSON
                if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                    redisTemplate.opsForValue().set(key, value, ttl, timeUnit);
                    return;
                }
            }
            case HASH -> {
                if (value instanceof Map<?, ?> map) {
                    redisTemplate.opsForHash().putAll(key, map);
                    redisTemplate.expire(key, ttl, timeUnit);
                    return;
                }
            }
            case LIST -> {
                if (value instanceof List<?> list) {
                    redisTemplate.opsForList().rightPushAll(key, list);
                    redisTemplate.expire(key, ttl, timeUnit);
                    return;
                }
            }
            case SET -> {
                if (value instanceof Collection<?> collection) {
                    redisTemplate.opsForSet().add(key, collection.toArray());
                    redisTemplate.expire(key, ttl, timeUnit);
                    return;
                }else if (value.getClass().isArray()) {
                    redisTemplate.opsForSet().add(key, (Object[]) value);
                    redisTemplate.expire(key, ttl, timeUnit);
                    return;
                }
            }
        }
        redisTemplate.opsForValue().set(key, jsonUtils.toJson(value), ttl, timeUnit);
    }

    /**
     * 判断 Redis 中是否存在指定的 key
     */
    public boolean containsRedisKey(@NonNull String key) {
        if(isRedisNotEnabled()){
            return false;
        }
        return redisTemplate.hasKey(key);
    }

    /**
     * 删除 Redis 中的指定 key
     */
    public boolean deleteRedisKey(@NonNull String key) {
        if(isRedisNotEnabled()){
            return false;
        }
        return redisTemplate.delete(key);
    }

    /**
     * 更新 Redis 中指定 key 的过期时间
     */
    public boolean updateRedisKeyExpire(@NonNull String key, long ttl, @NonNull TimeUnit timeUnit) {
        if(isRedisNotEnabled()){
            return false;
        }
        return redisTemplate.expire(key, ttl, timeUnit);
    }
}
