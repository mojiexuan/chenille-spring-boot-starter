package com.chenjiabao.open.chenille.core;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 字符串工具类
 * @author ChenJiaBao
 */
@Slf4j
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

    /**
     * 文本模板格式化
     * @param template 模板，使用 {} 作为占位符
     * @param params 参数
     * @return 格式化后的消息
     */
    public String format(String template, Object... params){
        if(isEmpty(template) || params == null){
            return template;
        }
        StringBuilder result = new StringBuilder();
        int i = 0;
        int templateLength = template.length();

        for (int L = 0; L < templateLength; ++L) {
            int j = template.indexOf("{}", i);

            if (j == -1) {
                if (i == 0) {
                    return template;
                }

                result.append(template, i, template.length());
                return result.toString();
            }

            if (isEscapedDelimiter(template, j)) {
                if (!isDoubleEscaped(template, j)) {
                    --L;
                    result.append(template, i, j - 1);
                    result.append('{');
                    i = j + 1;
                } else {
                    result.append(template, i, j - 1);
                    deeplyAppendParameter(result, params[L], new HashMap<>());
                    i = j + 2;
                }
            } else {
                result.append(template, i, j);
                deeplyAppendParameter(result, params[L], new HashMap<>());
                i = j + 2;
            }
        }

        return result.toString();
    }

    /**
     * 判断是否为转义符
     *
     * @param messagePattern 消息模式
     * @param delimiterStartIndex 分隔符开始索引
     * @return 是否为转义符
     */
    public boolean isEscapedDelimiter(String messagePattern, int delimiterStartIndex) {
        if (delimiterStartIndex == 0) {
            return false;
        } else {
            char potentialEscape = messagePattern.charAt(delimiterStartIndex - 1);
            return potentialEscape == '\\';
        }
    }

    /**
     * 判断是否为双转义符
     *
     * @param messagePattern 消息模式
     * @param delimiterStartIndex 分隔符开始索引
     * @return 是否为双转义符
     */
    public boolean isDoubleEscaped(String messagePattern, int delimiterStartIndex) {
        return delimiterStartIndex >= 2 && messagePattern.charAt(delimiterStartIndex - 2) == '\\';
    }

    /**
     * 递归追加参数到 StringBuilder，处理循环引用
     *
     * @param stringBuilder StringBuilder
     * @param o             参数对象
     * @param seenMap       已处理对象映射，用于处理循环引用
     */
    public void deeplyAppendParameter(StringBuilder stringBuilder, Object o, Map<Object[], Object> seenMap) {
        if (o == null) {
            stringBuilder.append("null");
        } else {
            if (!o.getClass().isArray()) {
                safeObjectAppend(stringBuilder, o);
            } else if (o instanceof boolean[]) {
                booleanArrayAppend(stringBuilder, (boolean[])o);
            } else if (o instanceof byte[]) {
                byteArrayAppend(stringBuilder, (byte[])o);
            } else if (o instanceof char[]) {
                charArrayAppend(stringBuilder, (char[])o);
            } else if (o instanceof short[]) {
                shortArrayAppend(stringBuilder, (short[])o);
            } else if (o instanceof int[]) {
                intArrayAppend(stringBuilder, (int[])o);
            } else if (o instanceof long[]) {
                longArrayAppend(stringBuilder, (long[])o);
            } else if (o instanceof float[]) {
                floatArrayAppend(stringBuilder, (float[])o);
            } else if (o instanceof double[]) {
                doubleArrayAppend(stringBuilder, (double[])o);
            } else {
                objectArrayAppend(stringBuilder, (Object[]) o, seenMap);
            }

        }
    }

    /**
     * 安全追加 Object 到 StringBuilder，处理 toString() 异常
     *
     * @param stringBuilder StringBuilder
     * @param o             Object
     */
    public void safeObjectAppend(StringBuilder stringBuilder, Object o) {
        try {
            String oAsString = o.toString();
            stringBuilder.append(oAsString);
        } catch (Throwable t) {
            log.error("调用对象 toString() 失败 [{}]", o.getClass().getName(), t);
            stringBuilder.append("[FAILED toString()]");
        }

    }

        /**
         * 追加 Object 数组到 StringBuilder
         *
         * @param stringBuilder StringBuilder
         * @param a             Object 数组
         * @param seenMap       已处理对象映射，用于处理循环引用
         */
        public void objectArrayAppend(StringBuilder stringBuilder, Object[] a, Map<Object[], Object> seenMap) {
        stringBuilder.append('[');
        if (!seenMap.containsKey(a)) {
            seenMap.put(a, null);
            int len = a.length;

            for(int i = 0; i < len; ++i) {
                deeplyAppendParameter(stringBuilder, a[i], seenMap);
                if (i != len - 1) {
                    stringBuilder.append(", ");
                }
            }

            seenMap.remove(a);
        } else {
            stringBuilder.append("...");
        }

        stringBuilder.append(']');
    }

    /**
     * 追加 boolean 数组到 StringBuilder
     *
     * @param stringBuilder StringBuilder
     * @param a             boolean 数组
     */
    public void booleanArrayAppend(StringBuilder stringBuilder, boolean[] a) {
        stringBuilder.append('[');
        int len = a.length;

        for(int i = 0; i < len; ++i) {
            stringBuilder.append(a[i]);
            if (i != len - 1) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(']');
    }

    /**
     * 追加 byte 数组到 StringBuilder
     *
     * @param stringBuilder StringBuilder
     * @param a             byte 数组
     */
    public void byteArrayAppend(StringBuilder stringBuilder, byte[] a) {
        stringBuilder.append('[');
        int len = a.length;

        for(int i = 0; i < len; ++i) {
            stringBuilder.append(a[i]);
            if (i != len - 1) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(']');
    }

    /**
     * 追加 char 数组到 StringBuilder
     *
     * @param stringBuilder StringBuilder
     * @param a             char 数组
     */
    public void charArrayAppend(StringBuilder stringBuilder, char[] a) {
        stringBuilder.append('[');
        int len = a.length;

        for(int i = 0; i < len; ++i) {
            stringBuilder.append(a[i]);
            if (i != len - 1) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(']');
    }

    /**
     * 追加 short 数组到 StringBuilder
     *
     * @param stringBuilder StringBuilder
     * @param a             short 数组
     */
    public void shortArrayAppend(StringBuilder stringBuilder, short[] a) {
        stringBuilder.append('[');
        int len = a.length;

        for(int i = 0; i < len; ++i) {
            stringBuilder.append(a[i]);
            if (i != len - 1) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(']');
    }

    /**
     * 追加 int 数组到 StringBuilder
     *
     * @param stringBuilder StringBuilder
     * @param a             int 数组
     */
    public void intArrayAppend(StringBuilder stringBuilder, int[] a) {
        stringBuilder.append('[');
        int len = a.length;

        for(int i = 0; i < len; ++i) {
            stringBuilder.append(a[i]);
            if (i != len - 1) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(']');
    }

    /**
     * 追加 long 数组到 StringBuilder
     *
     * @param stringBuilder StringBuilder
     * @param a             long 数组
     */
    public void longArrayAppend(StringBuilder stringBuilder, long[] a) {
        stringBuilder.append('[');
        int len = a.length;

        for(int i = 0; i < len; ++i) {
            stringBuilder.append(a[i]);
            if (i != len - 1) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(']');
    }

    /**
     * 追加 float 数组到 StringBuilder
     *
     * @param stringBuilder StringBuilder
     * @param a             float 数组
     */
    public void floatArrayAppend(StringBuilder stringBuilder, float[] a) {
        stringBuilder.append('[');
        int len = a.length;

        for(int i = 0; i < len; ++i) {
            stringBuilder.append(a[i]);
            if (i != len - 1) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(']');
    }

    /**
     * 追加 double 数组到 StringBuilder
     *
     * @param stringBuilder StringBuilder
     * @param a             double 数组
     */
    public void doubleArrayAppend(StringBuilder stringBuilder, double[] a) {
        stringBuilder.append('[');
        int len = a.length;

        for(int i = 0; i < len; ++i) {
            stringBuilder.append(a[i]);
            if (i != len - 1) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append(']');
    }
}
