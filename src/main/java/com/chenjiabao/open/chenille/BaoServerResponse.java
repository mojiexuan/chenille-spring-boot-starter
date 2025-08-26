package com.chenjiabao.open.chenille;

import com.chenjiabao.open.chenille.enums.ResponseCode;
import com.chenjiabao.open.chenille.exception.ChannelException;
import com.chenjiabao.open.chenille.model.property.Time;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * 接口返回类
 * <p>
 * 该类已被废弃，建议使用 {@link BaoServerResponse} 类。
 *
 * @author 陈佳宝 mail@chenjiabao.com
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // 不序列化空属性
public class BaoServerResponse<T> {
    private ResponseCode code = ResponseCode.SUCCESS;
    private String message = "成功";
    private T data = null;
    private final String time = new TimeUtils(new Time()).getNowTime();

    private BaoServerResponse(ResponseCode code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Builder<T> builder(){
        return new Builder<>();
    }

    public static ResponseEntity<BaoServerResponse<Void>> ok(){
        return success();
    }

    public static ResponseEntity<BaoServerResponse<Void>> success(){
        return success(null);
    }

    public static <T> ResponseEntity<BaoServerResponse<T>> success(T data){
        return BaoServerResponse.<T>builder()
                .setCode(ResponseCode.SUCCESS)
                .setData(data)
                .getResponseEntity();
    }

    public static ResponseEntity<BaoServerResponse<Void>> fail(ChannelException e){
        return BaoServerResponse.<Void>builder()
                .setCode(e.getCode())
                .setMessage(e.getMessage())
                .getResponseEntity();
    }

    public static class Builder<T> {
        @Getter
        private ResponseCode code = ResponseCode.SUCCESS;
        @Getter
        private String message = "成功";
        @Getter
        private T data = null;

        public Builder() {
        }

        public Builder<T> setCode(ResponseCode code) {
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

        public BaoServerResponse<T> build() {
            return new BaoServerResponse<>(this.code, this.message, this.data);
        }

        public ResponseEntity<BaoServerResponse<T>> getResponseEntity() {
            return this.getResponseEntity(build());
        }

        public ResponseEntity<BaoServerResponse<T>> getResponseEntity(BaoServerResponse<T> baoServerResponse) {
            return ResponseEntity
                    .status(baoServerResponse.code.getStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(baoServerResponse);
        }
    }
}
