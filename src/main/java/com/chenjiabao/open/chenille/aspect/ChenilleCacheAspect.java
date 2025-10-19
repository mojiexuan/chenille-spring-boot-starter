package com.chenjiabao.open.chenille.aspect;

import com.chenjiabao.open.chenille.annotation.ChenilleCacheEvict;
import com.chenjiabao.open.chenille.annotation.ChenilleCacheable;
import com.chenjiabao.open.chenille.cache.ChenilleSpElParser;
import com.chenjiabao.open.chenille.core.ChenilleCacheUtils;
import com.chenjiabao.open.chenille.core.ChenilleStringUtils;
import com.chenjiabao.open.chenille.model.property.ChenilleCache;
import com.fasterxml.jackson.core.type.TypeReference;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

@Aspect
public record ChenilleCacheAspect(ChenilleCache chenilleCache,
                                  ChenilleCacheUtils cacheUtils,
                                  ChenilleStringUtils stringUtils) {

    @Around("@annotation(chenilleCacheable)")
    public Mono<Object> aroundCacheable(ProceedingJoinPoint joinPoint,
                                ChenilleCacheable chenilleCacheable) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();
        Type genericReturnType = signature.getMethod().getGenericReturnType();

        // 解析key
        String key = ChenilleSpElParser.parseKey(chenilleCacheable.key(), joinPoint);

        // 获取缓存名
        String cacheName = stringUtils.isBlank(chenilleCacheable.cacheName())
                ? chenilleCache.getName()
                : chenilleCacheable.cacheName();

        long ttl = chenilleCacheable.ttl();

        if(ttl <= 0){
            // ----------- 一级/二级缓存模式 ------------
            return cacheUtils.computeIfAbsent(cacheName, key, k ->
                    Mono.defer(() -> {
                        try {
                            Object result = joinPoint.proceed();
                            return Mono.just(result);
                        } catch (Throwable e) {
                            return Mono.error(e);
                        }
                    })
            ).cast(Object.class);
        }else {
            // ----------- 纯 Redis 模式 ------------
            return cacheUtils.getRedisJson(key, new TypeReference<>() {
                @Override
                public Type getType() {
                    return genericReturnType;
                }
            }).switchIfEmpty(
                    Mono.defer(() -> {
                        try {
                            Object result = joinPoint.proceed();
                            return cacheUtils.putRedis(key, result, ttl, TimeUnit.MINUTES)
                                    .thenReturn(result);
                        } catch (Throwable e) {
                            return Mono.error(e);
                        }
                    })
            );
        }
    }

    @After("@annotation(chenilleCacheEvict)")
    public Mono<Void> afterCacheEvict(JoinPoint joinPoint, ChenilleCacheEvict chenilleCacheEvict) {
        String key = ChenilleSpElParser.parseKey(chenilleCacheEvict.key(), joinPoint);

        String cacheName = stringUtils.isBlank(chenilleCacheEvict.cacheName())
                ? chenilleCache.getName()
                : chenilleCacheEvict.cacheName();

        return cacheUtils.evict(cacheName, key);
    }
}
