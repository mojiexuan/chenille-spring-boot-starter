package com.chenjiabao.open.chenille.core;

import com.chenjiabao.open.chenille.enums.ChenilleResponseCode;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.model.property.ChenilleTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/**
 * 接口返回类
 *
 * @author 陈佳宝 mail@chenjiabao.com
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // 不序列化空属性
public class ChenilleServerResponse<T> implements Serializable {
    private ChenilleResponseCode code;
    private String message;
    private T data;
    private final String time = new ChenilleTimeUtils(new ChenilleTime()).getNowTime();

    private ChenilleServerResponse(ChenilleResponseCode code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Builder<T> builder(){
        return new Builder<>();
    }

    public static ResponseEntity<ChenilleServerResponse<Void>> ok(){
        return success();
    }

    public static ResponseEntity<ChenilleServerResponse<Void>> success(){
        return success(null);
    }

    public static <T> ResponseEntity<ChenilleServerResponse<T>> success(T data){
        return ChenilleServerResponse.<T>builder()
                .setCode(ChenilleResponseCode.SUCCESS)
                .setData(data)
                .getResponseEntity();
    }

    public static ResponseEntity<ChenilleServerResponse<Void>> fail(ChenilleResponseCode code){
        return ChenilleServerResponse.<Void>builder()
                .setCode(code)
                .setMessage(code.getMessage())
                .getResponseEntity();
    }

    public static ResponseEntity<ChenilleServerResponse<Void>> fail(ChenilleChannelException e){
        return ChenilleServerResponse.<Void>builder()
                .setCode(e.getCode())
                .setMessage(e.getMessage())
                .getResponseEntity();
    }

    public static class Builder<T> {
        @Getter
        private ChenilleResponseCode code = ChenilleResponseCode.SUCCESS;
        @Getter
        private String message = "成功";
        @Getter
        private T data = null;

        public Builder() {
        }

        public Builder<T> setCode(ChenilleResponseCode code) {
            this.code = code;
            this.message = code.getMessage();
            return this;
        }

        public Builder<T> setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> setData(T data) {
            if(data != null){
                this.data = data;
            }
            return this;
        }

        public ChenilleServerResponse<T> build() {
            return new ChenilleServerResponse<>(this.code, this.message, this.data);
        }

        public ResponseEntity<ChenilleServerResponse<T>> getResponseEntity() {
            return this.getResponseEntity(build());
        }

        public ResponseEntity<ChenilleServerResponse<T>> getResponseEntity(ChenilleServerResponse<T> chenilleServerResponse) {
            return ResponseEntity
                    .status(chenilleServerResponse.code.getStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(chenilleServerResponse);
        }
    }
}
