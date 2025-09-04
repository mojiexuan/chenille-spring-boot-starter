package com.chenjiabao.open.chenille.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChenilleWeChatPayV3 {
    private String mchId;
    private String mchSerialNo;
    private String privateKey;
    private String apiV3Key;
    private String certPath;
}
