# 技术方案草案：基于 Canal + Kafka 的准实时核对系统

## 状态

阶段 1 到 8 已完成第一版可运行骨架。当前方案用于记录已落地的设计和后续真实基础设施适配方向。

## 目标

- 将 Canal 原始消息转换为统一 `ChangeEvent`。
- 使用事件账本实现幂等消费。
- 使用 Java 规则接口匹配事件并创建核对任务。
- 通过 XXL-JOB 风格的扫描入口抢占并执行任务。
- 支持核对状态机、重试、告警、补偿和人工处理。
- 第一版使用内存仓储和内存告警客户端，后续替换为真实数据库、Kafka、XXL-JOB 和 Lark。

## 分层设计

- `domain.model`：领域模型和值对象。
- `domain.service`：规则接口、规则注册表、重试计算。
- `domain.repository`：仓储接口。
- `application.service`：用例编排，包括事件处理、任务执行、告警、补偿、人工处理和查询。
- `infrastructure.persistence`：第一版内存仓储实现，后续替换为数据库实现。
- `infrastructure.messaging`：Canal 消息解析，后续接入 Kafka Consumer。
- `infrastructure.external`：告警客户端，后续接入 Lark。
- `job`：调度入口，后续接入 XXL-JOB handler。
- `interfaces.controller`：REST 入口。

## 核心模型

- `ChangeEvent`：统一变更事件。
- `ReconTask`：核对任务。
- `ReconcilePolicy`：规则策略和重试配置。
- `ReconExecutionLog`：任务执行日志。
- `AlertLog`：告警日志和告警幂等记录。
- `ManualActionLog`：人工操作日志。
- `RuleMatchRecord`：事件命中规则记录。

## 幂等设计

事件幂等键：

```text
Canal destination + database + table + primaryKey + eventType + binlog position
```

任务创建幂等键：

```text
eventKey + ruleCode
```

业务聚合键：

```text
ruleCode + bizType + bizKey
```

## 状态机设计

已实现任务状态：

```text
INIT
RUNNING
RETRY_WAIT
SUCCESS
FAILED_FINAL
ALERTING
PAUSED
RESOLVED
```

说明：

- `preCheck` 不通过：`RUNNING -> RESOLVED`。
- 核对成功：`RUNNING -> SUCCESS`。
- 核对不一致且未达最大次数：`RUNNING -> RETRY_WAIT`。
- 核对不一致且达到最大次数：`RUNNING -> FAILED_FINAL`。
- 告警任务拉起：`FAILED_FINAL -> ALERTING`。
- 人工确认需要处理：`ALERTING -> PAUSED`。
- 人工确认不需要处理：`ALERTING/PAUSED -> RESOLVED`。
- 人工重试：`PAUSED -> RETRY_WAIT`。

## 当前实现入口

- 事件处理：`ChangeEventApplicationService`
- 任务执行：`ReconTaskExecutionService`
- 任务扫描：`ReconTaskScheduleService`
- 告警处理：`AlertApplicationService`
- 补偿处理：`CompensationApplicationService`
- 人工处理：`ManualTaskApplicationService`
- 查询服务：`ReconQueryApplicationService`

## REST 入口

- `POST /api/recon/events/canal`
- `POST /api/recon/jobs/tasks`
- `POST /api/recon/jobs/alerts`
- `POST /api/recon/jobs/compensation/events`
- `POST /api/recon/jobs/compensation/tasks`
- `POST /api/recon/jobs/compensation/alerts`
- `GET /api/recon/tasks`
- `GET /api/recon/tasks/{id}`
- `GET /api/recon/tasks/{id}/executions`
- `GET /api/recon/tasks/{id}/alerts`
- `GET /api/recon/tasks/{id}/actions`
- `POST /api/recon/tasks/{id}/mark-need-handle`
- `POST /api/recon/tasks/{id}/mark-no-need-handle`
- `POST /api/recon/tasks/{id}/retry`
- `POST /api/recon/tasks/{id}/notes`

## 后续设计任务

- 设计数据库表结构和索引。
- 将内存仓储替换为数据库仓储实现。
- 接入真实 Kafka Consumer。
- 校准真实 Canal 消息格式。
- 接入真实 XXL-JOB handler。
- 接入真实 Lark webhook/client。
- 完善分页查询、筛选条件和运维指标。
