package com.chenjiabao.open.chenille.core;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 数字工具类
 * @author ChenJiaBao
 */
public class ChenilleNumberUtils {

    /**
     * 比较两个数字是否相等
     * @param number1 数字1
     * @param number2 数字2
     * @return 是否相等
     */
    public boolean isEqual(final Number number1,final Number number2) {
        if(number1 instanceof BigDecimal && number2 instanceof BigDecimal){
            return isEqual((BigDecimal)number1, (BigDecimal)number2);
        }
        return Objects.equals(number1, number2);
    }

    /**
     * 比较两个BigDecimal数字是否相等
     * @param number1 数字1
     * @param number2 数字2
     * @return 是否相等
     */
    public boolean isEqual(BigDecimal number1, BigDecimal number2) {
        if(Objects.equals(number1, number2)){
            return true;
        }
        if(number1 == null || number2 == null){
            return false;
        }
        return 0 == number1.compareTo(number2);
    }

}
