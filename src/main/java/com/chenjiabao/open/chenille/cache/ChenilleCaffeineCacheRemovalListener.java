package com.chenjiabao.open.chenille.cache;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

@Slf4j
public class ChenilleCaffeineCacheRemovalListener implements RemovalListener<Object, Object> {

    @Override
    public void onRemoval(@Nullable Object key,
                          @Nullable Object value,
                          @NonNull RemovalCause cause) {
        log.info("Caffeine [一级缓存] 移除: {}, value: {}, cause: {}", key, value, cause.name());
        // 超出最大缓存
        if (cause == RemovalCause.SIZE) {
        }
        // 超出过期时间
        if (cause == RemovalCause.EXPIRED) {
        }
        // 显式移除
        if (cause == RemovalCause.EXPLICIT) {
        }
        // 旧数据被更新
        if (cause == RemovalCause.REPLACED) {
        }
    }
}
