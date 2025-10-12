package com.chenjiabao.open.chenille.core;

import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.model.property.ChenilleJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;

/**
 * jwt工具（支持东8区时间）
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
        if(chenilleStringUtils.isEmpty(chenilleJwt.getSecret())){
            secretKey = Jwts.SIG.HS256.key().build();
        }else {
            this.setJwtSecret(chenilleJwt.getSecret());
        }
    }

    /**
     * 设置秘钥（只能设置一次）
     * @param jwtSecret 新的秘钥（建议使用Base64编码的32位以上字符串）
     */
    public synchronized void setJwtSecret(String jwtSecret) {
        if(secretKey == null){
            try {
                // 解码Base64字符串
                byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);

                // 检查密钥的字节长度是否至少为 32 字节（256位）
                if (decodedKey.length < 32) {
                    throw new IllegalArgumentException("JWT 密钥的长度不足，必须至少为32字节（256位）。");
                }
                secretKey = new SecretKeySpec(decodedKey,"HmacSHA256");
            } catch (Exception e) {
                log.error("配置的 JWT 密钥应该确保是有效的 Base64 编码字符串",e);
            }
        }
    }

    /**
     * 生成Token（东8区时间）
     * @param subject 荷载
     * @return 生成的JWT Token
     */
    public String createToken(Object subject) {
        try {
            String subjectJson = chenilleJsonUtils.toJson(subject);

            // 计算东8区时间
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT+8"));
            Date issuedAt = Date.from(now.toInstant());
            Date expiration = Date.from(now.plusSeconds(chenilleJwt.getExpires()).toInstant());

            // 生成 Token
            return Jwts.builder()
                    .subject(subjectJson)
                    .issuedAt(issuedAt)
                    .expiration(expiration)
                    .signWith(this.secretKey, Jwts.SIG.HS256)
                    .compact();
        } catch (Exception e) {
            log.error("subject转JSON字符串失败 -> ", e);
            throw new RuntimeException("创建Token失败 -> ", e);
        }
    }

    /**
     * 解析Token
     * @param token JWT Token
     * @return 声明内容
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(this.secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取Token声明中的用户唯一标识
     * @param token JWT Token
     * @return 用户唯一标识
     */
    public <T> T getSubject(String token, Class<T> clazz) {
        try {
            // 解析 token 并获取 subject 字段
            String subjectJson = parseToken(token).getSubject();

            // 将 JSON 字符串反序列化为对象
            return chenilleJsonUtils.fromJson(subjectJson, clazz);
        } catch (Exception e) {
            log.error("从Token中获取subject失败 -> ", e);
            throw new RuntimeException("从Token中获取subject失败 -> ", e);
        }
    }

    /**
     * 验证Token有效性
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

}
