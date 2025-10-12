package com.chenjiabao.open.chenille.handler;

import com.chenjiabao.open.chenille.serializer.ChenilleServerResponse;
import com.chenjiabao.open.chenille.enums.ChenilleResponseCode;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ServerWebInputException;

/**
 * 这些异常发生在控制器方法内部同步执行时
 */
@Slf4j
@ControllerAdvice
public class ChenilleSyncExceptionHandler {

    /**
     * 流处理异常
     * <p>
     * 你在处理流时，抛出的 {@link ChenilleChannelException} 异常，若未捕获会在此处处理
     */
    @ExceptionHandler(value = ChenilleChannelException.class)
    @ResponseBody
    public ResponseEntity<ChenilleServerResponse<Void>> channelExceptionHandler(ChenilleChannelException e) {
        log.error("流处理异常 -> {}", e.getMessage());
        return ChenilleServerResponse.fail(e).entityOf();
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(value = ServerWebInputException.class)
    @ResponseBody
    public ResponseEntity<ChenilleServerResponse<Void>> serverWebInputExceptionHandler(ServerWebInputException e) {
        log.error("参数绑定异常 -> {}", e.getMessage());
        return ChenilleServerResponse.fail(ChenilleResponseCode.PARAM_ERROR).entityOf();
    }

    /**
     * 校验异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ChenilleServerResponse<Void>> methodArgumentNotValidExceptionHandler(
            MethodArgumentNotValidException e) {
        log.error("校验异常 -> {}", e.getMessage());
        String message = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return ChenilleServerResponse
                .<Void>builder()
                .setCode(ChenilleResponseCode.PARAM_ERROR)
                .setMessage(message)
                .entityOf();
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity<ChenilleServerResponse<Void>> exceptionHandler(Exception e) {
        log.error("系统异常 -> {}", e.getMessage());
        return ChenilleServerResponse.fail(ChenilleResponseCode.SYSTEM_ERROR).entityOf();
    }

}
