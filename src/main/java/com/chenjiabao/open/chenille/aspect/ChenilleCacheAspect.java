package com.chenjiabao.open.chenille.aspect;

import com.chenjiabao.open.chenille.annotation.ChenilleCacheEvict;
import com.chenjiabao.open.chenille.annotation.ChenilleCacheable;
import com.chenjiabao.open.chenille.cache.ChenilleSpElParser;
import com.chenjiabao.open.chenille.core.ChenilleCacheUtils;
import com.chenjiabao.open.chenille.core.ChenilleStringUtils;
import com.chenjiabao.open.chenille.enums.ChenilleCacheType;
import com.chenjiabao.open.chenille.model.property.ChenilleCache;
import com.fasterxml.jackson.core.type.TypeReference;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Type;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Aspect
public record ChenilleCacheAspect(ChenilleCache chenilleCache,
                                  ChenilleCacheUtils cacheUtils,
                                  ChenilleStringUtils stringUtils) {

    @Around("@annotation(chenilleCacheable)")
    public Object aroundCacheable(ProceedingJoinPoint joinPoint,
                                  ChenilleCacheable chenilleCacheable) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();
        Type genericReturnType = signature.getMethod().getGenericReturnType();

        // 解析key
        String key = ChenilleSpElParser.parseKey(chenilleCacheable.key(), joinPoint);

        String cacheName = chenilleCacheable.cacheName();
        if (stringUtils.isBlank(cacheName)) {
            cacheName = chenilleCache.getName();
        }

        // 尝试从缓存获取
        Object result;

        long ttl = chenilleCacheable.ttl();
        if(ttl <= 0){
            // ----------- 一级/二级缓存模式 ------------
            result = cacheUtils.get(cacheName, key, returnType);

            if (result != null) {
                return result;
            }

            // 执行方法
            result = joinPoint.proceed();

            // 二级缓存
            if(chenilleCacheable.syncToSecondary()){
                cacheUtils.put(cacheName, key, result);
            }else {
                cacheUtils.putLocal(cacheName, key, result);
            }
        }else {
            // ----------- 纯 Redis 模式 ------------
            result = cacheUtils.getRedisJson(key, new TypeReference<>() {
                @Override
                public Type getType() {
                    return genericReturnType;
                }
            });

            if (result != null) {
                return result;
            }

            // 执行方法
            result = joinPoint.proceed();

            // 走纯redis
            if (chenilleCacheable.randomRange() > 0) {
                ttl += ThreadLocalRandom.current().nextLong(chenilleCacheable.randomRange());
            }
            cacheUtils.putRedis(key,result, ChenilleCacheType.JSON,ttl,TimeUnit.MINUTES);
        }

        return result;
    }

    @After("@annotation(chenilleCacheEvict)")
    public void afterCacheEvict(JoinPoint joinPoint, ChenilleCacheEvict chenilleCacheEvict) {
        String key = ChenilleSpElParser.parseKey(chenilleCacheEvict.key(), joinPoint);

        String cacheName = chenilleCacheEvict.cacheName();
        if (stringUtils.isBlank(cacheName)) {
            cacheName = chenilleCache.getName();
        }

        cacheUtils.evict(cacheName, key);
    }
}
