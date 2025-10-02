package com.chenjiabao.open.chenille.docs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口组
 * @author ChenJiaBao
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleApiGroup {
    // 接口组名
    private String name;
    // 接口组描述
    private String description = "";
    // 接口组路径
    private String path = "/";
    // 接口组下的接口
    private List<ChenilleApiDefinition> apis = new ArrayList<>();
    // 接口组版本
    private String version = "1";

    /**
     * 添加接口
     * @param api 接口定义
     */
    public void addApi(ChenilleApiDefinition api) {
        apis.add(api);
    }

    @Override
    public String toString() {
        return "ChenilleApiGroup{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", path='" + path + '\'' +
                ", apis=" + apis +
                ", version='" + version + '\'' +
                '}';
    }
}
