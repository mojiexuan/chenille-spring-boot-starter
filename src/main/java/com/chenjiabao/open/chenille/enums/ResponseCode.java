package com.chenjiabao.open.chenille.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * 标准业务响应码枚举
 * 遵循规则：HTTP状态码(status)表示网络请求状态，业务码(code)和消息(message)表示业务逻辑状态。
 */
@Getter
public enum ResponseCode {

    // region ########## 通用成功 ##########
    /**
     * 通用成功
     */
    SUCCESS("COMM-0000", HttpStatus.OK, "Success", "成功"),
    // endregion

    // region ########## 系统级错误 (SYS-) ##########
    /**
     * 系统内部错误
     */
    SYSTEM_ERROR("SYS-0500", HttpStatus.INTERNAL_SERVER_ERROR, "System Error", "系统错误"),
    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE("SYS-0100", HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", "服务暂不可用"),
    // endregion

    // region ########## 客户端通用错误 (COMM-4xxx) ##########
    /**
     * 请求参数错误
     */
    PARAM_ERROR("COMM-4000", HttpStatus.BAD_REQUEST, "Invalid Parameter", "请求参数错误"),
    /**
     * 缺少必要参数
     */
    PARAM_MISSING("COMM-4001", HttpStatus.BAD_REQUEST, "Required Parameter is Missing", "缺少必要参数"),
    /**
     * 请求资源不存在
     */
    NOT_FOUND("COMM-4040", HttpStatus.NOT_FOUND, "Resource Not Found", "请求资源不存在"),
    // endregion

    // region ########## 认证与授权错误 (AUTH-) ##########
    /**
     * 未认证/Token失效
     */
    UNAUTHORIZED("AUTH-4001", HttpStatus.UNAUTHORIZED, "Unauthorized", "未授权，请登录"),
    /**
     * 无权限访问
     */
    FORBIDDEN("AUTH-4003", HttpStatus.FORBIDDEN, "Permission Denied", "权限不足，禁止访问"),
    /**
     * 用户名或密码错误
     */
    BAD_CREDENTIALS("AUTH-4100", HttpStatus.BAD_REQUEST, "Invalid Username or Password", "用户名或密码错误"),
    /**
     * 验证码错误
     */
    INVALID_CAPTCHA("AUTH-4101", HttpStatus.BAD_REQUEST, "Invalid Captcha", "验证码错误"),
    // endregion

    // region ########## 业务逻辑错误 (按模块划分，例如 USER-, ORDER-) ##########
    /**
     * 用户已存在
     */
    USER_EXISTS("USER-4100", HttpStatus.BAD_REQUEST, "User Already Exists", "用户已存在"),
    /**
     * 用户不存在
     */
    USER_NOT_FOUND("USER-4040", HttpStatus.NOT_FOUND, "User Not Found", "用户不存在"),
    /**
     * 订单状态异常
     */
    ORDER_INVALID_STATUS("ORDER-4300", HttpStatus.BAD_REQUEST, "Order Status is Invalid", "订单状态异常"),
    /**
     * 支付失败
     */
    PAYMENT_FAILED("PAY-5000", HttpStatus.INTERNAL_SERVER_ERROR, "Payment Failed", "支付失败"),
    // endregion

    // region ########## 其他状态 ##########
    /**
     * 请求已接受，正在处理（常用于异步操作）
     */
    ACCEPTED("COMM-2020", HttpStatus.ACCEPTED, "Accepted", "请求已接受，处理中"),
    // endregion
    ;

    /**
     * 业务响应码 (字符串类型)，遵循 [模块]-[编号] 的格式
     */
    private final String code;

    /**
     * HTTP 状态码
     */
    private final HttpStatusCode status;

    /**
     * 英文消息（用于国际化或开发者调试）
     */
    private final String enMessage;

    /**
     * 中文消息
     */
    private final String zhMessage;

    ResponseCode(String code, HttpStatusCode status, String enMessage, String zhMessage) {
        this.code = code;
        this.status = status;
        this.enMessage = enMessage;
        this.zhMessage = zhMessage;
    }

    /**
     * 获取消息（默认返回中文，可根据项目需求重写此方法以支持国际化）
     */
    public String getMessage() {
        return this.zhMessage;
    }

    /**
     * 获取消息（支持指定语言）en zh
     */
    public String getMessage(String lang) {
        if ("en".equalsIgnoreCase(lang)) {
            return this.enMessage;
        } else {
            return this.zhMessage;
        }
    }
}
