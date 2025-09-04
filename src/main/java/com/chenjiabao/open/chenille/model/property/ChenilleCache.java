package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 缓存配置
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleCache {
    /**
     * 缓存名称
     */
    private String name = "chenille-cache";
    /**
     * 缓存主题
     */
    private String topic = "chenille-cache-topic";
    /**
     * Caffeine 缓存配置
     */
    @NestedConfigurationProperty
    private ChenilleCacheCaffeine caffeine;
    /**
     * Redis 缓存配置
     */
    @NestedConfigurationProperty
    private ChenilleCacheRedis redis;
}
