package com.chenjiabao.open.chenille.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 验证状态
 * @author ChenJiaBao
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChenilleAuthStatus {

    /**
     * 是否认证成功
     */
    private boolean auth = true;
    /**
     * 认证成功时添加的属性，在后续的处理中可以通过该属性来获取相关信息
     */
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
