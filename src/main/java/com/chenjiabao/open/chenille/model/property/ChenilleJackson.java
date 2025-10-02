package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleJackson {
    /**
     * json 配置
     */
    @NestedConfigurationProperty
    private ChenilleJacksonJson json = new ChenilleJacksonJson();
    /**
     * xml 配置
     */
    @NestedConfigurationProperty
    private ChenilleJacksonXml xml = new ChenilleJacksonXml();
}
