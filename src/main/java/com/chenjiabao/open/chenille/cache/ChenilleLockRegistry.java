package com.chenjiabao.open.chenille.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存锁注册中心
 */
public class ChenilleLockRegistry {
    // 存放锁对象
    private static final Map<String, Object> LOCKS = new ConcurrentHashMap<>();
    // 引用队列，用于清理已经被垃圾回收的对象，防止锁池无限增长
    private static final ReferenceQueue<Object> QUEUE = new ReferenceQueue<>();

    /**
     * 根据 key 获取一个锁对象
     * @param key 锁的 key
     * @return 锁对象
     */
    public static Object getLock(Object key) {
        // 把 key 转成字符串，保证一致性
        String lockKey = String.valueOf(key);

        // 清理已经被 GC 回收的弱引用，避免内存泄漏
        WeakReference<Object> ref;
        while ((ref = (WeakReference<Object>) QUEUE.poll()) != null) {
            LOCKS.values().remove(ref);
        }

        // 如果锁池里没有这个 key，就创建一个新的 Object 作为锁
        return LOCKS.computeIfAbsent(lockKey, k -> new Object());
    }
}
