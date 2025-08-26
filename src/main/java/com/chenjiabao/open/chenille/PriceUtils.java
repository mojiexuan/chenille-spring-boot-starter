package com.chenjiabao.open.chenille;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * 价格工具类
 * @author ChenJiaBao
 */
public class PriceUtils {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * 元转分
     * @param yuan 元
     * @return 分
     */
    public Long yuanToFen(BigDecimal yuan){
        if(yuan == null || yuan.compareTo(BigDecimal.ZERO) <= 0){
            return 0L;
        }
        return yuan.multiply(HUNDRED).longValue();
    }

    /**
     * 分转元
     * @param fen 分
     * @return 元
     */
    public BigDecimal fenToYuan(Long fen){
        if(fen == null || fen <= 0){
            return BigDecimal.ZERO;
        }
        return new BigDecimal(fen).divide(HUNDRED,SCALE, RoundingMode.DOWN);
    }

    /* ========== 格式化 ========== */

    /**
     * 格式化元
     * @param yuan 元
     * @return 格式化后的元
     */
    public String formatYuan(BigDecimal yuan) {
        if (yuan == null) {
            return "￥0.00";
        }
        return NumberFormat.getCurrencyInstance(Locale.CHINA)
                .format(yuan.setScale(SCALE, ROUNDING_MODE));
    }

    /**
     * 格式化分
     * @param fen 分
     * @return 格式化后的分
     */
    public String formatFen(Long fen) {
        return formatYuan(fenToYuan(fen));
    }

}
