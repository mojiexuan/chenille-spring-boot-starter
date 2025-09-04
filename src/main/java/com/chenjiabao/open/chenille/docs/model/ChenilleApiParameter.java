package com.chenjiabao.open.chenille.docs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 接口参数
 * @author ChenJiaBao
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleApiParameter {
    // 参数名称
    private String name;
    // 参数描述
    private String description;
    // 参数类型
    private String type;
    // 是否必填
    private boolean required = true;
    // 参数位置
    private String in;
    // 属性
    private Map<String, ChenilleApiParameter> property = null;

    /**
     * 添加属性
     * @param key 属性名
     * @param value 值
     */
    public void addProperty(String key, ChenilleApiParameter value){
        if(this.property == null){
            property = new HashMap<>();
        }
        property.put(key, value);
    }
}
