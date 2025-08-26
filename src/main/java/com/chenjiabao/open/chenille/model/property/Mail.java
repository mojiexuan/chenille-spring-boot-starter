package com.chenjiabao.open.chenille.model.property;

import lombok.*;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mail {
    private boolean enabled = true;
    private String host;
    private Integer port = 456;
    private Boolean ssl = false;
    private Boolean auth = false;
    private String username;
    private String password;
    private String protocol = "smtp";
    @NestedConfigurationProperty
    private MailTemplate template;
}
