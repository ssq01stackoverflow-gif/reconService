# 项目状态

本文档记录项目当前进展、近期目标、阻塞项和下一步动作。它是项目事实文档，应与代码和已确认文档保持一致。

## 当前阶段

准实时核对主链路骨架阶段。

项目当前已经建立 Spring Boot 服务骨架、DDD 分层包结构和文档工作流，并落地了 Canal 事件模型、事件幂等账本、Java 规则接口、核对任务、任务执行状态机、告警、补偿、人工处理和查询接口的第一版内存实现。真实 Kafka、数据库、XXL-JOB 和 Lark 适配器尚未接入。

## 当前状态

- 已初始化 Java 8、Spring Boot 2.7.x、Maven 项目。
- 已提供基础健康检查接口。
- 已建立统一响应对象和全局异常处理器。
- 已建立分层单体 DDD 包结构。
- 已建立 `.agents/` 草稿区和 `docs/` 项目事实文档区。
- 已建立项目状态、路线图、变更记录和决策索引文档。
- 已新增 `ChangeEvent`、`ReconTask`、`ReconcileRule`、执行日志、告警日志和人工操作日志等核心模型。
- 已实现事件幂等处理和规则匹配创建任务流程。
- 已实现核对任务执行状态机、重试计算、告警发送、补偿和人工处理服务。
- 已提供内存仓储和内存 Lark 告警客户端作为第一版适配器。
- 已提供 Job 入口和 REST 查询/操作接口。
- 已同步 `.agents/docs/` 下的需求、技术方案、测试用例、待办和决策草案。
- 已建立“每次执行变更必须同步更新项目进展和变更记录”的规则。

## 已完成

- 项目基础骨架。
- Maven Wrapper 和本地 Maven 仓库使用约定。
- DDD 分层包结构。
- `AGENTS.md` 代理规则精简。
- 架构、领域、API、开发环境和测试说明文档拆分。
- Canal + Kafka 准实时核对系统需求草案。
- 阶段 1 到 8 的第一版可运行骨架。
- `.agents/docs/designs/canal-kafka-reconciliation-design.md` 技术方案草案。
- `.agents/docs/test-cases/canal-kafka-reconciliation-test-cases.md` 测试用例草案。
- `.agents/docs/backlog/canal-kafka-reconciliation-backlog.md` 后续待办。
- `.agents/docs/decisions/canal-kafka-reconciliation-decisions.md` 决策草案。

## 进行中

- 真实基础设施适配前的领域和应用服务骨架完善。
- 技术方案和测试用例文档沉淀。

## 下一步

- 设计数据库表结构和持久化实现。
- 接入真实 Kafka Consumer 和 Canal 消息格式。
- 接入真实 XXL-JOB handler。
- 接入真实 Lark webhook/client。
- 补充 Controller 测试、补偿测试、告警幂等测试和查询接口测试。
- 将确认后的需求和技术方案从 `.agents/docs/` 提升到 `docs/`。

## 阻塞项

暂无。

## 状态维护规则

- 每完成一个阶段性功能后，更新本文档。
- 每次把 `.agents/docs/` 中的草案提升为项目事实时，更新本文档。
- 如果状态与代码不一致，以代码为准，并及时修正文档。
