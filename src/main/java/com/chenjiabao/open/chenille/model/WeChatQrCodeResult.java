package com.chenjiabao.open.chenille.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author ChenJiaBao
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeChatQrCodeResult {
    private byte[] qrCodeImage;
    private String errorMsg = null;
}
