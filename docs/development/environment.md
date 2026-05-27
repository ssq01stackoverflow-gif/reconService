# 开发环境

`reconService` 是一个用于准实时核对的 Spring Boot 服务。

## 技术栈

- Java 8
- Spring Boot 2.7.x
- Maven
- 不使用 Lombok

Spring Boot 版本固定在 2.7.x 线，目的是保持 Java 8 运行兼容性。不要升级到 Spring Boot 3.x，除非同时明确升级运行时 JDK 到 Java 17 或更高版本。

## 本地环境

- 项目 SDK 使用 Java 8。
- IntelliJ IDEA 的 Maven Runner JRE 使用 Project JDK 或 Java 8。
- IntelliJ IDEA 的 Java Compiler bytecode version 设置为 8。
- 如果 IDEA 报旧 JDK 路径不存在，检查 Project SDK、Maven Runner JRE 和构建缓存。
- 命令行优先使用 Maven Wrapper。
