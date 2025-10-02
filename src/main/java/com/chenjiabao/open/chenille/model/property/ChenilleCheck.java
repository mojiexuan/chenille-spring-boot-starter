package com.chenjiabao.open.chenille.model.property;

import lombok.*;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleCheck {
    private Boolean enabled = true;
    /**
     * 密码校验
     */
    @NestedConfigurationProperty
    private ChenilleCheckPassword password = new ChenilleCheckPassword();
}
