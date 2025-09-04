package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Redis 缓存配置
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleCacheRedis {
    /**
     * 是否启用
     */
    private boolean enabled = false;
    /**
     * 缓存名称
     */
    private String name = "chenille-redis-cache";
    /**
     * 缓存过期时间(单位：毫秒)
     */
    private long expire = 60 * 60 * 1000;
}
