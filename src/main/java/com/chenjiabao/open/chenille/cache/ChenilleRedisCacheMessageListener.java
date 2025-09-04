package com.chenjiabao.open.chenille.cache;

import com.chenjiabao.open.chenille.core.ChenilleJsonUtils;
import com.chenjiabao.open.chenille.model.property.ChenilleCache;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * 缓存消息监听器
 */
@Slf4j
public record ChenilleRedisCacheMessageListener(ChenilleCache chenilleCache,
                                                ChenilleTwoLevelCacheManager twoLevelCacheManager,
                                                ChenilleJsonUtils chenilleJsonUtils)
        implements MessageListener {

    @Override
    public void onMessage(@NonNull Message message,
                          byte[] pattern) {
        try {
            ChenilleCacheMessage chenilleCacheMessage = chenilleJsonUtils.fromJsonBytes(message.getBody(),
                    ChenilleCacheMessage.class);

            if (chenilleCacheMessage != null) {
                // 只清理一级缓存，避免再次触发广播
                twoLevelCacheManager
                        .getTwoLevelCache(chenilleCache.getName())
                        .evictLocal(chenilleCacheMessage.getKey());
            }
        } catch (Exception e) {
            log.error("处理缓存失效通知异常", e);
        }
    }
}
