package com.chenjiabao.open.chenille.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限过滤信息
 * @author ChenJiaBao
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthFilterInfo {
    /**
     * 路径
     */
    private String path;
    /**
     * jwt令牌
     */
    private String jwtToken;
    /**
     * 负载
     */
    private String subject;

    @JsonIgnore
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public <T> T getSubject(Class<T> clazz) {
        return objectMapper.convertValue(subject, clazz);
    }

}
