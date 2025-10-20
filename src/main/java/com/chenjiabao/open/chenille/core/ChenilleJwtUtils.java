package com.chenjiabao.open.chenille.core;

import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.model.property.ChenilleJwt;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;

/**
 * jwt工具（支持东8区时间）
 *
 * @author ChenJiaBao
 */
@Slf4j
public class ChenilleJwtUtils {

    private final ChenilleJwt chenilleJwt;
    private final ChenilleJsonUtils chenilleJsonUtils;

    // 默认密钥（至少 32 字符）
    private SecretKey secretKey;

    public ChenilleJwtUtils(ChenilleJwt chenilleJwt,
                            @Autowired(required = false) ChenilleJsonUtils chenilleJsonUtils,
                            ChenilleStringUtils chenilleStringUtils) {
        this.chenilleJwt = chenilleJwt;
        if (chenilleJsonUtils == null) {
            throw new ChenilleChannelException("启用JWT功能需要同时启用 chenille.config.jackson.json.enabled=true");
        }
        this.chenilleJsonUtils = chenilleJsonUtils;
        if (chenilleStringUtils.isEmpty(chenilleJwt.getSecret())) {
            secretKey = Jwts.SIG.HS256.key().build();
        } else {
            this.setJwtSecret(chenilleJwt.getSecret());
        }
    }

    /**
     * 设置秘钥（只能设置一次）
     *
     * @param jwtSecret 新的秘钥（建议使用Base64编码的32位以上字符串）
     */
    public synchronized void setJwtSecret(String jwtSecret) {
        if (secretKey == null) {
            try {
                // 解码Base64字符串
                byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);

                // 检查密钥的字节长度是否至少为 32 字节（256位）
                if (decodedKey.length < 32) {
                    throw new IllegalArgumentException("JWT 密钥的长度不足，必须至少为32字节（256位）。");
                }
                secretKey = new SecretKeySpec(decodedKey, "HmacSHA256");
            } catch (Exception e) {
                log.error("配置的 JWT 密钥应该确保是有效的 Base64 编码字符串", e);
            }
        }
    }

    /**
     * 生成Token（东8区时间）
     *
     * @param subject 荷载
     * @return 生成的JWT Token
     */
    public Mono<String> generateToken(Object subject) {
        return Mono.fromCallable(() -> chenilleJsonUtils.toJson(subject)) // 将对象转 JSON
                .map(subjectJson -> {
                    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT+8"));
                    Date issuedAt = Date.from(now.toInstant());
                    Date expiration = Date.from(now.plusSeconds(chenilleJwt.getExpires()).toInstant());

                    return Jwts.builder()
                            .subject(subjectJson)
                            .issuedAt(issuedAt)
                            .expiration(expiration)
                            .signWith(this.secretKey, Jwts.SIG.HS256)
                            .compact();
                })
                .onErrorResume(e ->
                        Mono.error(new ChenilleChannelException("创建Token失败")));
    }

    /**
     * 解析Token
     *
     * @param token JWT Token
     * @return 声明内容
     */
    public Mono<Claims> parseClaims(String token) {
        return Mono.fromCallable(() -> Jwts.parser()
                        .verifyWith(this.secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload())
                .onErrorResume(e ->
                        Mono.error(new ChenilleChannelException("解析Token失败")));
    }

    /**
     * 获取Token声明中的用户唯一标识
     *
     * @param token JWT Token
     * @return 用户唯一标识
     */
    public <T> Mono<T> extractSubject(String token, Class<T> clazz) {
        return parseClaims(token)
                .flatMap(claims -> {
                    try {
                        T obj = chenilleJsonUtils.fromJson(claims.getSubject(), clazz);
                        return Mono.just(obj);
                    } catch (Exception e) {
                        return Mono.error(new ChenilleChannelException("从Token中获取subject失败"));
                    }
                });
    }

    /**
     * 验证Token有效性
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public Mono<Boolean> isTokenValid(String token) {
        return parseClaims(token)
                .map(claims -> claims.getExpiration().after(new Date()))
                .onErrorReturn(false);
    }

}
