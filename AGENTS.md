# AGENTS.md

## 项目硬约束

- 使用 Java 8。
- 使用 Spring Boot 2.7.x。
- 使用 Maven 和 Maven Wrapper。
- 不引入 Lombok。
- 不升级到 Spring Boot 3.x，除非同时明确升级运行时 JDK 到 Java 17 或更高版本。

## 代理工作规则

- 当前阶段保持清晰的分层单体结构。
- 优先使用简单明确的领域模型。
- 不提前引入复杂抽象，除非它确实降低复杂度、减少重复或匹配已有模式。
- 核对逻辑必须确定、可测试。
- Controller、Consumer、Job 中不要写死核对规则。
- IO 集成逻辑与核对比较逻辑分离。
- 代码、配置默认使用 ASCII；业务文档或中文说明可以使用中文。
- 不提交密钥、账号密码、机器本地路径、`target/`、IDE 文件或本地 Maven 缓存。

## DDD 依赖规则

- 依赖方向只能从外层指向内层：`interfaces/job/infrastructure -> application -> domain`。
- `domain` 不依赖 Spring、HTTP、数据库、MQ、DTO 或 infrastructure 实现。
- `application` 可以依赖 `domain` 的模型、领域服务和仓储接口。
- `application` 不直接依赖 controller、数据库实体、MQ 客户端或外部系统 SDK。
- `interfaces.controller` 只调用 `application.service`，不直接调用 repository 或 infrastructure。
- `interfaces.dto` 只用于接口入参出参，不进入 `domain`。
- 仓储接口放在 `domain.repository`，实现放在 `infrastructure.persistence`。

## 文档工作流

- 使用 `.agents/docs/` 编写需求草案、方案探索、测试用例规划和实现笔记。
- `.agents/docs/` 是代理协作工作区，不是权威项目事实。
- 已确认并已实现的内容，再提升到 `docs/`。
- `docs/` 必须与当前代码保持一致。
- 如果 `.agents/docs/` 与代码或 `docs/` 冲突，默认以代码和 `docs/` 为准，除非用户明确要求修订。
- 每一次执行产生代码、配置、测试或文档变更时，都必须同步更新项目进展和变更记录。
- 同步更新至少包括 `docs/project/status.md` 和 `docs/project/changelog.md`；如果涉及架构或决策，还要更新 `docs/project/decision-log.md`。
- 如果变更来自 `.agents/docs/` 中的需求、方案或测试草案，也要同步更新对应草案的实现状态或后续待办。

## 常用命令

推荐测试命令：

```bash
MAVEN_USER_HOME=.mvn/local-repo ./mvnw -Dmaven.repo.local=.mvn/local-repo/repository test
```

## 参考文档

- 项目状态：[docs/project/status.md](docs/project/status.md)
- 项目路线图：[docs/project/roadmap.md](docs/project/roadmap.md)
- 项目变更记录：[docs/project/changelog.md](docs/project/changelog.md)
- 设计决策索引：[docs/project/decision-log.md](docs/project/decision-log.md)
- 开发环境：[docs/development/environment.md](docs/development/environment.md)
- 测试说明：[docs/development/testing.md](docs/development/testing.md)
- DDD 分层：[docs/architecture/ddd-layering.md](docs/architecture/ddd-layering.md)
- 包结构：[docs/architecture/package-structure.md](docs/architecture/package-structure.md)
- 领域模型：[docs/domain/reconciliation-model.md](docs/domain/reconciliation-model.md)
- API 设计：[docs/api/api-design.md](docs/api/api-design.md)
