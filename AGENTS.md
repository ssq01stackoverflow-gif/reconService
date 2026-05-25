# AGENTS.md

## 项目说明

`reconService` 是一个用于准实时核对的 Spring Boot 服务。

当前技术栈：

- Java 8
- Spring Boot 2.7.x
- Maven
- 不使用 Lombok

Spring Boot 版本固定在 2.7.x 线，目的是保持 Java 8 运行兼容性。不要升级到 Spring Boot 3.x，除非同时明确升级运行时 JDK 到 Java 17 或更高版本。

## 开发环境

- 项目 SDK 使用 Java 8。
- IntelliJ IDEA 的 Maven Runner JRE 使用 Project JDK 或 Java 8。
- IntelliJ IDEA 的 Java Compiler bytecode version 设置为 8。
- 如果 IDEA 报旧 JDK 路径不存在，检查 Project SDK、Maven Runner JRE 和构建缓存。
- 命令行优先使用 Maven Wrapper。

推荐测试命令：

```bash
MAVEN_USER_HOME=.mvn/local-repo ./mvnw -Dmaven.repo.local=.mvn/local-repo/repository test
```

如果本机 Maven 可用，也可以执行：

```bash
mvn test
```

## 项目目标

- 准实时接入业务侧和目标侧的数据变化。
- 按可配置核对键匹配数据记录。
- 支持字段、金额、状态、时间和派生值对比。
- 持久化核对任务、核对批次、源数据快照、目标数据快照和差异结果。
- 提供任务管理、手动重试、结果查询和运维检查接口。

## 工程规则

- 只使用 Maven 作为构建工具。
- 第一阶段保持清晰的分层单体结构。
- 优先使用简单明确的领域模型，不提前引入复杂抽象。
- 核对逻辑必须确定、可测试。
- Controller 和 Consumer 中不要写死核对规则。
- IO 集成逻辑与核对比较逻辑分离。
- 代码、配置默认使用 ASCII；业务文档或中文说明可以使用中文。
- 不引入 Lombok，避免 Java 8/IDE/Javac 兼容问题。

## 包结构

基础包名：

```text
com.recon.service
```

当前采用分层单体 DDD 结构：

```text
com.recon.service
  common
  config
  exception

  interfaces
    controller
    dto
    assembler

  application
    service
    command
    query

  domain
    model
    service
    repository
    event

  infrastructure
    persistence
    external
    messaging
    config

  job
```

### 层级职责

- `interfaces`：入站适配层，负责 HTTP、参数校验、请求/响应 DTO 和 DTO 转换。
- `application`：应用层，负责编排用例、事务边界和调用领域对象，不承载核心核对规则。
- `domain`：领域层，承载核心模型、值对象、领域服务、仓储接口和领域事件。
- `infrastructure`：基础设施层，承载数据库、消息、外部系统、仓储实现和技术配置。
- `job`：定时任务和异步调度入口，作为入站触发器调用应用层。
- `common`、`config`、`exception`：跨层基础设施，保持小而稳定，避免沉淀业务规则。

### 依赖规则

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

## 领域命名

除非模型设计发生明确变化，否则统一使用以下命名：

- `ReconTask`：核对任务定义。
- `ReconBatch`：一次任务执行批次。
- `ReconSourceRecord`：源侧数据快照。
- `ReconTargetRecord`：目标侧数据快照。
- `ReconRule`：核对规则或规则组。
- `ReconDiffResult`：核对差异结果。
- `ReconStatus`：任务、批次或结果状态。

## API 设计原则

- Controller 只处理 HTTP、参数校验和响应映射。
- Service 负责业务编排。
- 核对比较逻辑放在独立的策略类或引擎类中。
- 对外返回稳定 DTO，不直接暴露持久化实体。
- 使用 Bean Validation 做请求校验。
- 使用统一错误响应格式。

## 数据与处理原则

- 接入的源侧和目标侧记录按不可变快照处理。
- 数据接入要考虑幂等。
- 差异结果必须能追溯到源数据、目标数据、核对规则和执行批次。
- 使用明确状态表达 pending、processing、matched、mismatched、failed、ignored、confirmed。
- 人工处理动作与自动核对结果分离。

## 测试要求

- 核对匹配和比较逻辑需要单元测试。
- 任务执行、重试等编排逻辑需要 Service 测试。
- 新增 API 时补充 Controller 测试，覆盖参数校验和响应结构。
- 外部系统依赖要可 mock。

## 配置要求

- 使用 `application.yml`。
- 环境相关配置不要写死在代码里。
- 不提交密钥、账号密码或机器本地路径。
- 核对相关配置优先使用显式配置类承载。

## 完成变更前

有构建文件时，至少执行相关测试：

```bash
MAVEN_USER_HOME=.mvn/local-repo ./mvnw -Dmaven.repo.local=.mvn/local-repo/repository test
```

如果因环境限制无法执行测试，需要在最终说明中明确说明原因。
