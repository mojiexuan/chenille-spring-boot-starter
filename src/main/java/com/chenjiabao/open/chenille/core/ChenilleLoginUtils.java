package com.chenjiabao.open.chenille.core;

import com.chenjiabao.open.chenille.enums.ChenilleInternalEnum;
import com.chenjiabao.open.chenille.enums.ChenilleResponseCode;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.model.property.ChenilleLogin;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * 登录工具类
 */
@Slf4j
public record ChenilleLoginUtils(ChenilleLogin chenilleLogin,
                                 ChenilleCacheUtils chenilleCacheUtils,
                                 ChenilleJwtUtils chenilleJwtUtils) {

    public ChenilleLoginUtils(ChenilleLogin chenilleLogin,
                              ChenilleCacheUtils chenilleCacheUtils,
                              ChenilleJwtUtils chenilleJwtUtils) {
        this.chenilleLogin = chenilleLogin;
        if (chenilleCacheUtils == null) {
            log.error("要使用 ChenilleLoginUtils ，请先配置 chenille.config.cache.redis 启用");
            throw new ChenilleChannelException("要使用 ChenilleLoginUtils ，请先配置 chenille.config.cache.redis 启用");
        }
        if (chenilleJwtUtils == null) {
            log.error("要使用 ChenilleLoginUtils ，请先配置 chenille.config.jwt 启用");
            throw new ChenilleChannelException("要使用 ChenilleLoginUtils ，请先配置 chenille.config.jwt 启用");
        }
        this.chenilleJwtUtils = chenilleJwtUtils;
        this.chenilleCacheUtils = chenilleCacheUtils;
    }

    /**
     * 登录
     *
     * @param userId 用户唯一标识
     * @return 登录凭证
     */
    @NonNull
    public String login(@NonNull String userId) {
        String token = chenilleJwtUtils.createToken(userId);
        // 之前已登录的，先退出登录
        logout(userId);
        // 缓存登录凭证
        chenilleCacheUtils.putRedis(ChenilleInternalEnum.UserCacheKey.USER_KEY_INFO.getValue().formatted(userId),
                token,
                chenilleLogin.getExpire(),
                TimeUnit.SECONDS);
        // 缓存用户ID
        chenilleCacheUtils.putRedis(ChenilleInternalEnum.UserCacheKey.USER_KEY_TOKEN.getValue().formatted(token),
                userId,
                chenilleLogin.getExpire(),
                TimeUnit.SECONDS);
        return token;
    }

    /**
     * 刷新token
     *
     * @param token 旧token
     * @return 新token
     */
    public Mono<String> refresh(@NonNull String token) {
        return getUserId(token)
                .flatMap(userId -> {
                    if (!isLoggedIn(userId)) {
                        return Mono.error(new ChenilleChannelException(
                                ChenilleResponseCode.UNAUTHORIZED,
                                "凭证已失效，请重新登录"));
                    }
                    String newToken = login(userId);
                    return Mono.just(newToken);
                });
    }

    /**
     * 检查用户是否已登录
     *
     * @param userId 用户唯一标识
     * @return true 如果用户已登录，否则 false
     */
    public boolean isLoggedIn(@NonNull String userId) {
        return chenilleCacheUtils.containsRedisKey(ChenilleInternalEnum.UserCacheKey.USER_KEY_INFO.getValue().formatted(userId));
    }

    /**
     * 退出登录
     *
     * @param userId 用户唯一标识
     */
    public void logout(@NonNull String userId) {
        if (isLoggedIn(userId)) {
            chenilleCacheUtils.getRedisString(ChenilleInternalEnum.UserCacheKey.USER_KEY_INFO.getValue().formatted(userId))
                    .ifPresent(token -> {
                        chenilleCacheUtils.deleteRedisKey(ChenilleInternalEnum.UserCacheKey.USER_KEY_INFO.getValue().formatted(userId));
                        chenilleCacheUtils.deleteRedisKey(ChenilleInternalEnum.UserCacheKey.USER_KEY_TOKEN.getValue().formatted(token));
                    });
        }
    }

    /**
     * 获取用户ID
     *
     * @param token 登录凭证
     * @return 用户唯一标识
     */
    public Mono<String> getUserId(@NonNull String token) {
        return Mono.justOrEmpty(
                chenilleCacheUtils.getRedisString(
                        ChenilleInternalEnum.UserCacheKey.USER_KEY_TOKEN.getValue().formatted(token)
                )
        ).switchIfEmpty(Mono.error(new ChenilleChannelException(
                ChenilleResponseCode.UNAUTHORIZED,
                "登录凭证不存在")));
    }

}
