package com.chenjiabao.open.chenille.exception;

import com.chenjiabao.open.chenille.enums.ResponseCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelException extends Exception {

    private ResponseCode code = ResponseCode.SYSTEM_ERROR;

    public ChannelException(String message) {
        super(message);
    }

    public ChannelException(ResponseCode code, String message) {
        super(message);
        this.code = code;
    }

}
