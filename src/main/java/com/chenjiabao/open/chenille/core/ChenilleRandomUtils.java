package com.chenjiabao.open.chenille.core;

import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import lombok.NonNull;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机数工具类
 */
public class ChenilleRandomUtils {

    public static final String BASE_NUMBER = "0123456789";
    public static final String BASE_CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    public static final String BASE_CHAR_UPPER = BASE_CHAR_LOWER.toUpperCase();

    /**
     * 获取随机数对象
     *
     * @return 随机数对象
     */
    public ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 获取随机数对象
     *
     * @param isSecure 是否安全随机数
     * @return 随机数对象
     */
    public Random getRandom(boolean isSecure) {
        return isSecure ? getSecureRandom() : getRandom();
    }

    /**
     * 获取安全随机数对象
     *
     * @param salt 盐值,随机数种子
     * @return 安全随机数对象
     */
    public SecureRandom getSecureRandom(final byte[] salt) {
        return (salt != null) ? new SecureRandom(salt) : new SecureRandom();
    }

     /**
     * 获取安全随机数对象
     *
     * @return 安全随机数对象
     */
    public SecureRandom getSecureRandom() {
        return getSecureRandom(null);
    }

    /**
     * 生成随机布尔值
     *
     * @return 随机布尔值
     */
    public boolean randomBoolean() {
        return randomInt(2) == 0;
    }

    /**
     * 生成随机字节数组
     *
     * @param length 数组长度
     * @return 随机字节数组
     */
    public byte[] randomBytes(final int length) {
        byte[] bytes = new byte[length];
        getRandom().nextBytes(bytes);
        return bytes;
    }

    /**
     * 生成随机整数
     *
     * @return 随机整数
     */
    public int randomInt(){
        return getRandom().nextInt();
    }

    /**
     * 生成随机整数
     *
     * @param bound 边界值,生成的随机整数在[0,bound)范围内
     * @return 随机整数
     */
    public int randomInt(final int bound) {
        return getRandom().nextInt(bound);
    }

    /**
     * 生成随机整数
     *
     * @param min 最小值,包含
     * @param max 最大值,不包含
     * @return 随机整数
     */
    public int randomInt(final int min, final int max) {
        if(min == max){
            return min;
        }
        return randomInt(min, max, true, false);
    }

    /**
     * 生成随机整数
     *
     * @param min 最小值,包含
     * @param max 最大值,不包含
     * @param includeMin 是否包含最小值
     * @param includeMax 是否包含最大值
     * @return 随机整数
     */
    public int randomInt(int min, int max,final boolean includeMin, final boolean includeMax) {
        if (min >= max) {
            throw ChenilleChannelException.builder()
                    .userMessage("最小值必须小于最大值")
                    .logMessage("最小值必须小于最大值")
                    .build();
        }
        if(!includeMin){
            min++;
        }
        if(includeMax){
            max++;
        }
        return getRandom().nextInt(min, max);
    }

     /**
     * 生成随机整数数组
     *
     * @param length 数组长度
     * @return 随机整数数组
     */
    public int[] randomInts(final int length) {
        final int[] ints = new int[length];
        for (int i = 0; i < length; i++) {
            ints[i] = randomInt();
        }
        return ints;
    }

    /**
     * 生成随机长整数
     *
     * @return 随机长整数
     */
    public long randomLong() {
        return getRandom().nextLong();
    }

    /**
     * 生成随机长整数
     *
     * @param bound 边界值,生成的随机长整数在[0,bound)范围内
     * @return 随机长整数
     */
    public long randomLong(final long bound) {
        return getRandom().nextLong(bound);
    }

    /**
     * 生成随机长整数
     *
     * @param min 最小值,包含
     * @param max 最大值,不包含
     * @return 随机长整数
     */
    public long randomLong(final long min, final long max) {
        if(min == max){
            return min;
        }
        return randomLong(min, max, true, false);
    }

    /**
     * 生成随机长整数
     *
     * @param min 最小值,包含
     * @param max 最大值,不包含
     * @param includeMin 是否包含最小值
     * @param includeMax 是否包含最大值
     * @return 随机长整数
     */
    public long randomLong(long min, long max, final boolean includeMin, final boolean includeMax) {
        if (min >= max) {
            throw ChenilleChannelException.builder()
                    .userMessage("最小值必须小于最大值")
                    .logMessage("最小值必须小于最大值")
                    .build();
        }
        if(!includeMin){
            min++;
        }
        if(includeMax){
            max++;
        }
        return getRandom().nextLong(min, max);
    }

     /**
     * 生成随机浮点数
     *
     * @return 随机浮点数
     */
    public float randomFloat() {
        return getRandom().nextFloat();
    }

    /**
     * 生成随机浮点数
     *
     * @param bound 边界值,生成的随机浮点数在[0,bound)范围内
     * @return 随机浮点数
     */
    public float randomFloat(final float bound) {
        return getRandom().nextFloat(bound);
    }

     /**
     * 生成随机浮点数
     *
     * @param min 最小值,包含
     * @param max 最大值,不包含
     * @return 随机浮点数
     */
    public float randomFloat(final float min, final float max) {
        if(min == max){
            return min;
        }
        return randomFloat(min, max, true, false);
    }

    /**
     * 生成随机浮点数
     *
     * @param min 最小值,包含
     * @param max 最大值,不包含
     * @param includeMin 是否包含最小值
     * @param includeMax 是否包含最大值
     * @return 随机浮点数
     */
    public float randomFloat(float min, float max, final boolean includeMin, final boolean includeMax) {
        if (min >= max) {
            throw ChenilleChannelException.builder()
                    .userMessage("最小值必须小于最大值")
                    .logMessage("最小值必须小于最大值")
                    .build();
        }
        if(!includeMin){
            min += 0.000001f;
        }
        if(includeMax){
            max -= 0.000001f;
        }
        return getRandom().nextFloat(min, max);
    }

    /**
     * 生成随机双精度浮点数
     *
     * @return 随机双精度浮点数
     */
    public double randomDouble() {
        return getRandom().nextDouble();
    }

    /**
     * 生成随机双精度浮点数
     *
     * @param bound 边界值,生成的随机双精度浮点数在[0,bound)范围内
     * @return 随机双精度浮点数
     */
    public double randomDouble(final double bound) {
        return getRandom().nextDouble(bound);
    }

     /**
     * 生成随机双精度浮点数
     *
     * @param min 最小值,包含
     * @param max 最大值,不包含
     * @return 随机双精度浮点数
     */
    public double randomDouble(final double min, final double max) {
        if(min == max){
            return min;
        }
        return randomDouble(min, max, true, false);
    }

    /**
     * 生成随机双精度浮点数
     *
     * @param min 最小值,包含
     * @param max 最大值,不包含
     * @param includeMin 是否包含最小值
     * @param includeMax 是否包含最大值
     * @return 随机双精度浮点数
     */
    public double randomDouble(double min, double max, final boolean includeMin, final boolean includeMax) {
        if (min >= max) {
            throw ChenilleChannelException.builder()
                    .userMessage("最小值必须小于最大值")
                    .logMessage("最小值必须小于最大值")
                    .build();
        }
        if(!includeMin){
            min += 0.000001d;
        }
        if(includeMax){
            max -= 0.000001d;
        }
        return getRandom().nextDouble(min, max);
    }

    /**
     * 生成随机字符串,使用默认的基础字符串{@link #BASE_NUMBER}{@link #BASE_CHAR_LOWER}{@link #BASE_CHAR_UPPER}
     *
     * @param length 字符串长度
     * @return 随机字符串
     */
    public String randomString(final int length) {
        return randomString(BASE_NUMBER + BASE_CHAR_LOWER + BASE_CHAR_UPPER, length);
    }

    /**
     * 生成随机字符串
     *
     * @param baseString 基础字符串,用于生成随机字符串
     * @param length 字符串长度
     * @return 随机字符串
     */
    public String randomString(@NonNull final String baseString, final int length) {
        final int baseLength = baseString.length();
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(baseString.charAt(randomInt(baseLength)));
        }
        return sb.toString();
    }

    /**
     * 生成随机数字字符串,使用默认的基础字符串{@link #BASE_NUMBER}
     *
     * @param length 字符串长度
     * @return 随机数字字符串
     */
    public String randomNumberString(final int length) {
        return randomString(BASE_NUMBER, length);
    }

    /**
     * 生成随机字母字符串,使用默认的基础字符串{@link #BASE_CHAR_LOWER}{@link #BASE_CHAR_UPPER}
     *
     * @param length 字符串长度
     * @return 随机字母字符串
     */
    public String randomLetterString(final int length) {
        return randomString(BASE_CHAR_LOWER + BASE_CHAR_UPPER, length);
    }

    /**
     * 生成随机小写字母字符串,使用默认的基础字符串{@link #BASE_CHAR_LOWER}
     *
     * @param length 字符串长度
     * @return 随机小写字母字符串
     */
    public String randomLowerLetterString(final int length) {
        return randomString(BASE_CHAR_LOWER, length);
    }

    /**
     * 生成随机大写字母字符串,使用默认的基础字符串{@link #BASE_CHAR_UPPER}
     *
     * @param length 字符串长度
     * @return 随机大写字母字符串
     */
    public String randomUpperLetterString(final int length) {
        return randomString(BASE_CHAR_UPPER, length);
    }

     /**
     * 生成随机数字小写字母字符串,使用默认的基础字符串{@link #BASE_NUMBER}{@link #BASE_CHAR_LOWER}
     *
     * @param length 字符串长度
     * @return 随机数字小写字母字符串
     */
    public String randomNumberLowerString(final int length) {
        return randomString(BASE_NUMBER + BASE_CHAR_LOWER, length);
    }

    /**
     * 生成随机数字大写字母字符串,使用默认的基础字符串{@link #BASE_NUMBER}{@link #BASE_CHAR_UPPER}
     *
     * @param length 字符串长度
     * @return 随机数字大写字母字符串
     */
    public String randomNumberUpperString(final int length) {
        return randomString(BASE_NUMBER + BASE_CHAR_UPPER, length);
    }

    /**
     * 生成随机中文字符
     *
     * @return 随机中文字符
     */
    public char randomChineseChar() {
        return (char) randomInt(0x4E00, 0x9FA5, true, true);
    }

     /**
     * 生成随机中文字符串
     *
     * @param length 字符串长度
     * @return 随机中文字符串
     */
    public String randomChineseString(final int length) {
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(randomChineseChar());
        }
        return sb.toString();
    }

    /**
     * 生成随机字符,使用默认的基础字符串{@link #BASE_NUMBER}{@link #BASE_CHAR_LOWER}{@link #BASE_CHAR_UPPER}
     *
     * @return 随机字符
     */
    public char randomChar() {
        return randomChar(BASE_NUMBER + BASE_CHAR_LOWER + BASE_CHAR_UPPER);
    }

     /**
     * 生成随机字符
     *
     * @param baseString 基础字符串,用于生成随机字符
     * @return 随机字符
     */
    public char randomChar(@NonNull final String baseString) {
        final int baseLength = baseString.length();
        return baseString.charAt(randomInt(baseLength));
    }

    /**
     * 生成随机数字字符
     *
     * @return 随机数字字符
     */
    public char randomNumber(){
        return randomChar(BASE_NUMBER);
    }

     /**
     * 生成随机字母字符
     *
     * @return 随机字母字符
     */
    public char randomLetter(){
        return randomChar(BASE_CHAR_LOWER + BASE_CHAR_UPPER);
    }

     /**
     * 生成随机小写字母字符
     *
     * @return 随机小写字母字符
     */
    public char randomLowerLetter(){
        return randomChar(BASE_CHAR_LOWER);
    }

     /**
     * 生成随机大写字母字符
     *
     * @return 随机大写字母字符
     */
    public char randomUpperLetter(){
        return randomChar(BASE_CHAR_UPPER);
    }



}
