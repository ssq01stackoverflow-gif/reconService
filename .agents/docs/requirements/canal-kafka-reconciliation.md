# 需求草案：基于 Canal + Kafka 的准实时核对系统

## 背景

业务数据库发生变更后，希望通过 Binlog 准实时触发核对任务，验证相关业务数据是否达到预期状态。系统基于 Canal 订阅 Binlog，并将消息投递到 Kafka。`reconService` 消费 Kafka 消息后，将 Canal 原始消息转换成统一事件模型，匹配 Java 编写的核对规则，幂等创建核对任务，再由 XXL-JOB 定时抢占任务并执行核对。

本文件是需求草案，位于 `.agents/docs/`，用于需求澄清和方案推演；不是最终项目事实文档。

## 当前实现状态

状态：阶段 1 到 8 已完成第一版可运行骨架。

已实现：

- 核心领域模型：`ChangeEvent`、`ReconTask`、`ReconcilePolicy`、执行日志、告警日志、人工操作日志、规则命中记录。
- Java 规则接口：`ReconcileRule<L, R>`。
- 事件幂等：基于 `eventKey` 的内存账本实现。
- 任务幂等：基于 `eventKey + ruleCode` 的内存唯一约束。
- 规则匹配和任务创建应用服务。
- 任务扫描、乐观锁抢占和任务执行状态机。
- `preCheck` 不通过后系统置为 `RESOLVED`。
- 核对成功、重试等待、最终失败状态流转。
- 告警发送端口和内存 Lark 客户端。
- 告警失败补偿、执行异常补偿和事件处理中超时补偿。
- 人工处理：确认需要处理、确认不需要处理、重试、备注。
- 查询接口：任务、执行日志、告警日志、人工操作日志。
- REST 入口：事件消费、Job 触发、任务查询和人工处理。
- 核心单元测试：任务创建幂等、同业务键多事件、preCheck、重试和最终失败。

未实现：

- 真实数据库表结构和持久化 Repository。
- 真实 Kafka Consumer。
- 真实 Canal 消息格式适配。
- 真实 XXL-JOB handler。
- 真实 Lark webhook/client。
- Controller 层完整测试。
- 告警幂等和补偿场景的完整测试。
- 查询接口的分页、筛选和排序。

## 本版结论

- `UPDATE` 事件允许规则读取整行 `after` 数据。
- 事件幂等使用 `Canal destination + database + table + primaryKey + eventType + binlog position`。
- 重复事件不简单丢弃，而是通过事件账本表判断是否已完成、处理中、失败或可补偿。
- 规则由 Java 接口 `ReconcileRule<L, R>` 实现，第一阶段不做动态 DSL。
- 任务创建幂等键采用 `eventKey + ruleCode`。
- `ruleCode + bizType + bizKey` 作为任务业务聚合键和查询键，不作为任务创建唯一键。
- 核对任务由 XXL-JOB 定时扫描，按创建时间正序，每次扫描 100 条，使用乐观锁抢占。
- 告警使用 Lark，HTTP 调用失败即视为告警失败，告警需要幂等。
- `PROCESSING` 事件超时时间可配置。
- `RUNNING` 任务执行超时时间可配置。
- 告警补偿重试间隔和最大补偿次数可配置，例如重试间隔 `[60, 300, 900]`，最多 5 次。
- 告警成功后任务仍保持 `ALERTING`，人工确认需要处理后才进入 `PAUSED`。
- 第一版保存完整 `rawMessage`。

## 目标

- 消费 Canal 投递到 Kafka 的数据库变更消息。
- 将 Canal 原始消息转换为统一 `ChangeEvent`。
- 对事件做幂等去重，避免重复消费导致重复建任务。
- 使用 Java 编写的 `ReconcileRule` 匹配事件。
- 一条事件可以命中多条规则。
- 命中规则后幂等创建核对任务。
- 使用 XXL-JOB 定时扫描、抢占并执行核对任务。
- 支持规则级重试策略。
- 支持最终失败后的 Lark 告警。
- 支持告警失败补偿和执行异常补偿。
- 支持人工处理告警，并记录处理动作。
- 支持查询事件、任务、执行日志、告警日志和人工操作日志。

## 非目标

- 第一阶段不做动态规则 DSL。
- 第一阶段不支持多版本规则并存。
- 第一阶段不将规则配置完全托管给页面。
- 第一阶段不引入复杂工作流引擎。

## 总体流程

```text
业务数据库变更
  -> Binlog
  -> Canal 订阅
  -> Kafka
  -> reconService 消费 Kafka 消息
  -> Canal 原始消息转换为 ChangeEvent
  -> 事件幂等去重
  -> 匹配 ReconcileRule
  -> 幂等创建 ReconTask
  -> XXL-JOB 定时扫描任务
  -> 乐观锁抢占任务
  -> 执行 loadLeft / preCheck / loadRight / check
  -> 成功、重试、最终失败、告警、人工处理或归档
```

## ChangeEvent

`ChangeEvent` 是系统内部统一变更事件模型，用于屏蔽 Canal 原始消息结构。

建议字段：

- `eventKey`：事件幂等键。
- `destination`：Canal destination。
- `databaseName`：数据库名。
- `tableName`：表名。
- `eventType`：事件类型，例如 INSERT、UPDATE、DELETE。
- `primaryKey`：主键值。
- `before`：变更前数据。
- `after`：变更后数据。
- `changedFields`：发生变化的字段。
- `binlogFile`：Binlog 文件。
- `binlogPosition`：Binlog 位点。
- `eventTime`：业务变更时间或 Binlog 事件时间。
- `receiveTime`：系统接收时间。
- `rawMessage`：原始 Canal 消息，便于排查。

`UPDATE` 事件允许读取整行 `after` 数据，规则可以基于变更后完整记录判断是否命中。

## 事件幂等

同一条事件使用以下字段生成唯一键：

```text
Canal destination + database + table + primaryKey + eventType + binlog position
```

处理方式：

1. 消费 Kafka 消息后，先转换为 `ChangeEvent`。
2. 尝试向事件表插入 `eventKey`，事件表对 `eventKey` 建唯一索引。
3. 插入成功，说明是首次处理，进入规则匹配和任务创建。
4. 如果唯一键冲突：
   - 事件状态为 `PROCESSED`：直接跳过业务处理并确认 Kafka 消息。
   - 事件状态为 `PROCESSING` 且未超时：跳过或稍后重试，避免并发重复处理。
   - 事件状态为 `PROCESSING` 且已超时：允许补偿任务或当前消费者重新抢占处理。
   - 事件状态为 `FAILED`：允许重新处理。
5. 规则匹配和任务创建完成后，将事件状态置为 `PROCESSED`。

不建议对重复事件简单无条件丢弃。原因是可能出现“事件记录已写入，但任务尚未创建完成”的部分失败场景。事件幂等表应该同时承担消费进度账本职责。

事件状态：

- `INIT`：事件已落库，尚未开始处理。
- `PROCESSING`：事件处理中。
- `PROCESSED`：事件处理完成。
- `FAILED`：事件处理失败，等待重试或补偿。

建议事件表至少包含：

- `id`
- `eventKey`
- `destination`
- `databaseName`
- `tableName`
- `eventType`
- `primaryKey`
- `binlogFile`
- `binlogPosition`
- `status`
- `rawMessage`
- `errorMessage`
- `createdAt`
- `updatedAt`

第一版保存完整 `rawMessage`，优先保证排查能力。后续如果容量压力明显，再考虑只保留摘要、压缩内容或归档历史原始消息。

建议唯一索引：

```text
uk_change_event_key(event_key)
```

## 规则接口

规则由 Java 编写，编写者实现以下接口：

```java
public interface ReconcileRule<L, R> {

    /**
     * 规则编码，全局唯一。
     */
    String ruleCode();

    /**
     * 业务类型，例如 PAYMENT / REFUND / WITHDRAW / ORDER。
     */
    String bizType();

    /**
     * 是否匹配当前 Binlog 事件。
     * 这里应该尽量过滤掉不需要生成任务的事件。
     */
    boolean match(ChangeEvent event);

    /**
     * 从事件中提取业务唯一键。
     */
    String bizKey(ChangeEvent event);

    /**
     * 代码默认策略，仅用于初始化和兜底。
     * 真实执行时 DB 配置优先。
     */
    default ReconcilePolicy defaultPolicy() {
        return ReconcilePolicy.defaultPolicy();
    }

    /**
     * 加载左侧实体，通常是平台主单据。
     */
    L loadLeft(ReconcileContext ctx);

    /**
     * 回源后的前置检查。
     * 不通过时任务会被系统置为 RESOLVED，而不是进入失败重试。
     */
    default PreCheckResult preCheck(L left, ReconcileContext ctx) {
        return PreCheckResult.pass();
    }

    /**
     * 加载右侧实体，可以是 B 表、C 表、多表聚合、渠道 API。
     */
    R loadRight(L left, ReconcileContext ctx);

    /**
     * 核对逻辑。
     */
    CheckResult check(L left, R right, ReconcileContext ctx);
}
```

规则语义示例：

```text
当某张表的某个字段更新成某个值时，
断言另一张表或外部系统中的某个业务状态应该满足预期。
```

## 规则策略

重试策略是规则级别的，可以在数据库中自定义。代码中的 `defaultPolicy()` 只用于初始化和兜底。

建议策略字段：

- `ruleCode`：规则编码。
- `ruleName`：规则名称。
- `enabled`：是否启用。
- `maxAttempts`：最大执行次数。
- `retryIntervals`：重试间隔列表，单位建议为秒。
- `alertEnabled`：是否开启告警。
- `alertTemplateCode`：告警模板编码。

重试间隔示例：

```text
retryIntervals = [30, 60, 90]
```

含义：

- 第一次执行失败后，延后 30 秒执行。
- 第二次执行失败后，延后 60 秒执行。
- 第三次及以后执行失败后，延后 90 秒执行。

## ReconTask 创建

一条事件可以命中多条规则。每条命中的规则应创建一条核对任务。

已确认要求：

- 同一事件命中同一规则，只能创建一个任务。
- 一条业务主键在短时间内多次发生满足条件的变更，应该正常触发核对。
- 当前不支持多版本规则，只有一个版本规则。

任务创建幂等键：

```text
eventKey + ruleCode
```

原因：

- 它可以保证同一事件同一规则只创建一条任务。
- 它不会阻止同一业务主键在不同 Binlog 事件下多次创建任务。

任务业务聚合键：

```text
ruleCode + bizType + bizKey
```

该业务键用于查询、聚合、展示、人工排查和后续可能的合并策略，但不作为第一版任务创建唯一键。

建议唯一索引：

```text
uk_recon_task_event_rule(event_key, rule_code)
```

建议普通索引：

```text
idx_recon_task_biz(rule_code, biz_type, biz_key)
```

设计说明：

- 如果使用 `ruleCode + bizType + bizKey` 作为任务创建唯一键，会阻止同一业务主键、同一规则在不同 Binlog 事件下重复建任务。
- 因为需求要求“同一业务主键同一规则在短时间内多次变化，满足条件就要正常触发核对”，所以任务创建唯一键应包含事件维度。

## 任务状态机

任务状态：

- `INIT`：初始化。
- `RUNNING`：任务执行中。
- `RETRY_WAIT`：核对失败多次，但是没有超过最大次数，等待下一次定时任务拉起。
- `SUCCESS`：核对成功。
- `FAILED_FINAL`：核对失败多次，到达最大次数，等待告警任务拉起。
- `ALERTING`：告警中；告警发送成功后也保持该状态。
- `PAUSED`：告警处理中，人工排查中。
- `RESOLVED`：人工确认后标记告警不需要处理，或系统确认无需核对。

状态流转：

```text
INIT
  -> RUNNING

RUNNING
  -> RESOLVED    preCheck 不通过，系统确认无需核对
  -> SUCCESS     核对一致
  -> RETRY_WAIT  核对不一致，未达到最大次数
  -> FAILED_FINAL 核对不一致，达到最大次数

RETRY_WAIT
  -> RUNNING     next_retry_time 到期后被 XXL-JOB 拉起

FAILED_FINAL
  -> ALERTING    告警任务拉起

ALERTING
  -> PAUSED      人工确认需要处理，进入排查中
  -> RESOLVED    人工确认不需要处理，是噪音

PAUSED
  -> RESOLVED    人工确认不需要处理或问题已处理
  -> RETRY_WAIT  人工触发 RETRY

RETRY_WAIT
  -> RUNNING
```

说明：

- `preCheck` 的含义是加载需要核对的数据后，发现数据并不需要核对。
- `preCheck` 不通过时任务由系统置为 `RESOLVED`，不进入失败重试。
- 不额外引入更复杂的状态。
- 不一致表示核对执行了，但核对断言没有通过。
- 未就绪和异常不引入独立状态，也不等同于核对断言不通过。
- 未就绪和异常发生时，任务应恢复到本次执行前的可调度状态，并等待下一次定时任务重试；如果服务中断导致停留在 `RUNNING`，由执行异常补偿任务恢复。

## 任务执行

XXL-JOB 定时扫描任务并抢占执行。

扫描规则：

- 每次扫描 100 条任务。
- 按任务创建时间正序。
- 使用乐观锁抢占任务。
- 多个 XXL-JOB 分片通过任务 ID 取模过滤。

建议分片 SQL 条件：

```text
id % shardTotal = shardIndex
```

分片示例：

```text
shardTotal = 2，shardIndex = 0，拉取偶数 ID。
shardTotal = 2，shardIndex = 1，拉取奇数 ID。
```

并发控制：

- 任务执行允许多个调度器并发扫描。
- 具体任务执行通过 CAS 乐观锁拦截重复抢占。

建议任务字段：

- `id`
- `eventKey`
- `ruleCode`
- `ruleName`
- `bizType`
- `bizKey`
- `status`
- `attemptCount`
- `maxAttempts`
- `nextRetryTime`
- `lastRunTime`
- `lastErrorCode`
- `lastErrorMessage`
- `lastCheckResult`
- `lockedBy`
- `lockedAt`
- `previousStatus`
- `createdAt`
- `updatedAt`
- `version`

`RUNNING` 任务执行超时时间可配置。任务长时间停留在 `RUNNING` 且超过配置阈值时，由执行异常补偿任务恢复。

## 重试策略

重试策略由规则配置决定，DB 配置优先于代码默认配置。

`nextRetryTime` 通过当前执行次数和配置决定。

人工 `RETRY` 不重置 `attemptCount`。

建议规则：

- 执行失败后递增 `attemptCount`。
- 未达到最大次数，进入 `RETRY_WAIT` 并计算 `nextRetryTime`。
- 达到最大次数，进入 `FAILED_FINAL`。
- 人工 `RETRY` 更新 `nextRetryTime`，状态进入 `RETRY_WAIT`。

## 告警

任务达到最大执行次数后进入 `FAILED_FINAL`，由告警任务拉起发送 Lark 告警。

`ALERTING` 表示任务已经进入告警流程。告警发送成功后仍保持 `ALERTING`，等待人工判断是否需要处理。如果人工确认需要处理，任务进入 `PAUSED`；如果人工确认不需要处理，任务进入 `RESOLVED`。

告警模板：

```text
规则编码：PAY_ORDER_AMOUNT_CHECK
规则名称：支付订单金额一致性核对
任务ID：123456789
业务主键：payOrderId=202605060001
渠道：SCB
商户：M123456
用户ID：2088xxxx
失败原因：左右表，哪个字段不一致
首次触发时间：2026-05-06 10:00:01
最后重试时间：2026-05-06 10:05:33
```

告警失败判断：

- HTTP 调用失败即视为告警失败。

告警幂等：

- 告警需要幂等，避免重复刷屏。
- 建议使用 `taskId + alertType` 作为告警幂等键。
- 告警请求前先记录或抢占告警日志。
- 已成功发送的告警不重复发送。

告警补偿策略：

- 告警补偿重试间隔可配置。
- 告警补偿最大次数可配置。
- 示例配置：重试间隔 `[60, 300, 900]`，最多 5 次。

建议告警日志字段：

- `id`
- `taskId`
- `ruleCode`
- `alertType`
- `alertKey`
- `status`
- `requestPayload`
- `responsePayload`
- `errorMessage`
- `attemptCount`
- `lastAttemptTime`
- `createdAt`
- `updatedAt`

## 补偿任务

需要两类补偿任务：

### 告警失败补偿

用于恢复告警 HTTP 调用失败、超时或进程异常导致的告警中断。

建议扫描范围：

- 告警日志状态为失败或处理中超时。
- 任务状态为 `FAILED_FINAL` 或 `ALERTING`，但没有成功告警记录。

告警日志处理中超时时间和补偿策略均可配置。

### 执行异常补偿

用于恢复任务执行过程中服务重启、线程中断、数据库异常等导致的异常状态。

建议扫描范围：

- `RUNNING` 状态且 `updatedAt` 超过执行超时时间。
- `ALERTING` 状态且告警日志长时间未完成。
- 事件状态为 `PROCESSING` 且超过处理超时时间。

`PROCESSING` 事件超时时间可配置。`RUNNING` 任务执行超时时间可配置。

补偿原则：

- 补偿任务也必须使用乐观锁或唯一键，避免重复补偿。
- 补偿任务只恢复可判断的中间态，不直接吞掉错误。
- 补偿动作应记录日志，便于审计。

## 人工处理

人工处理主要面向已经告警的任务。

建议动作：

- `MARK_NEED_HANDLE`：确认需要处理，任务进入 `PAUSED`。
- `MARK_NO_NEED_HANDLE`：确认不需要处理，是噪音，任务进入 `RESOLVED`。
- `RETRY`：需要重试，更新下一次重试时间，任务进入 `RETRY_WAIT`。
- `ADD_NOTE`：追加备注，不改变任务状态。

建议人工操作日志字段：

- `id`
- `taskId`
- `action`
- `operator`
- `reason`
- `remark`
- `fromStatus`
- `toStatus`
- `createdAt`

## 查询与审计

建议保留以下记录，方便问题排查和后续运营：

- `ChangeEvent`：记录已消费事件、事件状态和原始消息。
- `RuleMatch`：记录事件命中的规则，便于解释为什么创建任务。
- `ReconTask`：记录核对任务主状态。
- `ReconExecutionLog`：记录每次执行的输入、结果、错误和耗时。
- `AlertLog`：记录告警请求、响应、失败原因和幂等状态。
- `ManualActionLog`：记录人工处理动作。

第一版可以优先实现 `ChangeEvent`、`ReconTask`、`ReconExecutionLog` 和 `AlertLog`，人工操作日志可以跟随人工处理接口一起实现。

## 待确认问题

暂无。后续进入技术方案阶段时，需要设计这些配置项的配置来源、默认值和数据库表结构。
