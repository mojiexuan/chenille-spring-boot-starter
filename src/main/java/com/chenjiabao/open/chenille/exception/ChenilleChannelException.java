package com.chenjiabao.open.chenille.exception;

import com.chenjiabao.open.chenille.ChenilleBeanHelper;
import com.chenjiabao.open.chenille.core.ChenilleStringUtils;
import com.chenjiabao.open.chenille.enums.ChenilleResponseCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * 流处理异常
 * <p>
 * 在服务层或控制层抛出此异常
 */
@Getter
@Slf4j
@ToString
public class ChenilleChannelException extends RuntimeException {

    private final ChenilleResponseCode code;
    private final String userMessage;
    private final String logMessage;

    public ChenilleChannelException(Builder builder) {
        super(builder.logMessage, builder.cause);
        this.code = builder.code;
        this.userMessage = builder.userMessage;
        this.logMessage = builder.logMessage;
    }

    public <T> Mono<T> toMono() {
        return Mono.error(this);
    }

    public ChenilleChannelException logError() {
        log.error("Chenille Channel Exception - Code: {}, UserMessage: {}, LogMessage: {}",
                code.getCode(), userMessage, logMessage, this);
        return this;
    }

    public <T> T throwException() {
        throw this;
    }

    public <T> CompletableFuture<T> toFuture() {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(this);
        return future;
    }

    public ChenilleChannelException logWarn() {
        log.warn("Chenille Channel Exception - Code: {}, UserMessage: {}", code, userMessage, this);
        return this;
    }

    public static class Builder {
        private ChenilleResponseCode code = ChenilleResponseCode.SYSTEM_ERROR;
        private String userMessage;
        private String logMessage;
        private Throwable cause;

        public Builder code(ChenilleResponseCode code) {
            this.code = code;
            return this;
        }

        public Builder userMessage(String userMessage,Object... o) {
            if(o.length == 0){
                this.userMessage = userMessage;
            }
            try {
                this.userMessage = ChenilleBeanHelper.get(ChenilleStringUtils.class).format(userMessage,o);
            }catch (Exception e){
                log.error("Chenille Channel Exception - UserMessage 格式化错误 ->", e);
            }
            return this;
        }

        public Builder logMessage(String logMessage,Object... o) {
            if(o.length == 0){
                this.logMessage = logMessage;
            }
            try {
                this.logMessage = ChenilleBeanHelper.get(ChenilleStringUtils.class).format(logMessage,o);
            }catch (Exception e){
                log.error("Chenille Channel Exception - LogMessage 格式化错误 ->", e);
            }
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public ChenilleChannelException build() {
            if(code == null) {
                code = ChenilleResponseCode.SYSTEM_ERROR;
            }
            if(userMessage == null || userMessage.trim().isEmpty()) {
                userMessage = code.getMessage();
            }
            if(logMessage == null || logMessage.trim().isEmpty()) {
                logMessage = userMessage;
            }
            return new ChenilleChannelException(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ChenilleChannelException of(ChenilleResponseCode code, String userMessage) {
        return builder()
                .code(code)
                .userMessage(userMessage)
                .logMessage(userMessage)
                .build();
    }

    public static ChenilleChannelException of(ChenilleResponseCode code, String userMessage, String logMessage) {
        return builder()
                .code(code)
                .userMessage(userMessage)
                .logMessage(logMessage)
                .build();
    }

}
