package com.chenjiabao.open.chenille.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ChenilleHash
 * <p>
 * 提供安全的 SHA-256 哈希工具，支持盐（Salt）与胡椒（Pepper）。
 * <br>
 * 胡椒应由应用在启动时显式初始化。
 * </p>
 *
 * @author ChenJiaBao
 */
public class ChenilleHashUtils {

    private final AtomicReference<String> PEPPER = new AtomicReference<>();

    /**
     * 构造函数，初始化系统级胡椒值。
     * <p>仅可调用一次，重复调用将被忽略。</p>
     *
     * @param pepper 高熵随机字符串（例如从环境变量加载）
     */
    public ChenilleHashUtils(String pepper){
        if (pepper == null || pepper.isBlank()) {
            throw new IllegalArgumentException("Pepper不能为null或空白");
        }
        PEPPER.compareAndSet(null, pepper);
    }

    /**
     * 生成随机盐值（Base64 编码）。
     *
     * @return 盐值字符串
     */
    public String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 对字符串进行 SHA-256 哈希（Base64 编码）。
     *
     * @param input 输入字符串
     * @return 哈希字符串
     */
    public String hash(String input) {
        return doHash(input);
    }

    /**
     * 对字符串进行 SHA-256 哈希，并加入盐值与胡椒（Base64 编码）。
     *
     * @param input 输入字符串
     * @param salt  盐值
     * @return 哈希字符串
     */
    public String hashWithSaltAndPepper(String input, String salt) {
        String pepper = PEPPER.get();
        if (pepper == null) {
            throw new IllegalStateException("Pepper尚未初始化。首先调用initializePepper()。");
        }
        return doHash(input + salt + pepper);
    }

    /**
     * 内部通用哈希逻辑。
     */
    private String doHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256算法不可用", e);
        }
    }

}
