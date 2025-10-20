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
                                 ChenilleJwtUtils chenilleJwtUtils,
                                 ChenilleObjectUtils chenilleObjectUtils) {

    public ChenilleLoginUtils {
        chenilleObjectUtils.requireNonNull(chenilleCacheUtils, "chenille.config.cache.redis 未启用");
        chenilleObjectUtils.requireNonNull(chenilleJwtUtils, "chenille.config.jwt 未启用");
    }

    /**
     * 登录
     *
     * @param userId 用户唯一标识
     * @return 登录凭证
     */
    @NonNull
    public Mono<String> login(@NonNull String userId) {
        String userKey = ChenilleInternalEnum.UserCacheKey.USER_KEY_INFO.getValue().formatted(userId);
        long expire = chenilleLogin.getExpire();

        return chenilleCacheUtils.getRedisString(userKey)
                .flatMap(existingToken -> {
                    // 用户已登录，延长过期时间
                    String tokenKey = ChenilleInternalEnum.UserCacheKey.USER_KEY_TOKEN.getValue().formatted(existingToken);
                    return Mono.when(
                            chenilleCacheUtils.expireRedisKey(userKey, expire, TimeUnit.SECONDS),
                            chenilleCacheUtils.expireRedisKey(tokenKey, expire, TimeUnit.SECONDS)
                    ).thenReturn(existingToken);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // 用户未登录，生成新 token
                    return chenilleJwtUtils.generateToken(userId)
                            .flatMap(newToken -> {
                                String tokenKey = ChenilleInternalEnum.UserCacheKey.USER_KEY_TOKEN.getValue().formatted(newToken);
                                return Mono.when(
                                        chenilleCacheUtils.putRedis(userKey, newToken, expire, TimeUnit.SECONDS),
                                        chenilleCacheUtils.putRedis(tokenKey, userId, expire, TimeUnit.SECONDS)
                                ).thenReturn(newToken);
                            });
                }));
    }

    /**
     * 刷新token
     *
     * @param token 旧token
     * @return 新token
     */
    public Mono<String> refresh(@NonNull String token) {
        return extractUserId(token)
                .flatMap(this::login)
                .switchIfEmpty(Mono.error(new ChenilleChannelException(
                        ChenilleResponseCode.UNAUTHORIZED,
                        "凭证已失效，请重新登录")));
    }

    /**
     * 检查用户是否已登录
     *
     * @param userId 用户唯一标识
     * @return true 如果用户已登录，否则 false
     */
    public Mono<Boolean> isUserLoggedIn(@NonNull String userId) {
        String userKey = ChenilleInternalEnum.UserCacheKey.USER_KEY_INFO.getValue().formatted(userId);
        return chenilleCacheUtils.containsRedisKey(userKey);
    }

    /**
     * 验证token是否有效
     *
     * @param token 登录凭证
     * @return true 如果token有效，否则 false
     */
    public Mono<Boolean> verifyToken(@NonNull String token) {
        return extractUserId(token)
                .flatMap(userId -> Mono.just(true))
                .switchIfEmpty(Mono.just(false));
    }

    /**
     * 退出登录
     *
     * @param userId 用户唯一标识
     */
    public Mono<Void> logout(@NonNull String userId) {
        String userKey = ChenilleInternalEnum.UserCacheKey.USER_KEY_INFO.getValue().formatted(userId);
        return chenilleCacheUtils.getRedisString(userKey)
                .flatMap(token -> {
                    String tokenKey = ChenilleInternalEnum.UserCacheKey.USER_KEY_TOKEN.getValue().formatted(token);
                    return Mono.when(
                            chenilleCacheUtils.deleteRedisKey(userKey),
                            chenilleCacheUtils.deleteRedisKey(tokenKey)
                    ).then();
                }).then(); // 如果没登录也直接完成
    }

    /**
     * 获取用户ID
     *
     * @param token 登录凭证
     * @return 用户唯一标识
     */
    public Mono<String> extractUserId(@NonNull String token) {
        return chenilleJwtUtils.isTokenValid(token)
                .flatMap(isValid->{
                    if (!isValid) {
                        return Mono.error(new ChenilleChannelException(
                                ChenilleResponseCode.UNAUTHORIZED,
                                "登录凭证失效"));
                    }
                    String tokenKey = ChenilleInternalEnum.UserCacheKey.USER_KEY_TOKEN.getValue().formatted(token);
                    return chenilleCacheUtils.getRedisString(tokenKey)
                            .switchIfEmpty(Mono.error(new ChenilleChannelException(
                                    ChenilleResponseCode.UNAUTHORIZED,
                                    "登录凭证不存在")));
                });
    }

}
