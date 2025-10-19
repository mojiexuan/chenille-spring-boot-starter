package com.chenjiabao.open.chenille.common;

import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.model.ChenilleAccessToken;
import com.chenjiabao.open.chenille.model.ChenilleOpenId;
import com.chenjiabao.open.chenille.model.ChenillePhoneNumber;
import com.chenjiabao.open.chenille.model.ChenilleWeChatQrCodeResult;
import com.chenjiabao.open.chenille.model.property.ChenilleWeChat;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信工具
 * @author ChenJiaBao
 */
@Slf4j
public class ChenilleWeChatCommon {

    private final ChenilleWeChat weChat;
    // 维护accessToken，不用每次都刷新
    private static ChenilleAccessToken accessToken = null;
    // 获取AccessToken失败次数
    private int accessTokenFailCount = 0;
    private final WebClient webClient;

    public ChenilleWeChatCommon(ChenilleWeChat weChat){
        this.weChat = weChat;
        webClient = WebClient.create();
    }

    /**
     * 获取微信端OpenId
     * @param jsCode 微信端临时jsCode
     * @return OpenId
     */
    public Mono<String> requestOpenId(String jsCode) throws ChenilleChannelException {
        return webClient.get()
                // 拼接微信 OpenId 请求地址
                .uri(uriBuilder -> uriBuilder
                        .path(weChat.getUrl().getOpenId())
                        .queryParam("appid", weChat.getAppId())
                        .queryParam("secret", weChat.getAppSecret())
                        .queryParam("js_code", jsCode)
                        .queryParam("grant_type", "authorization_code")
                        .build()
                )
                .retrieve()
                // 将响应体反序列化为 ChenilleOpenId 对象
                .bodyToMono(ChenilleOpenId.class)
                .flatMap(openId -> {
                    if (openId == null) {
                        return Mono.error(new ChenilleChannelException("获取到的OpenId是空的！"));
                    }
                    if (openId.getErrcode() == 0) {
                        return Mono.just(openId.getOpenid());
                    } else {
                        return Mono.error(new ChenilleChannelException("获取OpenId失败：" + openId.getErrmsg()));
                    }
                })
                .onErrorResume(e -> {
                    log.error("获取微信端OpenId异常！", e);
                    return Mono.error(new ChenilleChannelException("获取微信端OpenId异常！" + e.getMessage()));
                });
    }

    /**
     * 获取微信端用户手机号
     * @param code 微信客户端code
     * @return 手机号，空字符串表示获取失败
     */
    public Mono<String> requestPhone(String code){
        return requestPhone(code, null);
    }

    /**
     * 获取微信端用户手机号
     * @param code 微信客户端code
     * @param openid 微信端用户openid
     * @return 手机号
     */
    public Mono<String> requestPhone(String code,String openid) throws ChenilleChannelException {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("code", code);
        if (openid != null) {
            requestBody.put("openid", openid);
        }

        return webClient.post()
                // 构建 URI
                .uri(uriBuilder -> uriBuilder
                        .path(weChat.getUrl().getPhoneNumber())
                        .queryParam("access_token", weChat.getAppSecret())
                        .build()
                )
                // 设置 Content-Type
                .contentType(MediaType.APPLICATION_JSON)
                // 写入请求体
                .bodyValue(requestBody)
                // 取回响应体
                .retrieve()
                .bodyToMono(ChenillePhoneNumber.class)
                // 业务逻辑处理
                .flatMap(phoneNumber -> {
                    if (phoneNumber == null
                            || phoneNumber.getPhone_info() == null
                            || phoneNumber.getPhone_info().getPhoneNumber() == null
                            || phoneNumber.getPhone_info().getPhoneNumber().isEmpty()) {
                        return Mono.error(new ChenilleChannelException("获取到的手机号是空的！"));
                    }
                    return Mono.just(phoneNumber.getPhone_info().getPhoneNumber());
                })
                // 异常处理
                .onErrorResume(e -> {
                    log.error("获取微信端用户手机号异常！", e);
                    return Mono.error(new ChenilleChannelException("获取微信端用户手机号异常！" + e.getMessage()));
                });
    }

    /**
     * 获取小程序码
     * @param scene 场景值
     * @param envVersion 环境版本 develop
     */
    public Mono<ChenilleWeChatQrCodeResult> getQrCode(@NonNull String scene,
                                                      @NonNull String envVersion){
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("scene", scene);
        requestBody.put("env_version", envVersion);

        return webClient.post()
                // 构建请求 URI
                .uri(uriBuilder -> uriBuilder
                        .path(weChat.getUrl().getQrCode())
                        .queryParam("access_token", accessToken.getAccess_token())
                        .build())
                // 设置请求头
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .bodyValue(requestBody)
                // 处理响应
                .exchangeToMono(response -> {
                    // 获取响应类型
                    MediaType contentType = response.headers()
                            .contentType()
                            .orElse(MediaType.APPLICATION_OCTET_STREAM);

                    // 1️⃣ 图片类型（成功）
                    if (contentType.includes(MediaType.IMAGE_JPEG)) {
                        return response.bodyToMono(byte[].class)
                                .map(imageData -> {
                                    ChenilleWeChatQrCodeResult result = new ChenilleWeChatQrCodeResult();
                                    result.setQrCodeImage(imageData);
                                    return result;
                                });
                    }

                    // 2️⃣ JSON 错误响应
                    else if (contentType.includes(MediaType.APPLICATION_JSON)) {
                        return response.bodyToMono(String.class)
                                .map(err -> {
                                    log.error("获取小程序二维码错误！{}", err);
                                    ChenilleWeChatQrCodeResult result = new ChenilleWeChatQrCodeResult();
                                    result.setErrorMsg(err);
                                    return result;
                                });
                    }

                    // 3️⃣ 未知类型响应
                    else {
                        log.error("获取小程序二维码出现未知响应类型：{}", contentType);
                        ChenilleWeChatQrCodeResult result = new ChenilleWeChatQrCodeResult();
                        result.setErrorMsg("未知响应类型: " + contentType);
                        return Mono.just(result);
                    }
                })
                // 捕获异常，转为统一的错误结果
                .onErrorResume(e -> {
                    log.error("获取小程序二维码出现网络错误！", e);
                    ChenilleWeChatQrCodeResult result = new ChenilleWeChatQrCodeResult();
                    result.setErrorMsg("网络错误：" + e.getMessage());
                    return Mono.just(result);
                });
    }

    /**
     * 获取微信端AccessToken
     * 项目启动后轮训获取，在过期之前
     */
    public Mono<Void> requestAccessToken() {
        if(accessTokenFailCount > 5){
            log.error("获取AccessToken失败次数过多！向管理员报警！");
            throw new ChenilleChannelException("高危报警\n获取AccessToken失败次数过多！\n严重影响正常业务！");
        }
        return webClient.post()
                // 构建请求
                .uri(weChat.getUrl().getAccessToken())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("appid", weChat.getAppId())
                        .with("secret", weChat.getAppSecret())
                        .with("grant_type", "client_credential"))
                .retrieve()
                .bodyToMono(ChenilleAccessToken.class)
                // 核心业务逻辑
                .flatMap(token -> {
                    if (token == null) {
                        accessTokenFailCount++;
                        log.error("获取 AccessToken 结果为空，准备重试...");
                        return scheduleRetry();
                    }

                    // 成功：重置失败次数，保存 AccessToken
                    accessTokenFailCount = 0;
                    setAccessToken(token);

                    // 计算延迟时间（提前5分钟刷新）
                    long delaySeconds = Math.max(token.getExpires_in() - 300, 300);
                    return Mono.delay(Duration.ofSeconds(delaySeconds))
                            .flatMap(tick -> requestAccessToken())
                            .then();
                })
                // 捕获异常并进行重试
                .onErrorResume(e -> {
                    accessTokenFailCount++;
                    log.error("获取 AccessToken 失败，准备重试", e);
                    return scheduleRetry();
                });
    }

    /**
     * 失败后延迟重试
     */
    private Mono<Void> scheduleRetry() {
        return Mono.delay(Duration.ofSeconds(10))
                .flatMap(tick -> requestAccessToken())
                .then();
    }

    /**
     * 设置accessToken
     * @param accessToken accessToken
     */
    public static void setAccessToken(ChenilleAccessToken accessToken) {
        ChenilleWeChatCommon.accessToken = accessToken;
    }
}
