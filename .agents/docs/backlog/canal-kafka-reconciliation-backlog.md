# 待办：基于 Canal + Kafka 的准实时核对系统

## 下一阶段优先级

1. 设计数据库表结构和索引。
2. 实现数据库持久化 Repository。
3. 接入真实 Kafka Consumer。
4. 校准真实 Canal 消息格式。
5. 接入真实 XXL-JOB handler。
6. 接入真实 Lark webhook/client。
7. 补充 Controller 测试和补偿测试。
8. 将确认后的需求和实现文档提升到 `docs/`。

## 技术债

- 当前仓储为内存实现，服务重启后数据丢失。
- 当前 Lark 客户端为内存成功返回，不会真实发送告警。
- 当前 Job 入口为普通 Spring 组件和 REST 触发，尚未接入 XXL-JOB。
- 当前事件解析器只支持简化 JSON 结构，尚未校准真实 Canal 消息。
- 当前查询接口没有分页、排序和复杂筛选。
