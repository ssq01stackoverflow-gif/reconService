# DDD 分层

当前项目采用分层单体 DDD 结构。第一阶段优先保持边界清晰，不提前拆分服务。

## 层级职责

- `interfaces`：入站适配层，负责 HTTP、参数校验、请求/响应 DTO 和 DTO 转换。
- `application`：应用层，负责编排用例、事务边界和调用领域对象，不承载核心核对规则。
- `domain`：领域层，承载核心模型、值对象、领域服务、仓储接口和领域事件。
- `infrastructure`：基础设施层，承载数据库、消息、外部系统、仓储实现和技术配置。
- `job`：定时任务和异步调度入口，作为入站触发器调用应用层。
- `common`、`config`、`exception`：跨层基础设施，保持小而稳定，避免沉淀业务规则。

## 依赖规则

- 依赖方向只能从外层指向内层：`interfaces/job/infrastructure -> application -> domain`。
- `domain` 不依赖 Spring、HTTP、数据库、MQ、DTO 或 infrastructure 实现。
- `application` 可以依赖 `domain` 的模型、领域服务和仓储接口，但不直接依赖 controller、数据库实体、MQ 客户端或外部系统 SDK。
- `interfaces.controller` 只调用 `application.service`，不直接调用 repository 或 infrastructure。
- `interfaces.dto` 只用于接口入参出参，不进入 `domain`。
- `interfaces.assembler` 负责 DTO 与 command/query/response 之间的转换，不承载业务判断。
- 仓储接口放在 `domain.repository`，实现放在 `infrastructure.persistence`。
- 外部系统和消息中间件的技术细节放在 `infrastructure.external` 或 `infrastructure.messaging`，领域层通过接口表达需求。
- `job` 可以调用 `application.service` 启动用例，不直接写核对规则或访问数据库实现。
- 跨层复用对象必须谨慎；业务含义明确的对象优先放入 `domain.model`，接口协议对象放入 `interfaces.dto`。

## 工程原则

- 只使用 Maven 作为构建工具。
- 第一阶段保持清晰的分层单体结构。
- 优先使用简单明确的领域模型，不提前引入复杂抽象。
- 核对逻辑必须确定、可测试。
- Controller 和 Consumer 中不要写死核对规则。
- IO 集成逻辑与核对比较逻辑分离。
- 代码、配置默认使用 ASCII；业务文档或中文说明可以使用中文。
- 不引入 Lombok，避免 Java 8、IDE 和 javac 兼容问题。
