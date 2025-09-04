package com.chenjiabao.open.chenille.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * sha
 * @author ChenJiaBao
 */
public class ChenilleHashUtils {

    // sha版本
    private static final String HASH_ALGORITHM = "SHA-256";
    // 默认胡椒，生产环境通过启动变量引入
    private static String HASH_PEPPER = "8wFhA5VR9DbeOnmpcHq+1wFP8nL0fgFqYSsNV7FjLsI=";

    /**
     * 设置胡椒，仅可更新一次
     * @param hashPepper 胡椒值
     */
    public void setHashPepper(String hashPepper) {
        if("8wFhA5VR9DbeOnmpcHq+1wFP8nL0fgFqYSsNV7FjLsI=".equals(HASH_PEPPER)){
            HASH_PEPPER = hashPepper;
        }
    }

    /**
     * 获取随机盐值base64
     */
    public String getRandomSalt(){
        // 创建一个 SecureRandom 实例
        SecureRandom secureRandom = new SecureRandom();
        // 生成 16 字节的盐值
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        // 将盐值转换为 Base64 编码的字符串形式
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 字符串转hash值
     * @param str 需要转换的字符串
     * @return hash摘要转base64
     */
    public String stringToHash256(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(str.getBytes(StandardCharsets.UTF_8));
            // 将哈希值转换为 Base64 编码的字符串
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * 字符串转hash摘要，加上胡椒和盐值
     * @param str 需要加密的字符串
     * @param salt 盐值
     * @return hash摘要转base64
     */
    public String stringToHash256WithSaltAndPepper(String str,String salt){
        return stringToHash256(str + salt + HASH_PEPPER);
    }

}
