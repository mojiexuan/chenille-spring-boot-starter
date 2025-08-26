package com.chenjiabao.open.chenille;

import lombok.Getter;
import lombok.Setter;

/**
 * HTTP请求结果
 * @author ChenJiaBao
 */
@Getter
@Setter
@Deprecated(since = "0.8.3", forRemoval = true)
public class HttpResponse<T> {
    private int code;
    private String msg;
    private T data;

    public HttpResponse() {
    }

    public HttpResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public HttpResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

}
