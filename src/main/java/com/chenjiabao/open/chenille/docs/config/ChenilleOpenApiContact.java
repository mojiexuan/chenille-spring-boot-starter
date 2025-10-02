package com.chenjiabao.open.chenille.docs.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleOpenApiContact {
    // 联系人名称
    private String name;
    // 联系人 URL
    private String url;
    // 联系人邮箱
    private String email;
    // 扩展属性
    private Map<String, Object> extensions;

    /**
     * 设置联系人名称
     */
    public ChenilleOpenApiContact setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 设置联系人 URL
     */
    public ChenilleOpenApiContact setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * 设置联系人邮箱
     */
    public ChenilleOpenApiContact setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * 添加扩展属性
     */
    public ChenilleOpenApiContact addExtensions(String key, Object value){
        if(this.extensions == null){
            this.extensions = new HashMap<>();
        }
        this.extensions.put(key, value);
        return this;
    }
}
