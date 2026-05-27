# 测试用例草案：基于 Canal + Kafka 的准实时核对系统

## 当前覆盖状态

已新增核心单元测试：

- `ReconcilePolicyTests`
- `ChangeEventApplicationServiceTests`
- `ReconTaskExecutionServiceTests`

当前测试结果：

```text
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

## 已覆盖场景

- 重试次数超过配置长度时，使用最后一个重试间隔。
- 一条事件命中多条规则，创建多条任务。
- 同一事件重复消费，不重复创建任务。
- 同一业务键但不同事件，可以创建多条任务。
- `preCheck` 不通过时任务进入 `RESOLVED`。
- 核对不一致且未达最大次数时进入 `RETRY_WAIT`。
- 核对不一致且达到最大次数时进入 `FAILED_FINAL`。

## 待补充单元测试

- `ChangeEvent` 解析真实 Canal 消息。
- 事件 `PROCESSING` 未超时和已超时处理。
- 事件 `FAILED` 后重新处理并创建缺失任务。
- 规则不匹配时不创建任务。
- 规则禁用后的任务创建行为。
- `NOT_READY` 结果恢复到执行前状态。
- 执行异常恢复到执行前状态。
- XXL-JOB 分片扫描条件。
- 乐观锁并发抢占只有一个成功。
- 告警成功后任务保持 `ALERTING`。
- 告警幂等避免重复发送。
- 告警失败后按配置重试。
- 告警补偿达到最大次数后的行为。
- `RUNNING` 超时补偿。
- 人工确认需要处理：`ALERTING -> PAUSED`。
- 人工确认不需要处理：`ALERTING/PAUSED -> RESOLVED`。
- 人工重试不重置 `attemptCount`。

## 待补充 Controller 测试

- `POST /api/recon/events/canal` 参数校验。
- `POST /api/recon/events/canal` 成功创建任务。
- `POST /api/recon/jobs/tasks` 触发任务扫描。
- `POST /api/recon/jobs/alerts` 触发告警扫描。
- 人工处理接口的合法状态和非法状态。
- 查询接口响应结构。

## 待补充集成测试

- 使用真实数据库验证唯一索引和状态更新。
- 使用测试 Kafka 验证消费幂等。
- 使用模拟 Lark HTTP 服务验证告警重试和幂等。
- 使用 XXL-JOB handler 或替身验证分片参数传递。
