# 设计决策草案：基于 Canal + Kafka 的准实时核对系统

## 已确认决策

- `UPDATE` 事件允许读取整行 `after` 数据。
- 事件幂等键使用 `Canal destination + database + table + primaryKey + eventType + binlog position`。
- 重复事件不简单丢弃，使用事件账本表判断处理状态。
- 规则使用 Java 接口 `ReconcileRule<L, R>`，第一阶段不做动态 DSL。
- 任务创建幂等键使用 `eventKey + ruleCode`。
- `ruleCode + bizType + bizKey` 作为业务聚合键和查询键。
- 当前不支持多版本规则并存。
- `PROCESSING` 事件超时时间可配置。
- `RUNNING` 任务执行超时时间可配置。
- 告警补偿重试间隔和最大次数可配置。
- 告警成功后任务保持 `ALERTING`，人工确认需要处理后进入 `PAUSED`。
- 第一版保存完整 `rawMessage`。

## 已同步到项目事实文档

- 任务创建幂等键使用 `eventKey + ruleCode`。
- `ruleCode + bizType + bizKey` 作为业务聚合键。
- 第一版保存完整 `rawMessage`。
- 告警成功后任务保持 `ALERTING`。

对应文档：

- `docs/project/decision-log.md`
