package com.chenjiabao.open.chenille.docs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 接口定义
 * @author ChenJiaBao
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleApiDefinition {
    // 接口名称
    private String summary;
    // 接口编码
    private String urlCode;
    // 接口描述
    private String description;
    // 接口路径
    private String path;
    // 请求方式
    private String method;
    // 查询参数
    private List<ChenilleApiParameter> queryParam = new ArrayList<>();
    // 路径参数
    private List<ChenilleApiParameter> pathParam = new ArrayList<>();
    // 请求体
    private List<ChenilleApiParameter> bodyParam = new ArrayList<>();
    // 是否弃用
    private boolean deprecated = false;
    // 接口版本
    private String version = "1";

    public void setMethod(String method) {
        this.method = method.toUpperCase();
    }

    public void setSummary(String summary) {
        this.summary = summary;
        this.urlCode = URLEncoder.encode(summary, StandardCharsets.UTF_8);
    }

    /**
     * 添加查询参数
     * @param param 请求参数
     */
    public void addQueryParam(ChenilleApiParameter param){
        this.queryParam.add(param);
    }

    public void addPathParam(ChenilleApiParameter param){
        this.pathParam.add(param);
    }

    public void addBodyParam(ChenilleApiParameter parm){
        this.bodyParam.add(parm);
    }

    @Override
    public String toString() {
        return "ChenilleApiDefinition{" +
                "summary='" + summary + '\'' +
                ", urlCode='" + urlCode + '\'' +
                ", description='" + description + '\'' +
                ", path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", queryParam=" + queryParam.toString() +
                ", pathParam=" + pathParam.toString() +
                ", bodyParam=" + bodyParam.toString() +
                ", deprecated=" + deprecated +
                ", version='" + version + '\'' +
                '}';
    }
}
