package com.chenjiabao.open.chenille.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Redis序列化
 * @author ChenJiaBao
 */
public class JsonRedisSerializer<T> implements RedisSerializer<T> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<T> type;

    public JsonRedisSerializer(Class<T> type) {
        super();
        this.type = type;
    }

    @Override
    public byte[] serialize(T value) throws SerializationException {
        try {
            // 将对象序列化为字节数组
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new SerializationException("序列化对象发生错误->", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            // 处理反序列化为空的情况
            return null;
        }
        try {
            // 将字节数组反序列化为指定类型的对象
            return objectMapper.readValue(bytes, type);
        } catch (Exception e) {
            throw new SerializationException("反序列化发生错误->", e);
        }
    }
}
