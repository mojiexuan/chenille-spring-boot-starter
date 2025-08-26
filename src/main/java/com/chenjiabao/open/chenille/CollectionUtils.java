package com.chenjiabao.open.chenille;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 集合工具
 * @author ChenJiaBao
 */
public class CollectionUtils {

    /**
     * 判断集合是否为空
     * @param collection 集合
     * @return 是否为空
     */
    public boolean isEmpty(Collection<?> collection){
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合是否非空
     * @param collection 集合
     * @return 是否非空
     */
    public boolean isNotEmpty(Collection<?> collection){
        return !isEmpty(collection);
    }

    /**
     * 合并多个集合
     */
    @SafeVarargs
    public final <T> List<T> merge(Collection<T>... collections) {
        List<T> result = new ArrayList<>();
        if (collections != null) {
            for (Collection<T> c : collections) {
                if (isNotEmpty(c)) {
                    result.addAll(c);
                }
            }
        }
        return result;
    }

    /**
     * 过滤null元素
     */
    public <T> List<T> filterNull(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
