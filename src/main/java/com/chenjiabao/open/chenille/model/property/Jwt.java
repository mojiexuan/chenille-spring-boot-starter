package com.chenjiabao.open.chenille.model.property;

import lombok.*;

/**
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jwt {
    private boolean enabled = true;
    private String secret;
    private Integer expires = 7200;
}
