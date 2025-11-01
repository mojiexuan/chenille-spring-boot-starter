# 版本日志

> 一些小的更新可能不会被及时记录。

遵循语义化版本

| 版本名   | 描述                                                                                                                                                                                                  |
|-------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 0.2.4 | 1、增加`ChenilleMonoErrorFilter`，处理Mono错误                                                                                                                                                              |
| 0.2.3 | 修复包装类无限递归的问题                                                                                                                                                                                        |
| 0.2.2 | 增加部分工具类，修复`ChenilleResponseHandler`对`ChenilleServerResponse`的包装问题                                                                                                                                   |
| 0.2.1 | 1、修复Spring配置元数据                                                                                                                                                                                     |
| 0.2.0 | 1、重构所有类名，添加Chenille前缀<br/>2、拥抱WebFlux，该版本移除对SpringMVC的依赖，WebMVC请使用上一版本<br/>3、实现二级缓存<br/>4、移除迁移残留，现在真正属于毛毛虫<br/>5、增加`ChenilleResponseHandler`响应体增强器，对响应体进行增强，添加ChenilleServerResponse包装<br/>         |
| 0.1.1 | 1、修复`AuthFilterInfo`只能获取字符串类型荷载的问题                                                                                                                                                                  |
| 0.1.0 | 1、源自于 [devtools](https://github.com/mojiexuan/open) 的沉淀，正式规范化发布该版本<br/>2、重构了`RequestCode`<br/>3、重构了接口返回类 `BaoServerResponse`<br/>4、`JwtUtils`支持复杂类型的荷载<br/>5、增加`ChannelException`异常类，这在WebFlux中或许有用 |
