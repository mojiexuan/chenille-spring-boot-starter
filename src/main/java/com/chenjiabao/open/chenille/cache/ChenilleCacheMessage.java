package com.chenjiabao.open.chenille.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * 缓存消息
 */
@Data
public class ChenilleCacheMessage implements Serializable {
    private String cacheName;
    private Object key;
    private Object value;
}
