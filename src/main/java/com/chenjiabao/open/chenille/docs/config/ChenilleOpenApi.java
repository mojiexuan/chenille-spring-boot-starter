package com.chenjiabao.open.chenille.docs.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 接口文档 配置类
 * @author 陈佳宝
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleOpenApi {
    // 接口文档标题
    private String title;
    // 接口文档描述
    private String description;
    // 接口文档服务条款 URL
    private String termsOfService;
    // 接口文档版本
    private String version;
    // 接口文档联系人
    private ChenilleOpenApiContact contact;
    // 接口文档协议
    private ChenilleOpenApiLicense license;
    // 接口文档扩展属性
    private Map<String, Object> extensions;
    // 接口文档服务列表
    private List<ChenilleOpenApiServer> servers;

    /**
     * 设置接口文档标题
     */
    public ChenilleOpenApi setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * 设置接口文档描述
     */
    public ChenilleOpenApi setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * 设置接口文档服务条款 URL
     */
    public ChenilleOpenApi setTermsOfService(String termsOfService) {
        this.termsOfService = termsOfService;
        return this;
    }

    /**
     * 设置接口文档版本
     */
    public ChenilleOpenApi setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * 设置接口文档联系人
     */
    public ChenilleOpenApi setContact(ChenilleOpenApiContact contact) {
        this.contact = contact;
        return this;
    }

    /**
     * 设置接口文档协议
     */
    public ChenilleOpenApi setLicense(ChenilleOpenApiLicense license) {
        this.license = license;
        return this;
    }

    /**
     * 添加接口服务Base URL
     */
    public ChenilleOpenApi addServer(ChenilleOpenApiServer server){
        if(this.servers == null){
            this.servers = new ArrayList<>();
        }
        this.servers.add(server);
        return this;
    }

    /**
     * 添加接口文档扩展属性
     */
    public ChenilleOpenApi addExtensions(String key, Object value){
        if(this.extensions == null){
            this.extensions = new HashMap<>();
        }
        this.extensions.put(key, value);
        return this;
    }

}
