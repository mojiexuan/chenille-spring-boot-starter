package com.chenjiabao.open.chenille.core;

import com.chenjiabao.open.chenille.ChenilleBeanHelper;
import lombok.NonNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Base64;

/**
 * 字符串工具类
 * @author ChenJiaBao
 */
public class ChenilleStringUtils {

    /**
     * 字符串转base64
     * @param str 需要转换的字符串
     * @return base64
     */
    public String stringToBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 判断字符串是否为空白
     */
    public boolean isBlank(@NonNull String string) {
        if (!isEmpty(string)) {
            for (int i = 0; i < string.length(); ++i) {
                if (!Character.isWhitespace(string.charAt(i))) {
                    return false;
                }
            }

        }
        return true;
    }

    /**
     * 判断字符串是否非空白
     */
    public boolean isNotBlank(@NonNull String string) {
        return !isBlank(string);
    }

    /**
     * 判断字符串是否为空
     */
    public boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否非空
     * @param str 字符串
     */
    public boolean isNotEmpty(String str){
        return !isEmpty(str);
    }

    /**
     * 截断字符串
     */
    public String truncate(String string, int maxLength) {
        return string.length() > maxLength ? string.substring(0, maxLength) : string;
    }

    /**
     * 判断字符串是否纯数字构成
     */
    public boolean isStringNumber(String string) {
        return string.matches("-?\\d+");
    }

    /**
     * 复制文本到剪切板
     */
    public void copyToClipboard(String string) {
        StringSelection stringSelection = new StringSelection(string);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    /**
     * 生成随机指定范围字符串
     *
     * @param characters 指定范围如"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
     * @param num        长度
     *
     * @deprecated 从0.2.2开始，建议使用 {@link ChenilleRandomUtils#randomString(String, int)}
     */
    @Deprecated(since = "0.2.2",forRemoval = true)
    public String generateSureString(String characters, int num) {
        return ChenilleBeanHelper.get(ChenilleRandomUtils.class).randomString(characters, num);
    }

    /**
     * 生成随机指定范围字符串 默认范围为"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
     *
     * @param num 长度
     *
     * @deprecated 从0.2.2开始，建议使用 {@link ChenilleRandomUtils#randomNumberUpperString(int)}
     */
    @Deprecated(since = "0.2.2",forRemoval = true)
    public String generateSureString(int num) {
        return ChenilleBeanHelper.get(ChenilleRandomUtils.class).randomNumberUpperString(num);
    }

    /**
     * 生成随机长度纯数字字符串
     *
     * @param length 长度
     *
     * @deprecated 从0.2.2开始，建议使用 {@link ChenilleRandomUtils#randomNumberString(int)}
     */
    @Deprecated(since = "0.2.2",forRemoval = true)
    public String generateRandomNumberString(int length) {
        return ChenilleBeanHelper.get(ChenilleRandomUtils.class).randomNumberString(length);
    }

    /**
     * 数量格式化，数量过大处理
     * @param num long型数量
     * @return 格式化后的字符串
     */
    public String numberFormat(long num){
        if(num < 1000){
            return Long.toString(num);
        } else if (num < 10000) {
            return (num/1000)+"千+";
        } else if (num < 100000000) {
            return (num/10000)+"万+";
        }else {
            return "1亿+";
        }
    }

    /**
     * 格式化文件大小
     * @param bytes 通过file.getSize()获取的字节数
     * @return 格式化后的字符串
     */
    public String formatFileSize(long bytes) {
        if (bytes <= 0) {
            return "0";
        }

        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = (int) (Math.log(bytes) / Math.log(1024));
        double size = bytes / Math.pow(1024, unitIndex);

        // 确保最小值 0.01（例如 10 字节 → 0.01KB）
        if (size < 0.01 && unitIndex > 0) {
            unitIndex--;
            size = bytes / Math.pow(1024, unitIndex);
        }

        DecimalFormat df = new DecimalFormat("0.##");
        // 向上取整确保最小值
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(size) + " " + units[unitIndex];
    }

    /**
     * 隐藏部分手机号
     * @param phone 手机号
     * @return 隐藏部分后的手机号
     */
    public String maskPhone(String phone){
        if(isEmpty(phone) || phone.length() < 7){
            return phone;
        }
        return phone.substring(0,3) + "****" + phone.substring(7);
    }

}
