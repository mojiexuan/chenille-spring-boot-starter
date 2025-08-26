package com.chenjiabao.open.chenille;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.Random;

/**
 * 字符串工具类
 * @author ChenJiaBao
 */
public class StringUtils {

    /**
     * 字符串转base64
     * @param str 需要转换的字符串
     * @return base64
     */
    public String stringToBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
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
     */
    public String generateSureString(String characters, int num) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < num; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    /**
     * 生成随机指定范围字符串 默认范围为"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
     *
     * @param num 长度
     */
    public String generateSureString(int num) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        return generateSureString(characters,num);
    }

    /**
     * 生成随机长度纯数字字符串
     *
     * @param length 长度
     */
    public String generateRandomNumberString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // 生成0-9之间的随机数字
            sb.append(random.nextInt(10));
        }
        return sb.toString();
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
