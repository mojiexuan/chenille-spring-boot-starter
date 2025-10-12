package com.chenjiabao.open.chenille.exception;

import com.chenjiabao.open.chenille.enums.ChenilleResponseCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 流处理异常
 * <p>
 * 在服务层或控制层抛出此异常
 */
@Getter
@Setter
public class ChenilleChannelException extends RuntimeException {

    private ChenilleResponseCode code = ChenilleResponseCode.SYSTEM_ERROR;

    public ChenilleChannelException() {
        super(ChenilleResponseCode.SYSTEM_ERROR.getMessage());
    }

    public ChenilleChannelException(ChenilleResponseCode code) {
        super(ChenilleResponseCode.SYSTEM_ERROR.getMessage());
        this.code = code;
    }

    public ChenilleChannelException(String message) {
        super(message);
    }

    public ChenilleChannelException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChenilleChannelException(ChenilleResponseCode code, String message) {
        super(message);
        this.code = code;
    }

}
