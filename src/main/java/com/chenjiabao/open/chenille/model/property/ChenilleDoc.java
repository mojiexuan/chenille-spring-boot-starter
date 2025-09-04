package com.chenjiabao.open.chenille.model.property;

import lombok.*;

/**
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleDoc {
    private boolean enabled = false;
    private String path = "/docs";
}
