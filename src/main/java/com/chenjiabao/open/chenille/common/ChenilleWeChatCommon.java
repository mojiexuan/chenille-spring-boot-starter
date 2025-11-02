package com.chenjiabao.open.chenille.common;

import com.chenjiabao.open.chenille.core.ChenilleExponentialBackoffUtils;
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
                        return ChenilleChannelException.builder()
                                .userMessage("获取信息空")
                                .logMessage("通过 code:[" + jsCode + "]获取到的OpenId是空的")
                                .build()
                                .logError()
                                .toMono();
                    }
                    if (openId.getErrcode() == 0) {
                        return Mono.just(openId.getOpenid());
                    } else {
                        return ChenilleChannelException.builder()
                                .userMessage("获取身份失败")
                                .logMessage("通过 code:[" + jsCode + "]获取OpenId失败:" + openId.getErrmsg())
                                .build()
                                .logError()
                                .toMono();
                    }
                })
                .onErrorResume(e -> ChenilleChannelException.builder()
                        .userMessage("获取身份异常")
                        .logMessage("通过 code:[" + jsCode + "]获取OpenId异常:" + e.getMessage())
                        .build()
                        .logError()
                        .toMono());
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
                        return ChenilleChannelException.builder()
                                .userMessage("获取手机号为空")
                                .logMessage("通过code:[" + code + "],openid:[" + openid + "]获取到的手机号是空的！")
                                .build()
                                .logError()
                                .toMono();
                    }
                    return Mono.just(phoneNumber.getPhone_info().getPhoneNumber());
                })
                // 异常处理
                .onErrorResume(e -> {
                    log.error("获取微信端用户手机号异常！", e);
                    return ChenilleChannelException.builder()
                            .userMessage("获取手机号异常")
                            .logMessage("通过code:[" + code + "],openid:[" + openid + "]获取手机号异常：" + e.getMessage())
                            .build()
                            .logError()
                            .toMono();
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
        ChenilleExponentialBackoffUtils backoff = ChenilleExponentialBackoffUtils.builder()
                .baseDelay(Duration.ofSeconds(5))   // 初始延迟
                .maxDelay(Duration.ofMinutes(5))    // 最大延迟，达到后保持固定
                .enableJitter(true)                 // 抖动机制，避免并发风暴
                .maxAttempts(0)                     // 0 = 无限重试
                .build();
        return tryRequestAccessToken(backoff, 1);
    }

    /**
     * 获取微信端AccessToken实际请求逻辑
     */
    private Mono<Void> tryRequestAccessToken(ChenilleExponentialBackoffUtils backoff, int attempt) {
        return webClient.post()
                .uri(weChat.getUrl().getAccessToken())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("appid", weChat.getAppId())
                        .with("secret", weChat.getAppSecret())
                        .with("grant_type", "client_credential"))
                .retrieve()
                .bodyToMono(ChenilleAccessToken.class)
                .flatMap(token -> {
                    if (token == null) {
                        log.warn("第 {} 次获取 AccessToken 结果为空，准备退避重试...", attempt);
                        return retryLater(backoff, attempt);
                    }

                    // ✅ 成功：保存 AccessToken
                    setAccessToken(token);

                    long delaySeconds = Math.max(token.getExpires_in() - 300, 300);
                    log.info("✅ 成功获取 AccessToken，有效期 {} 秒，将在 {} 秒后自动刷新",
                            token.getExpires_in(), delaySeconds);

                    // 到期前5分钟自动刷新
                    return Mono.delay(Duration.ofSeconds(delaySeconds))
                            .flatMap(t -> requestAccessToken())
                            .then();
                })
                .onErrorResume(e -> {
                    log.error("第 {} 次获取 AccessToken 失败：{}", attempt, e.getMessage());
                    return retryLater(backoff, attempt);
                });
    }

    /**
     * 根据指数退避策略延迟重试（无次数上限）
     */
    private Mono<Void> retryLater(ChenilleExponentialBackoffUtils backoff, int attempt) {
        Duration delay = backoff.nextDelay(attempt);
        log.warn("将在 {} 秒后进行第 {} 次重试...", delay.toSeconds(), attempt + 1);

        return Mono.delay(delay)
                .flatMap(t -> tryRequestAccessToken(backoff, attempt + 1))
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
