package com.chenjiabao.open.chenille.exception;

/**
 * 与微信服务器请求异常
 * @author ChenJiaBao
 */
public class WeChatException extends RuntimeException {
    public WeChatException(String message) {
        super(message);
    }
}
