package com.chenjiabao.open.chenille.core;

import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

/**
 * 对象工具类
 *
 * @author ChenJiaBao
 */
@Slf4j
public record ChenilleObjectUtils(ChenilleNumberUtils chenilleNumberUtils) {

    /**
     * 检查对象是否为null, 为null则抛出异常
     *
     * @param obj     对象
     * @param message 异常信息
     * @param <T>     对象类型
     */
    public <T> void requireNonNull(T obj, String message) {
        if (obj == null) {
            log.error(message);
            throw  ChenilleChannelException.builder()
                    .userMessage(message)
                    .build();
        }
    }

    /**
     * 比较两个对象是否相等
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否相等
     */
    public boolean isEqual(Object obj1, Object obj2) {
        if (obj1 instanceof Number && obj2 instanceof Number) {
            return chenilleNumberUtils.isEqual((Number) obj1, (Number) obj2);
        }
        return Objects.equals(obj1, obj2);
    }

    /**
     * 比较两个对象是否不相等
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否不相等
     */
    public boolean isNotEqual(Object obj1, Object obj2) {
        return !isEqual(obj1, obj2);
    }

    /**
     * 获取对象的长度，字符串获取length，数组获取length，集合获取size，可迭代对象遍历获取长度，其他返回-1
     *
     * @param obj 对象
     * @return 长度 -1 表示无法测量长度, 0 表示长度为0或空
     */
    public int length(Object obj) {
        switch (obj) {
            case null -> {
                return 0;
            }
            case CharSequence charSequence -> {
                return charSequence.length();
            }
            case Collection<?> collection -> {
                return collection.size();
            }
            case Map<?, ?> map -> {
                return map.size();
            }
            case Iterable<?> iterable -> {
                return (int) StreamSupport.stream(iterable.spliterator(), false).count();
            }
            case Iterator<?> iterator -> {
                List<Object> temp = new ArrayList<>();
                iterator.forEachRemaining(temp::add);
                return temp.size();
            }
            case Enumeration<?> enumeration -> {
                List<Object> temp = new ArrayList<>();
                while (enumeration.hasMoreElements()) {
                    temp.add(enumeration.nextElement());
                }
                return temp.size();
            }
            default -> {
            }
        }
        if (obj.getClass().isArray()) {
            return Array.getLength(obj);
        }
        return -1;
    }

    /**
     * 如果对象为 {@code null}，则返回默认值
     *
     * @param obj          对象
     * @param defaultValue 默认值
     * @return 对象或默认值
     */
    public <T> T defaultIfNull(final T obj, final T defaultValue) {
        return obj == null ? defaultValue : obj;
    }

    /**
     * 如果对象为 {@code null}，则返回默认值
     *
     * @param obj                  对象
     * @param defaultValueSupplier 默认值供应器
     * @return 对象或默认值
     */
    public <T> T defaultIfNull(final T obj, final Supplier<? extends T> defaultValueSupplier) {
        return obj == null ? defaultValueSupplier.get() : obj;
    }

    /**
     * 如果对象为 {@code null}，则返回默认值
     *
     * @param obj                  对象
     * @param defaultValueSupplier 默认值供应器
     * @return 对象或默认值
     */
    public <T> T defaultIfNull(final T obj, final Function<T, ? extends T> defaultValueSupplier) {
        return obj == null ? defaultValueSupplier.apply(null) : obj;
    }

}
