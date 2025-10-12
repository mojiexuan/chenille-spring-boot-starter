package com.chenjiabao.open.chenille.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON 工具类（基于 Jackson）
 *
 * <p>封装常用的序列化与反序列化操作，避免重复创建 ObjectMapper。</p>
 */
@Slf4j
public class ChenilleJsonUtils {

    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    /**
     * 将对象序列化为 JSON 字符串
     *
     * @param object 要序列化的对象
     * @return 序列化后的 JSON 字符串
     */
    public String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            log.error("序列化对象为 JSON 字符串失败", e);
            return null;
        }
    }

    /**
     * 将对象序列化为 JSON 字节数组
     *
     * @param object 要序列化的对象
     * @return 序列化后的 JSON 字节数组
     */
    public byte[] toBytes(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(object);
        } catch (Exception e) {
            log.error("序列化对象为 JSON 字节数组失败", e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为对象
     *
     * @param json JSON 字符串
     * @param type 对象类型
     * @param <T>  对象类型参数
     * @return 反序列化后的对象
     */
    public <T> T fromJson(String json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (Exception e) {
            log.error("反序列化 JSON 字符串为对象失败", e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为复杂对象
     *
     * @param json          JSON 字符串
     * @param typeReference 类型引用
     * @param <T>           对象类型参数
     * @return 反序列化后的对象
     */
    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("反序列化 JSON 字符串为复杂对象失败，目标类型={}", typeReference.getType(), e);
            return null;
        }
    }

    /**
     * 将 JSON 字节数组反序列化为对象
     *
     * @param json JSON 字节数组
     * @param type 对象类型
     * @param <T>  对象类型参数
     * @return 反序列化后的对象
     */
    public <T> T fromJsonBytes(byte[] json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (Exception e) {
            log.error("反序列化 JSON 字节数组为对象失败", e);
            return null;
        }
    }

    /**
     * 将 JSON 字节数组反序列化为复杂对象
     *
     * @param json          JSON 字节数组
     * @param typeReference 类型引用
     * @param <T>           对象类型参数
     * @return 反序列化后的对象
     */
    public <T> T fromJsonBytes(byte[] json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("反序列化 JSON 字节数组为复杂对象失败，目标类型={}", typeReference.getType(), e);
            return null;
        }
    }

    /**
     * 获取 ObjectMapper 实例
     *
     * @return ObjectMapper 实例
     */
    public ObjectMapper mapper() {
        return OBJECT_MAPPER;
    }
}
