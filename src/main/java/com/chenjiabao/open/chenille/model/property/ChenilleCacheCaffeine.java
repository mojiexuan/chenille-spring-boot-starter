package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Caffeine 缓存配置
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleCacheCaffeine {
    /**
     * 是否启用
     */
    private boolean enabled = false;
    /**
     * 缓存名称
     */
    private String name = "chenille-caffeine-cache";
    /**
     * 最大容量
     * <p>
     * 默认为可用堆内存的 10%
     */
    private Integer maximumSize;
    /**
     * 初始容量
     * <p>
     * 默认为 最大容量的 10%
     */
    private Integer initialCapacity;
    /**
     * 缓存过期时间(单位：毫秒)
     */
    private Long expire = 60 * 60 * 1000L;
}
