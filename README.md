# 个人Java开发工具集——Alpha

> 该项目仍处于高活跃更新态，各版本之间差距极大，GA正式版本发布后将稳定

该项目将自动注入SpringBoot项目中常用工具。

## 环境

- SpringBoot：+3.5.0
- Java：+17

## 版本日志

[版本日志](VERSION.md)

## 依赖引入

```xml
<dependency>
    <groupId>com.chenjiabao.open</groupId>
    <artifactId>devtools</artifactId>
    <version>0.6.3</version>
 </dependency>
```

## 版本管理 @ApiVersion

该注解通过扫描`@RestController`注解，为接口层添加接口版本前缀，例如登录接口 `http://127.0.0.1:8080/login` ，使用`@ApiVersion`后将变成 `http://127.0.0.1:8080/server/v1/login` 。

**配置**

默认接口前缀为`server`，该配置可更改接口前缀，你不应该添加前导及尾随`/`

```yaml
chenjiabao:
  config:
    api:
      prefix: server
```

**使用**

- 该注解支持传入`value`指定接口版本，默认为`1`。
- 该注解可同时应用于类或方法，其中方法注解的优先级高于类注解，这一点很有用，当你希望给类中所有接口设置版本时，可在类上直接使用，对于不一样的，可通过在方法上添加注解以实现覆盖。

```java
import com.chenjiabao.open.chenille.annotation.ApiVersion;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiVersion
public class Example {

}
```

## 请求中的属性获取 @RequestAttributeParam

在开发中你也许会遇到这样的场景，前端请求接口携带token，该token通过拦截器或过滤器验证通过之后，在拦截层或过滤层可直接向请求中添加用户ID属性，这样接口层可使用`@RequestAttributeParam`来获取请求中的属性值。

**使用**

```java
import com.chenjiabao.open.chenille.annotation.RequestAttributeParam;
import com.chenjiabao.open.chenille.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;

@PostMapping
public ApiResponse example(@RequestAttributeParam("id") Long uid) {
}
```

## 状态码 RequestCode

这是一个枚举类，可通过`getValue()`或`getCode()`获取状态码，可通过`getMessage()`获取提示消息。

```java
CODE_200(200,"成功"),
CODE_304(304,"发生错误，请求资源未被修改"),
CODE_401(401,"请求未授权，需要身份验证"),
CODE_403(403,"服务器拒绝请求"),
CODE_404(404,"请求资源不存在"),
CODE_405(405,"请求方法不被允许"),
CODE_406(406,"不接受的请求内容"),
CODE_429(429,"请求频率受限"),
CODE_500(500,"服务器内部错误");
```

## 接口返回类 ApiResponse

为了规范化接口返回格式，该类包含了
- 状态码：`int code`-默认值`200`
- 消息：`String message`-默认`成功`
- 数据：`Map<String, Object> data`-默认`null`
- 时间：`String time`-默认当前时间

空值不会被序列化。

**使用**

该类使用建造者模式设计，提供了一个`Builder`建造者，除`build()`构建方法外，其余方法均可链式调用。

部分方法如下：

`success()`默认-成功-200

```java
import com.chenjiabao.open.chenille.ApiResponse;

public void example() {
    ApiResponse.success().build();
}
```

`error(RequestCode code)`或`error(RequestCode code, String message)`

```java
import com.chenjiabao.open.chenille.enums.ResponseCode;
import com.chenjiabao.open.chenille.ApiResponse;

public void example() {
    ApiResponse.error(ResponseCode.CODE_406, "参数异常").build();
}
```

`builder()`获取构造器

```java
import com.chenjiabao.open.chenille.ApiResponse;

public void example() {
    ApiResponse.builder().build();
}
```

构造器提供部分方法如下：

- `code(RequestCode code)`-设置响应码
- `message(String message)`-设置消息
- `addData(String key, Object value)`-向data中添加数据
- `build()`-构建

## 雪花生成ID算法

machineId参数为机器ID，在分布式中这是 ***必须*** 的，且每台机器不一致的，单体应用可随意传值，不填则默认为`1`。

**配置**

```yaml
chenjiabao:
  config:
    machine:
      id: 1
```

**使用**

```java
import com.chenjiabao.open.chenille.SnowflakeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Example {

    private final SnowflakeUtils snowflakeUtils;

    @Autowired
    public Example(SnowflakeUtils snowflakeUtils) {
        this.snowflakeUtils = snowflakeUtils;
        // 生成唯一ID
        String uuid = snowflakeUtils.nextId();
    }
}
```

## JWT身份验证

本库集成了JWT身份验证

**配置**

`${JWT_SECRET}`为服务端秘钥，请配置在环境变量中，*不可泄露* ！若不配置使用默认值，当然也是可以的，但不推荐，重启后其值会变化。
`expires`用于配置token过期时间

```yaml
chenjiabao:
  config:
    jwt:
      secret: ${JWT_SECRET}
      expires: 7200
```

**使用**

```java
import com.chenjiabao.open.chenille.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Example {

    private final JwtUtils jwtUtils;

    @Autowired
    public Example(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
        // 生成token
        String token = jwtUtils.createToken("传入用户信息");
        // 验证token是否过期
        if (jwtUtils.validateToken(token)) {
            System.out.println("token有效");
        }
        // 获取Token中的信息
        String subject = jwtUtils.getSubject(token);
    }
}
```

## 加密工具

**配置**

`${HASH_PEPPER}`设置胡椒值，不设置则使用默认值，这是绝对不可以的，因为默认值是公开的！！！

你应该记住你的胡椒值，不可随意更改，否则用户会校验不通过，导致用户登录密码错误！！！

```yaml
chenjiabao:
  config:
    hash:
      pepper: ${HASH_PEPPER}
```

**使用**

```java
import com.chenjiabao.open.chenille.HashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Example {

    private final HashUtils hashUtils;

    @Autowired
    public Example(HashUtils hashUtils) {
        this.hashUtils = hashUtils;

        // 获取盐值
        String salt = hashUtils.getRandomSalt();

        // 字符串转Hash256
        String s1 = hashUtils.stringToHash256("原字符串");

        // 加密用户密码
        // salt为用户的盐值，各用户不同
        String s2 = hashUtils.stringToHash256WithSaltAndPepper("用户原密码", salt);
    }
}
```

## 任务调度器

**使用**

`executeAfterDelay()`方法存在两个参数：

- 第一个参数是延迟时间，单位秒
- Runnable接口

```java
import com.chenjiabao.open.chenille.DelayedTaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Example {

    private final DelayedTaskExecutor delayedTaskExecutor;

    @Autowired
    public Example(DelayedTaskExecutor delayedTaskExecutor) {
        this.delayedTaskExecutor = delayedTaskExecutor;

        // 开启延迟任务
        delayedTaskExecutor.executeAfterDelay(5000, () -> {
            // 做一些事情
        });
    }
}
```

若你需要关闭任务，可使用`delayedTaskExecutor.shutdown();`

## 时间工具

东八区时间处理工具。

**使用**

```java
import com.chenjiabao.open.chenille.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Example {
    private final TimeUtils timeUtils;

    @Autowired
    public Example(TimeUtils timeUtils) {
        this.timeUtils = timeUtils;

        // 获取当前时间字符串 yyyy-MM-dd HH:mm:ss格式
        String t1 = timeUtils.getNowTime();
        // 同上 指定格式
        String t2 = timeUtils.getNowTime("yyyy-MM-dd HH:mm:ss");
        // 秒级时间戳转指定格式（yyyy-MM-dd HH:mm:ss）字符串（东八区）
        String t3 = timeUtils.getTime(1752883200L);
        // 同上 指定格式
        String t4 = timeUtils.getTime(1752883200L, "yyyy-MM-dd HH:mm:ss");
        // 获取当前时间戳
        long t5 = timeUtils.getNowTimeStamp();
        // 将时间字符串转指定格式时间戳
        long t6 = timeUtils.getTimeStamp("2025-7-19 08:00:00", "yyyy-MM-dd HH:mm:ss");
        // 获取东八区当日凌晨（00:00）时间戳（秒级）
        long t7 = timeUtils.getNowStartDayTimeStamp();
    }
}
```

## 校验工具

准备了一些用于校验的工具，例如邮箱号、中国手机号、字符串等。

**使用**

```java
import com.chenjiabao.open.chenille.CheckUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Example {

    public final CheckUtils checkUtils;

    @Autowired
    public Example(CheckUtils checkUtils) {
        this.checkUtils = checkUtils;
        // 接受可变参数列表，任一参数为空返回false
        boolean c1 = checkUtils.isValidEmptyParam();
        // 判断是否是11位中国手机号，支持 +86 或 86 区号
        boolean c2 = checkUtils.isValidChinaPhoneNumber("11111111111");
        // 验证电子邮件地址是否合法
        boolean c3 = checkUtils.isValidEmail("xxx@mail.com");
        // 验证是否纯数字字符串
        boolean c4 = checkUtils.isValidNumberString("123456789");
        // 验证字符串是否仅由0-9、a-z、A-Z构成
        boolean c5 = checkUtils.isValidNumberAndLetters("123abcABC");
        // 验证字符串是否仅由字母(a-z、A-Z)构成
        boolean c6 = checkUtils.isValidAlphabeticString("abcABC");
        // 验证字符串长度是否在指定范围内
        boolean c7 = checkUtils.isLengthInRange("xxx", 2, 20);
        // 验证字符串是否是合法用户名（字母开头，允许字母数字下划线，长度4-20）
        boolean c8 = checkUtils.isValidUsername("_hello");
    }
}
```

## 文件工具

**配置**

- `format`配置支持的文件格式列表
- `path`用户上传文件保存目录
- `max-size`支持最大文件大小，单位B，默认5242880B = 5MB

```yaml
chenjiabao:
  config:
    file:
      format:
        - .png
        - .jpg
        - .jpeg
        - .bmp
      path: /public/upload/avatar
      max-size: 5242880
```

**使用**

```java
import com.chenjiabao.open.chenille.FilesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Example {

    private final FilesUtils filesUtils;

    @Autowired
    public Example(FilesUtils filesUtils) {
        this.filesUtils = filesUtils;
        // 校验文件
        filesUtils.checkFile();
        // 保存文件
        filesUtils.saveFile();
        // 删除文件
        filesUtils.deleteFile();
        // 检查文件是否存在奶
        filesUtils.existFile();
    }
}
```

通过`FilesUtils.classesPath`可以获取当前工作（项目）根目录路径。

## 依赖说明

本库中SpringBoot的Web启动器需要您自行加入。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
