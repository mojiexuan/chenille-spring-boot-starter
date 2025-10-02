package com.chenjiabao.open.chenille.docs.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleOpenApiLicense {
    // 许可证名称
    private String name;
    // 许可证 URL
    private String url;
    // 许可证标识符，例如 Apache-2.0
    private String identifier;
    // 扩展属性
    private Map<String, Object> extensions;

    /**
     * 设置许可证名称
     */
    public ChenilleOpenApiLicense setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 设置许可证 URL
     */
    public ChenilleOpenApiLicense setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * 设置许可证标识符
     */
    public ChenilleOpenApiLicense setIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    /**
     * 添加扩展属性
     */
    public ChenilleOpenApiLicense addExtensions(String key, Object value){
        if(this.extensions == null){
            this.extensions = new HashMap<>();
        }
        this.extensions.put(key, value);
        return this;
    }
}
