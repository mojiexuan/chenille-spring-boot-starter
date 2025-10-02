package com.chenjiabao.open.chenille;

import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供 Bean 操作的帮助类
 * 1. 从 Spring 容器获取 Bean
 * 2. 非 Spring 环境下，使用懒加载单例模式
 */
@Component
public class ChenilleBeanHelper implements ApplicationContextAware {

    private static final Map<Class<?>, Object> NON_SPRING_CACHE = new ConcurrentHashMap<>();
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        ChenilleBeanHelper.applicationContext = applicationContext;
    }

    /**
     * 获取 Bean
     *
     * @param clazz Bean 类
     * @param <T>   类型
     * @return Spring Bean 或 非 Spring 懒加载单例
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        // Spring 容器存在，直接从容器拿
        if (applicationContext != null && applicationContext.containsBeanDefinition(clazz.getName())) {
            return applicationContext.getBean(clazz);
        }

        // 非 Spring 环境或容器未注册，使用缓存 + 懒加载
        return (T) NON_SPRING_CACHE.computeIfAbsent(clazz, key -> {
            try {
                return key.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new ChenilleChannelException("BeanHelper 无法创建实例: " + clazz, e);
            }
        });
    }

    /**
     * 手动注册实例（可选）
     *
     * @param clazz    类
     * @param instance 实例
     * @param <T>      类型
     */
    public static <T> void register(Class<T> clazz, T instance) {
        NON_SPRING_CACHE.put(clazz, instance);
    }
}
