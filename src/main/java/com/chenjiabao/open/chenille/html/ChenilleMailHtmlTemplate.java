package com.chenjiabao.open.chenille.html;

/**
 * 邮件模板
 * @author ChenJiaBao
 */
public class ChenilleMailHtmlTemplate {

    /**
     * 获取邮件验证码模板
     * @return 验证码模板
     */
    public static String getCodeTemplate(){
        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <title>{brand}验证码</title>
    <link rel="shortcut icon" href="https://chenjiabao.com/img/favicon.ico" type="image/x-icon"/>
    <meta name="viewport"
          content="width=device-width,user-scalable=no,initial-scale=1,maximum-scale=1,minimum-scale=1"/>
    <meta charset="UTF-8"/>
    <meta name="format-detection" content="telephone=no"/>
    <meta name="renderer" content="webkit"/>
    <meta name="version" content="mico"/>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
</head>
<body>
<div style="background-color: #f7f8fa;width: 100%;padding: 15px;box-sizing: border-box;">
    <div style="display: flex;align-items: center;">
        <img src="https://chenjiabao.com/favicon.ico" width="20" height="20" alt="LOGO">
        <div style="margin-left: 10px;font-weight: 700;">{brand}</div>
    </div>
    <div style="height: 2px;background-color: #00a4ff;border: 0;font-size: 0;padding: 0;width: 100%;margin-top: 20px;">
    </div>
    <div style="background-color: #ffffff;padding: 10px;box-sizing: border-box;border-radius: 0 0 5px 5px;">
        <div style="width: 100%;text-align: center;">
            <div style="font-weight: 900;font-size: 20px;margin: 10px 0 20px 0;">
                {brand}验证码
            </div>
        </div>
        <div style="background-color: black; width: 100%;height: 1px;"></div>
        <div style="height: 20px;"></div>
        <div style="font-weight: 400;">
            <p>尊敬的用户，您好：</p>
            <p>您的验证码是</p>
            <p style="
            color:#f15a29;
            font-size: 25px;
            font-weight: 700;
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #F5F5F5;
            padding: 10px;
            box-sizing: border-box;
            border-radius: 8px;">
                {code}
            </p>
            <p>{expireTime}内有效。请勿泄露给他人，若这不是您本人操作，请忽略。</p>
        </div>
        <div>
            <div style="margin-top: 40px;font-weight: 400;">此致</div>
            <div style="font-weight: 700;margin-top: 10px;">{brand}</div>
        </div>
    </div>
    <div style="height: 20px;"></div>
    <div style="width: 100%;text-align: center;">
        <div style="font-size: 12px;color: #9d9d9d;">此为系统邮件，请勿回复。</div>
    </div>
</div>
</body>
</html>
                """;
    }

    /**
     * 获取系统通知模板
     * @return 系统通知模板
     */
    public static String getSystemNoticeTemplate(){
        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <title>{title}</title>
    <link rel="shortcut icon" href="https://chenjiabao.com/img/favicon.ico" type="image/x-icon"/>
    <meta name="viewport"
          content="width=device-width,user-scalable=no,initial-scale=1,maximum-scale=1,minimum-scale=1"/>
    <meta charset="UTF-8"/>
    <meta name="format-detection" content="telephone=no"/>
    <meta name="renderer" content="webkit"/>
    <meta name="version" content="mico"/>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
</head>
<body>
<div style="font-size: 0;">收到 {writer} 的来信，快来看看吧！</div>
<img src="https://api.chenjiabao.com/public/upload/images/1/headImage.png" width="100%">
<div style="background-color: #f7f8fa;width: 100%;padding: 15px;box-sizing: border-box;">
    <div style="height: 2px;background-color: #00a4ff;border: 0;font-size: 0;padding: 0;width: 100%;">
    </div>
    <div style="background-color: #ffffff;padding: 10px;box-sizing: border-box;border-radius: 0 0 5px 5px;">
        <div style="width: 100%;text-align: center;">
            <div style="font-weight: 900;font-size: 20px;margin: 10px 0 20px 0;">{title}</div>
        </div>
        <div style="background-color: black; width: 100%;height: 1px;"></div>
        <div style="height: 20px;"></div>
        <div style="font-weight: 400;">
            <p style="font-weight: 700;">{call}</p>
            <div>{content}</div>
        </div>
        <div>
            <div style="margin-top: 40px;font-weight: 400;">此致</div>
            <div style="font-weight: 700;margin-top: 10px;">{writer}</div>
            <div style="font-weight: 700;margin-top: 10px;">{time}</div>
        </div>
    </div>
    <div style="height: 20px;"></div>
    <div style="width: 100%;text-align: center;">
        <div style="font-size: 12px;color: #9d9d9d;">此为系统邮件，请勿回复。</div>
    </div>
</div>
</body>

</html>""";
    }
}
