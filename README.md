# 介绍

[webstack-vue](https://github.com/zxbdzh/webstack-vue)的api项目，使用 Spring 构建，主要技术有
- SpringWeb
- caffeine 本地缓存
- s3存储 实现上传下载文件
- mybatis 驱动crud
- mybatis-plus 使mybatis更简单易用
- lombok 实用注解
- jsoup 解析网页
- jwt 令牌验证

# 使用教程

1. 下载java 17及其以上的 jdk
2. 运行sql 在release里
3. 在 `target` 下修改 application.yaml
```yaml
server:
  port: 8081
  baseUrl: https://dev.webstack.zxbdwy.online # 你的webstack地址
aws:
  s3:
    s3Url: https://cn-sy1.rains3.com # S3服务器地址
    accessKey: xxxxx # 账号key
    secretKey: xxxx # 私钥
spring:
  datasource:
    url: jdbc:mysql://xxxx:3306/webstack-vue
```
3. 在 `target` 下运行命令（需要先打包）
```java
java -jar webstack-backend-0.0.1-SNAPSHOT.jar
```
3. 代理（本地调试不用）
