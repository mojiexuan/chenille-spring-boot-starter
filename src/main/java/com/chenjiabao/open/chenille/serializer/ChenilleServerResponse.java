package com.chenjiabao.open.chenille.serializer;

import com.chenjiabao.open.chenille.core.ChenilleTimeUtils;
import com.chenjiabao.open.chenille.enums.ChenilleResponseCode;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.model.property.ChenilleTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口返回类
 *
 * @author 陈佳宝 mail@chenjiabao.com
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // 不序列化空属性
public class ChenilleServerResponse<T> implements Serializable {
    @Getter
    private ChenilleResponseCode code;
    @Getter
    private String message;
    @Getter
    private T data;
    @Getter
    private final String time;

    private ChenilleServerResponse() {
        this.code = ChenilleResponseCode.SUCCESS;
        this.message = ChenilleResponseCode.SUCCESS.getMessage();
        this.time = new ChenilleTimeUtils(new ChenilleTime()).getNowTime();
    }

    public ChenilleServerResponse<T> setCode(ChenilleResponseCode code) {
        this.code = code;
        this.message = code.getMessage();
        return this;
    }

    public ChenilleServerResponse<T> setData(T data) {
        this.data = data;
        return this;
    }

    /**
     * 添加数据到 data 中,需要保证 data 是 Map<String, Object> 类型
     *
     * @param key   键
     * @param value 值
     * @return 当前对象
     */
    @SuppressWarnings("unchecked")
    public ChenilleServerResponse<T> addData(String key, Object value) {
        if(this.data == null){
            this.data = (T) new HashMap<>();
        }
        if(this.data instanceof Map){
            ((Map<String, Object>) this.data).put(key, value);
        }else {
            throw ChenilleChannelException.builder()
                    .logMessage("无法添加数据：'data' 不是 Map 类型")
                    .build();
        }
        return this;
    }

    public ChenilleServerResponse<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    @JsonIgnore
    public ResponseEntity<ChenilleServerResponse<T>> entityOf() {
        return ResponseEntity
                .status(this.code.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(this);
    }

    public static <T> ChenilleServerResponse<T> builder() {
        return new ChenilleServerResponse<>();
    }

    public static ChenilleServerResponse<Map<String, Object>> mapOf() {
        return ChenilleServerResponse.builder();
    }

    public static ChenilleServerResponse<Void> success() {
        return success(null);
    }

    public static <T> ChenilleServerResponse<T> success(T data) {
        return ChenilleServerResponse.<T>builder()
                .setCode(ChenilleResponseCode.SUCCESS)
                .setData(data);
    }

    public static ChenilleServerResponse<Void> fail() {
        return ChenilleServerResponse.fail(ChenilleResponseCode.SYSTEM_ERROR);
    }

    public static ChenilleServerResponse<Void> fail(ChenilleResponseCode code) {
        return ChenilleServerResponse.<Void>builder()
                .setCode(code)
                .setMessage(code.getMessage());
    }

    public static ChenilleServerResponse<Void> fail(ChenilleChannelException e) {
        return ChenilleServerResponse.<Void>builder()
                .setCode(e.getCode())
                .setMessage(e.getMessage());
    }

}
