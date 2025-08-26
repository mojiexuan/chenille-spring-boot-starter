package com.chenjiabao.open.chenille;

import io.micrometer.common.lang.NonNull;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * redis工具类
 *
 * @author ChenJiaBao
 */
public record RedisUtils(
        StringRedisTemplate stringRedisTemplate,
        RedisTemplate<String, Object> redisTemplate) {

    /**
     * 设置键值对并指定过期时间
     *
     * @param key     键
     * @param value   值
     * @param seconds 过期时间 秒
     */
    public void set(String key, String value, long seconds) {
        stringRedisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    /**
     * 写入List
     *
     * @param key     键
     * @param values  List数组
     * @param seconds 过期时间 秒
     */
    public <T> void setList(String key, @NonNull List<T> values, long seconds) {
        if (values.isEmpty()) {
            return;
        }

        ListOperations<String, Object> opsForList = redisTemplate.opsForList();
        opsForList.rightPushAll(key, values.toArray());

        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    /**
     * 获取List
     *
     * @param key 键
     */
    public <T> List<T> getList(String key) {
        if (!redisTemplate.hasKey(key)) {
            return Collections.emptyList();
        }

        List<Object> objects = redisTemplate.opsForList().range(key, 0, -1);
        if (objects == null || objects.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<>();
        for (Object obj : objects) {
            try {
                list.add((T) obj);
            } catch (Exception ignored) {
            }
        }
        return list;
    }

    /**
     * 通过key获取值
     *
     * @param key 键
     * @return 值
     */
    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除
     *
     * @param key 键
     */
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 判断键是否存在
     *
     * @param key 键
     */
    public boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 设置新的过期时间
     *
     * @param key     key
     * @param seconds 时间
     */
    public void expire(String key, long seconds) {
        stringRedisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

}
