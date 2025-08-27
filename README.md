# Chenille——适用于SpringBoot的开发工具集（Alpha）

> ⚠️ 该项目仍处于高活跃更新态，各版本之间差距极大，GA正式版本发布后将稳定。
>
> （换句话说，现在用它，就像养一只贪吃的毛毛虫：每天都有点新变化。）

**Chenille**，法语里是 **毛毛虫** 的意思。别小看它——虽然看起来柔软、慢吞吞，但它有着 **惊人的蜕变潜力**。就像毛毛虫终将化身为翩翩起舞的蝴蝶，`Chenille` 希望成为你项目里的 **灵活小帮手**，帮你把繁琐的 Spring Boot 开发任务“啃掉一口”，慢慢成长为优雅、轻量、可扩展的解决方案。

> **温馨提示**：使用 `Chenille` 不会让你的代码长毛毛虫，但会让开发更轻松，也许还会偷偷变漂亮。

---

## 环境

- SpringBoot：+3.5.0
- Java：+17
  
（是的，你需要一只现代的毛毛虫，而不是考古级的 JDK。）

---

## 版本日志

[版本日志](VERSION.md)

（别忘了时不时看看，毛毛虫今天又学会了什么新技能。）

---

## 依赖引入

```xml
<dependency>
    <groupId>com.chenjiabao.open</groupId>
    <artifactId>chenille-spring-boot-starter</artifactId>
    <version>0.1.1</version>
</dependency>
```

（放心，这个依赖不会偷偷下载一堆奇怪的爬虫 🐛。）

---

## 版本管理 @ApiVersion

该注解通过扫描`@RestController`注解，为接口层添加接口版本前缀，例如登录接口 `http://127.0.0.1:8080/login` ，使用`@ApiVersion`后将变成 `http://127.0.0.1:8080/server/v1/login` 。

**配置**

默认接口前缀为`server`，该配置可更改接口前缀，你不应该添加前导及尾随`/`

```yaml
chenille:
  config:
    api:
      prefix: server
```

**使用**

- 该注解支持传入`value`指定接口版本，默认为`1`。
- 该注解可同时应用于类或方法，其中方法注解的优先级高于类注解，这一点很有用，当你希望给类中所有接口设置版本时，可在类上直接使用，对于不一样的，可通过在方法上添加注解以实现覆盖。

（小技巧：当你希望给整类接口统一加版本号时，把注解放在类上就行，省得一个个加，像毛毛虫一次啃掉一整片叶子。）

```java
import com.chenjiabao.open.chenille.annotation.ApiVersion;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiVersion
public class Example {

}
```

---

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

> 小贴士：当你不想在 Controller 里到处找 token 时，这个注解会像忠诚的毛毛虫一样默默爬到你手上

---

## 状态码 RequestCode

`ResponseCode` 枚举类用于统一管理接口的 业务响应码，它结合了 HTTP 状态码 和 业务逻辑状态，便于前后端约定与调试。

- code：业务码（如 COMM-4000），用于精确标识业务错误
- status：对应的 HTTP 状态码
- enMessage：英文提示消息（适合国际化场景）
- zhMessage：中文提示消息

**常见状态码一览**

| 业务码        | HTTP 状态 | 中文消息      | 英文消息                          |
|------------|---------|-----------|-------------------------------|
| COMM-0000  | 200     | 成功        | Success                       |
| SYS-0500   | 500     | 系统错误      | System Error                  |
| SYS-0100   | 503     | 服务暂不可用    | Service Unavailable           |
| COMM-4000  | 400     | 请求参数错误    | Invalid Parameter             |
| COMM-4001  | 400     | 缺少必要参数    | Required Parameter is Missing |
| COMM-4040  | 404     | 请求资源不存在   | Resource Not Found            |
| AUTH-4001  | 401     | 未授权，请登录   | Unauthorized                  |
| AUTH-4003  | 403     | 权限不足，禁止访问 | Permission Denied             |
| AUTH-4100  | 400     | 用户名或密码错误  | Invalid Username or Password  |
| AUTH-4101  | 400     | 验证码错误     | Invalid Captcha               |
| USER-4100  | 400     | 用户已存在     | User Already Exists           |
| USER-4040  | 404     | 用户不存在     | User Not Found                |
| ORDER-4300 | 400     | 订单状态异常    | Order Status is Invalid       |
| PAY-5000   | 500     | 支付失败      | Payment Failed                |
| COMM-2020  | 202     | 请求已接受，处理中 | Accepted                      |

> 小提示：别再到处写 200、500 这样的魔法数字了，把它们交给 ResponseCode，让你的代码既专业又整齐。

---

## 接口返回类 BaoServerResponse

`BaoServerResponse` 是统一的接口响应类，用于规范化接口的返回结果。相比直接返回原始数据，它能让前后端之间的交互更清晰、可维护。

**特性**

- **统一格式**：所有接口返回均包含 `code`、`message`、`data`、`time` 字段。
- **状态码规范**：`code` 使用 [`ResponseCode`](#状态码-responsecode) 枚举，避免魔法数字。
- **链式构建**：通过 `builder()` 灵活定制响应。
- **便捷方法**：`success()`、`ok()`、`fail()` 开箱即用。
- **自动序列化**：空字段不会被序列化（减少冗余）。
- **内置时间戳**：返回时自动生成响应时间。

**字段说明**

| 字段名       | 类型             | 说明                                          |
|-----------|----------------|---------------------------------------------|
| `code`    | `ResponseCode` | 业务响应码，默认 `SUCCESS`，可结合统一枚举类使用               |
| `message` | `String`       | 提示信息，默认 `"成功"`                              |
| `data`    | `T`（泛型）        | 返回的数据载体，可以是对象、列表或基本类型                       |
| `time`    | `String`       | 响应时间，自动生成，格式 `yyyy-MM-dd HH:mm:ss`（东八区时区处理） |

**使用示例**

1、返回成功（无数据）

```java
@GetMapping("/ping")
public ResponseEntity<BaoServerResponse<Void>> ping() {
    return BaoServerResponse.ok();
}
```

2、返回成功（带数据）

```java
@GetMapping("/user")
public ResponseEntity<BaoServerResponse<UserDto>> getUser() {
    UserDto user = new UserDto("chenille", "毛毛虫");
    return BaoServerResponse.success(user);
}
```

3、返回失败（业务异常）

```java
@GetMapping("/secure")
public ResponseEntity<BaoServerResponse<Void>> secure() {
    throw new ChannelException(ResponseCode.UNAUTHORIZED, "请先登录");
}

// 在全局异常处理器中捕获：
@ExceptionHandler(ChannelException.class)
public ResponseEntity<BaoServerResponse<Void>> handle(ChannelException e) {
    return BaoServerResponse.fail(e);
}
```

4、使用 Builder 灵活构建

```java
@PostMapping("/custom")
public ResponseEntity<BaoServerResponse<String>> custom() {
    return BaoServerResponse.<String>builder()
            .setCode(ResponseCode.PARAM_ERROR)
            .setMessage("参数校验失败")
            .setData("具体错误信息")
            .getResponseEntity();
}
```

5、示例返回 JSON

```json
{
  "code": "COMM-0000",
  "message": "成功",
  "data": {
    "id": 1,
    "name": "chenille"
  },
  "time": "2025-08-26 18:25:30"
}
```

> 🐛 **友情提示**：`BaoServerResponse` 就像毛毛虫裹上的小茧——看似普通，却能让你的接口响应更优雅。等它破茧而出时，你的项目也会更漂亮。

---

## 雪花生成ID算法

`SnowflakeUtils` 是一个用于生成分布式环境下 **全局唯一、短小紧凑且有序递增** 的 ID 工具，每个 ID 都是独一无二的，就像毛毛虫的花纹一样。

**配置说明**

- **machineId**：机器 ID。在分布式环境中，这是 **必须** 的，每台机器必须不同。单体应用可随意传值，不填则默认为 `1`。
- **位数说明**：
    - 机器 ID 占 10 位，最多支持 1024 台机器。
    - 序列号占 12 位，每毫秒最多生成 4096 个 ID。
    - 起始时间戳为 `2025-01-01 00:00:00`。

```yaml
chenille:
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

**特性**

- 全局唯一：结合时间戳、机器 ID 和序列号生成，确保全局唯一性。
- 有序递增：ID 根据时间戳递增，便于数据库索引优化。
- 短小紧凑：输出为 Base62 字符串（0-9, A-Z, a-z），比纯数字 ID 更短。
- 高并发安全：使用 synchronized 和 AtomicLong 确保线程安全。
- 时间回退检测：若系统时间回退，将抛出异常，避免生成重复 ID。

> 小贴士：如果你想让 ID 看起来像艺术品一样，SnowflakeUtils 可助你一臂之力——毛毛虫也会羡慕你的花纹。

---

## JWT身份验证

本库集成了 JWT（JSON Web Token）身份验证，用于生成和验证用户身份的安全 Token，每个 Token 都是独一无二的。

**配置说明**

- **JWT_SECRET**：服务端秘钥，请配置在环境变量中，*不可泄露*！  
  若不配置，使用默认值也是可以的，但不推荐，重启后其值会变化。
- **expires**：Token 过期时间（单位：秒）。

```yaml
chenille:
  config:
    jwt:
      secret: ${JWT_SECRET}
      expires: 7200
```

> ⚠️ 秘钥建议使用 Base64 编码的 32 字节以上字符串，确保安全性。

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

        // 生成 Token（可存放用户信息）
        String token = jwtUtils.createToken("传入用户信息");

        // 验证 Token 是否有效
        if (jwtUtils.validateToken(token)) {
            System.out.println("Token 有效");
        }

        // 获取 Token 中的用户信息
        String subject = jwtUtils.getSubject(token, String.class);
        System.out.println("Token 中的用户信息：" + subject);
    }
}

```

> 别担心，这里的 “秘钥” 可比毛毛虫的食谱安全得多。

---

## 加密工具

`HashUtils` 提供了 SHA-256 哈希加密功能，支持 **盐值** 和 **胡椒值** 增强安全性，适合密码加密或敏感信息保护。

**配置说明**

- **HASH_PEPPER**：胡椒值，用于增强哈希安全性。
    - 不设置时使用默认值，但默认值是公开的，**绝对不可在生产环境使用**。
    - 一旦设置，不可随意更改，否则用户密码校验将失败。

```yaml
chenille:
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

        // 生成随机盐值，每个用户独立
        String salt = hashUtils.getRandomSalt();

        // 字符串转 SHA-256 哈希（Base64 编码）
        String s1 = hashUtils.stringToHash256("原字符串");

        // 带盐值和胡椒值的密码加密
        String s2 = hashUtils.stringToHash256WithSaltAndPepper("用户原密码", salt);

        System.out.println("普通哈希: " + s1);
        System.out.println("加盐加胡椒哈希: " + s2);
    }
}

```

> 温馨提醒：别忘了设置 pepper（胡椒值）。如果忘了，你的密码保护就像没加调料的毛毛虫餐：毫无味道，危险又单调。

---

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

> 它的执行就像毛毛虫的日常作息：定点爬、定点吃，从不缺席。

---

## 时间工具

`TimeUtils` 是一个东八区时间处理工具，提供秒级和毫秒级时间戳转换、格式化、时间差计算、工作日判断等功能。

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
        // 指定格式
        String t2 = timeUtils.getNowTime("yyyy-MM-dd HH:mm:ss");

        // 秒级时间戳转指定格式字符串（东八区）
        long sampleSeconds = 1752883200L;
        String t3 = timeUtils.formatSeconds(sampleSeconds);
        String t4 = timeUtils.formatSeconds(sampleSeconds, "yyyy-MM-dd HH:mm:ss");

        // 获取当前时间戳
        long t5 = timeUtils.currentSeconds();

        // 时间字符串转时间戳
        long t6 = timeUtils.parseSeconds("2025-07-19 08:00:00", "yyyy-MM-dd HH:mm:ss");

        // 获取东八区当日凌晨（00:00）时间戳（秒级）
        long t7 = timeUtils.todayStartSeconds();
    }
}
```

**方法概览**

| 方法                                           | 功能                                  |
|----------------------------------------------|-------------------------------------|
| `getNowTime()`                               | 获取当前时间字符串（默认格式 yyyy-MM-dd HH:mm:ss） |
| `getNowTime(String format)`                  | 获取当前时间字符串（自定义格式）                    |
| `parseMillis(String time, String format)`    | 时间字符串转毫秒级时间戳                        |
| `parseSeconds(String time, String format)`   | 时间字符串转秒级时间戳                         |
| `formatMillis(long millis, String format)`   | 毫秒级时间戳转指定格式字符串                      |
| `formatSeconds(long seconds, String format)` | 秒级时间戳转指定格式字符串                       |
| `currentMillis()`                            | 获取当前毫秒级时间戳                          |
| `currentSeconds()`                           | 获取当前秒级时间戳                           |
| `todayStartMillis()`                         | 获取当天凌晨00:00毫秒级时间戳                   |
| `todayStartSeconds()`                        | 获取当天凌晨00:00秒级时间戳                    |
| `diffMillis(long t1, long t2)`               | 计算毫秒差                               |
| `diffSeconds(long t1, long t2)`              | 计算秒差                                |
| `isWeekday(long millis)`                     | 判断是否工作日                             |
| `plusMillis(long millis, long msToAdd)`      | 增加毫秒                                |
| `plusSeconds(long seconds, long sToAdd)`     | 增加秒                                 |
| `isBetween(long time, long start, long end)` | 判断时间是否在范围内                          |
| `formatDuration(long durationMillis)`        | 格式化持续时间为 HH:mm:ss                   |
| `getHour(long millis)`                       | 获取指定时间小时数（0-23）                     |

> 时间在爬行，代码在进化。放心，这只毛毛虫懂东八区。

---

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

> 当你怀疑用户提交的手机号是不是编的，交给 CheckUtils，它比毛毛虫的触角还灵敏。

## 文件工具

`FilesUtils` 提供文件操作相关功能，包括文件校验、保存、删除、读取等，并支持文件上传格式、大小限制等配置。  
通过 `FilesUtils.classesPath` 可获取当前项目根目录路径。

**配置**

- `format`：支持的文件格式列表
- `path`：用户上传文件保存目录
- `max-size`：支持最大文件大小，单位 B，默认 5MB（5242880 B）

```yaml
chenille:
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
import org.springframework.web.multipart.MultipartFile;

@Service
public class Example {

    private final FilesUtils filesUtils;

    @Autowired
    public Example(FilesUtils filesUtils) {
        this.filesUtils = filesUtils;
    }

    public void demo(MultipartFile file) {
        // 校验文件是否合法
        var status = filesUtils.checkFile(file);
        if (status.getCode() != 200) {
            System.out.println("文件不合法：" + status.getMessage());
            return;
        }

        // 保存文件（使用配置的默认路径）
        String savedPath = filesUtils.saveFile(file);
        System.out.println("文件保存路径：" + savedPath);

        // 删除文件
        boolean deleted = filesUtils.deleteFile(savedPath);
        System.out.println("文件删除结果：" + deleted);

        // 判断文件是否存在
        boolean exists = filesUtils.existFile(savedPath);
        System.out.println("文件是否存在：" + exists);
    }
}

```

**方法概览**

| 方法                                                        | 功能                  |
|-----------------------------------------------------------|---------------------|
| `checkFile(MultipartFile file)`                           | 校验文件是否合法（格式、大小、空文件） |
| `saveFile(MultipartFile file)`                            | 保存文件到配置路径，返回相对路径    |
| `saveFile(MultipartFile file, String savePath)`           | 保存文件到指定路径，返回相对路径    |
| `deleteFile(File file)`                                   | 删除指定文件              |
| `existFile(File file)`                                    | 判断文件是否存在            |
| `existFile(String path)`                                  | 判断路径对应的文件是否存在       |
| `existDir(File dir)`                                      | 判断目录是否存在            |
| `existDir(String path)`                                   | 判断路径对应目录是否存在        |
| `createFile(File file)`                                   | 创建新文件及父目录           |
| `createDirectory(File dir)`                               | 创建目录及父目录            |
| `readerFile(String file)`                                 | 读取文本文件内容（按行）        |
| `writerFile(String file, String content, boolean append)` | 写入文本文件，支持追加模式       |
| `closeFile(Reader fileReader)`                            | 关闭 Reader 流         |
| `closeFile(BufferedReader bufferedReader)`                | 关闭 BufferedReader 流 |
| `closeFile(FileWriter fileWriter)`                        | 关闭 FileWriter 流     |
| `closeFile(BufferedWriter bufferedWriter)`                | 关闭 BufferedWriter 流 |


通过`FilesUtils.classesPath`可以获取当前工作（项目）根目录路径。

> 它会帮你检查文件，像毛毛虫啃叶子一样挑剔。

## 邮件工具

本库提供邮件发送功能，支持普通邮件、验证码发送及系统通知发送，支持 HTML 模板渲染。

**配置**

- `host` SMTP 服务器地址
- `port` SMTP 端口（默认 465）
- `ssl` 是否启用 SSL（默认 true）
- `auth` 是否需要身份验证（默认 false）
- `username` SMTP 用户名（启用身份验证时必填）
- `password` SMTP 密码（启用身份验证时必填）
- `protocol` 邮件传输协议（默认 smtp）
- `brand` 邮件品牌（默认 "码界轩"）

**使用**

```java
import com.chenjiabao.open.chenille.MailUtils;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

@Service
public class Example {

    public void sendMail() throws MessagingException {
        MailUtils mailUtils = new MailUtils.Builder(new CheckUtils())
                .setHost("smtp.example.com")
                .setPort(465)
                .setSsl(true)
                .setAuth(true)
                .setUsername("your_username")
                .setPassword("your_password")
                .setBrand("你的品牌名")
                .build();

        // 发送普通邮件
        mailUtils.setFrom("from@example.com")
                .setTo("to@example.com")
                .setSubject("测试邮件")
                .setContent("<h1>邮件内容</h1>")
                .send();

        // 发送验证码
        mailUtils.setFrom("from@example.com")
                .setTo("to@example.com")
                .setSubject("验证码邮件")
                .sendCode("123456");

        // 发送系统通知
        mailUtils.setFrom("from@example.com")
                .setTo("to@example.com")
                .setSubject("系统通知")
                .sendSystemNotice("标题", "称呼", "内容正文", "作者");
    }
}
```

## 价格工具

本库提供价格计算与格式化工具，支持元与分的相互转换以及货币格式化。

**使用**

```java
import com.chenjiabao.open.chenille.PriceUtils;
import java.math.BigDecimal;

public class Example {

    public void priceDemo() {
        PriceUtils priceUtils = new PriceUtils();

        // 元转分
        Long fen = priceUtils.yuanToFen(new BigDecimal("12.34")); // 1234

        // 分转元
        BigDecimal yuan = priceUtils.fenToYuan(1234L); // 12.34

        // 格式化元
        String formattedYuan = priceUtils.formatYuan(new BigDecimal("12.34")); // ￥12.34

        // 格式化分
        String formattedFen = priceUtils.formatFen(1234L); // ￥12.34
    }
}
```

## 敏感词工具

基于 DFA（确定有穷自动机）算法实现的敏感词检测与替换工具，线程安全，适合高性能文本过滤。

**使用**

```java
import com.chenjiabao.open.chenille.SensitiveWordUtils;
import java.util.List;

public class Example {
    public void sensitiveDemo() {
        SensitiveWordUtils sensitiveWordUtils = SensitiveWordUtils.builder()
                .init(List.of("敏感词1", "敏感词2"));

        String text = "这里包含敏感词1和正常内容";

        // 检测是否包含敏感词
        boolean hasSensitive = sensitiveWordUtils.contains(text); // true

        // 替换敏感词为 '*'
        String filtered = sensitiveWordUtils.replace(text, '*'); // 这里包含**和正常内容
    }
}
```

## Redis 工具

简化 Spring Redis 操作的工具类，支持字符串和列表操作，同时可设置过期时间。

**使用**

```java
import com.chenjiabao.open.chenille.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Example {

    private final RedisUtils redisUtils;

    @Autowired
    public Example(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;

        // 设置字符串键值对，过期时间 60 秒
        redisUtils.set("key1", "value1", 60);

        // 获取字符串
        String value = redisUtils.getString("key1");

        // 删除键
        redisUtils.delete("key1");

        // 判断键是否存在
        boolean exists = redisUtils.hasKey("key1");

        // 设置列表
        redisUtils.setList("listKey", List.of("A", "B", "C"), 120);

        // 获取列表
        List<String> list = redisUtils.getList("listKey");

        // 更新过期时间
        redisUtils.expire("listKey", 300);
    }
}

```

## 字符串工具

提供常用字符串操作、格式化和随机生成工具。

**使用**

```java
import com.chenjiabao.open.chenille.StringUtils;

public class Example {

    public static void main(String[] args) {
        StringUtils stringUtils = new StringUtils();

        // 判断字符串是否为空
        boolean empty = stringUtils.isEmpty("test");

        // 判断字符串是否为数字
        boolean isNumber = stringUtils.isStringNumber("12345");

        // 复制文本到剪切板
        stringUtils.copyToClipboard("Hello World");

        // 字符串转 Base64
        String base64 = stringUtils.stringToBase64("Hello");

        // 生成随机字符串
        String randomStr = stringUtils.generateSureString(8);

        // 生成随机数字字符串
        String randomNum = stringUtils.generateRandomNumberString(6);

        // 数量格式化
        String formattedNumber = stringUtils.numberFormat(123456);

        // 文件大小格式化
        String fileSize = stringUtils.formatFileSize(10240);

        // 隐藏手机号中间位
        String maskedPhone = stringUtils.maskPhone("13800138000");
    }
}
```

## 依赖说明

本库中SpringBoot的Web启动器和WebFlux启动器需要您自行加入。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- WebFlux -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
<optional>true</optional>
</dependency>
```

> 千万别忘了加依赖，不然你的毛毛虫会饿肚子，啥也干不了。
