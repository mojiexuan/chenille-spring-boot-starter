package com.chenjiabao.open.chenille.common;

import com.chenjiabao.open.chenille.core.ChenilleDelayedTaskExecutor;
import com.chenjiabao.open.chenille.exception.ChenilleChannelException;
import com.chenjiabao.open.chenille.model.ChenilleAccessToken;
import com.chenjiabao.open.chenille.model.ChenilleOpenId;
import com.chenjiabao.open.chenille.model.ChenillePhoneNumber;
import com.chenjiabao.open.chenille.model.ChenilleWeChatQrCodeResult;
import com.chenjiabao.open.chenille.model.property.ChenilleWeChat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
     * @return OpenId,空字符串表示获取失败
     */
    public String requestOpenId(String jsCode) throws ChenilleChannelException {
        try{
            // 请求微信端
            ChenilleOpenId openId = webClient.get().uri(weChat.getUrl().getOpenId()
                    + "?appid=" + weChat.getAppId()
                    + "&secret=" + weChat.getAppSecret()
                    + "&js_code=" + jsCode
                    + "&grant_type=authorization_code")
                    .retrieve()
                    .bodyToMono(ChenilleOpenId.class)
                    .block();

            if (openId == null) {
                throw new ChenilleChannelException("获取到的OpenId是空的！");
            }
            if (openId.getErrcode() == 0) {
                return openId.getOpenid();
            } else {
                throw new ChenilleChannelException("获取到的OpenId失败！" + openId.getErrmsg());
            }
        }catch (Exception e){
            log.error("获取微信端OpenId异常！",e);
            throw new ChenilleChannelException("获取微信端OpenId异常！" + e.getMessage());
        }
    }

    /**
     * 获取微信端用户手机号
     * @param code 微信客户端code
     * @return 手机号，空字符串表示获取失败
     */
    public String requestPhone(String code){
        return requestPhone(code, null);
    }

    /**
     * 获取微信端用户手机号
     * @param code 微信客户端code
     * @param openid 微信端用户openid
     * @return 手机号，空字符串表示获取失败
     */
    public String requestPhone(String code,String openid) throws ChenilleChannelException {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("code", code);
        if (openid != null) {
            requestBody.put("openid", openid);
        }

        try{
            ChenillePhoneNumber phoneNumber = webClient.post()
                    .uri(weChat.getUrl().getPhoneNumber() + "?access_token="
                            + weChat.getAppSecret())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(ChenillePhoneNumber.class)
                    .block();
            if(phoneNumber == null || phoneNumber.getPhone_info().getPhoneNumber().isEmpty()){
                throw new ChenilleChannelException("获取到的手机号是空的！");
            }
            return phoneNumber.getPhone_info().getPhoneNumber();
        }catch (Exception e){
            log.error("获取微信端用户手机号异常！",e);
            throw new ChenilleChannelException("获取微信端用户手机号异常！" + e.getMessage());
        }
    }

    /**
     * 获取小程序码
     * @param scene 场景值
     */
    public ChenilleWeChatQrCodeResult getQrCode(String scene){
        try{
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("scene", scene);
            requestBody.put("env_version", "develop");
            return webClient.post()
                    .uri(weChat.getUrl().getQrCode() + "?access_token=" + accessToken.getAccess_token())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .accept(MediaType.ALL)
                    .exchangeToMono(response -> {
                        MediaType contentType = response.headers()
                                .contentType()
                                .orElse(MediaType.APPLICATION_OCTET_STREAM);
                        if(contentType.includes(MediaType.IMAGE_JPEG)){
                            // 获取图片二进制数据
                            return response.bodyToMono(byte[].class)
                                    .map(imageData -> {
                                        ChenilleWeChatQrCodeResult successResult = new ChenilleWeChatQrCodeResult();
                                        successResult.setQrCodeImage(imageData);
                                        return successResult;
                                    });
                        }else if (contentType.includes(MediaType.APPLICATION_JSON)) {
                            // 获取错误信息
                            return response.bodyToMono(String.class).map(err->{
                                log.error("获取小程序二维码错误！{}",err);
                                ChenilleWeChatQrCodeResult errorResult = new ChenilleWeChatQrCodeResult();
                                errorResult.setErrorMsg(err);
                                return errorResult;
                            });
                        }else {
                            log.error("获取小程序二维码出现未知错误！{}",contentType);
                            ChenilleWeChatQrCodeResult unknownResult = new ChenilleWeChatQrCodeResult();
                            unknownResult.setErrorMsg("未知响应类型: " + contentType);
                            return Mono.just(unknownResult);
                        }
                    })
                    .block();
        } catch (Exception e) {
            log.error("获取小程序二维码出现网络错误！", e);
            ChenilleWeChatQrCodeResult netErrorResult = new ChenilleWeChatQrCodeResult();
            netErrorResult.setErrorMsg("网络错误");
            return netErrorResult;
        }
    }

    /**
     * 获取微信端AccessToken
     * 项目启动后轮训获取，在过期之前
     */
    public void requestAccessToken() {
        if(accessTokenFailCount > 5){
            log.error("获取AccessToken失败次数过多！向管理员报警！");
            throw new ChenilleChannelException("高危报警\n获取AccessToken失败次数过多！\n严重影响正常业务！");
        }
        try{
            ChenilleAccessToken accessToken = webClient.post()
                    .uri(weChat.getUrl().getAccessToken())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("appid", weChat.getAppId())
                            .with("secret", weChat.getAppSecret())
                            .with("grant_type", "client_credential"))
                    .retrieve()
                    .bodyToMono(ChenilleAccessToken.class)
                    .block();
            if(accessToken != null){
                accessTokenFailCount = 0;
                setAccessToken(accessToken);
                // expires_in的单位是秒，且 >= 300 , <= 7200
                new ChenilleDelayedTaskExecutor().executeAfterDelay(
                        accessToken.getExpires_in() - 300,
                        this::requestAccessToken);
            }else {
                accessTokenFailCount++;
                log.error("获取AccessToken结果发现为空对象！准备重试！");
                this.requestAccessToken();

            }
        }catch (Exception e){
            accessTokenFailCount++;
            log.error("获取AccessToken失败！准备重试！", e);
            this.requestAccessToken();
        }
    }

    /**
     * 设置accessToken
     * @param accessToken accessToken
     */
    public static void setAccessToken(ChenilleAccessToken accessToken) {
        ChenilleWeChatCommon.accessToken = accessToken;
    }
}
