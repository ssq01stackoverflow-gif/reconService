# 测试说明

## 推荐命令

优先使用 Maven Wrapper，并将 Maven 用户目录和本地仓库限制在项目目录中：

```bash
MAVEN_USER_HOME=.mvn/local-repo ./mvnw -Dmaven.repo.local=.mvn/local-repo/repository test
```

如果本机 Maven 可用，也可以执行：

```bash
mvn test
```

## 测试要求

- 核对匹配和比较逻辑需要单元测试。
- 任务执行、重试等编排逻辑需要 Service 测试。
- 新增 API 时补充 Controller 测试，覆盖参数校验和响应结构。
- 外部系统依赖要可 mock。
- 完成变更前，至少执行相关测试。
- 如果因环境限制无法执行测试，需要在最终说明中明确说明原因。
