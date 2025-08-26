package com.chenjiabao.open.chenille.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 验证状态
 * @author ChenJiaBao
 */
@Setter
@Getter
public class AuthStatus {
    /**
     * 是否认证成功
     */
    private boolean auth = true;
    private final Map<String,Object> attributes = new HashMap<>();

    /**
     * 添加请求属性
     * @param key 属性键
     * @param value 属性值
     */
    public void addAttribute(String key,Object value){
        attributes.put(key,value);
    }
}
