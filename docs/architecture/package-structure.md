# 包结构

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

## 包职责

- `common`：通用响应、错误码、分页对象、基础常量等。
- `config`：应用级 Spring 配置。
- `exception`：全局异常、业务异常和异常处理器。
- `interfaces.controller`：Controller，只处理 HTTP、参数校验和调用应用层。
- `interfaces.dto`：Request 和 Response DTO，不暴露领域模型。
- `interfaces.assembler`：DTO 与 command、query、response 的转换。
- `application.service`：应用服务，负责编排业务用例。
- `application.command`：写操作入参对象。
- `application.query`：读操作入参对象。
- `domain.model`：实体、聚合根、值对象和领域枚举。
- `domain.service`：领域服务。
- `domain.repository`：仓储接口。
- `domain.event`：领域事件。
- `infrastructure.persistence`：数据库实体、Mapper 和仓储实现。
- `infrastructure.external`：外部系统客户端和适配器。
- `infrastructure.messaging`：消息消费者、生产者和消息中间件适配。
- `infrastructure.config`：基础设施相关配置。
- `job`：定时任务和异步调度入口。
