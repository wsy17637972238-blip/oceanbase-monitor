# OceanBase 智能巡检系统（ob-inspection）

面向 OceanBase 国产数据库的智能巡检平台：定时/手动**采集**关键指标 → **规则引擎**阈值判定 → **告警**（收敛/确认/通知）→ **AI 诊断**（DeepSeek 根因分析，可降级）→ **健康评分**与**正式巡检报告**导出，并配有 Vue3 可视化看板。

## 功能特性

- **多实例纳管**：被巡检实例注册在 `inspection_instance` 表，采集器按启用实例循环采集，单实例故障不影响整体
- **指标采集**：覆盖可用性 / 资源水位 / 性能 / 稳定性 / 合规 5 类 19 项指标（基于 OB 4.4 实测视图），单指标失败容错不中断
- **规则引擎**：慢 SQL、活跃会话、合并状态等巡检规则，阈值存库可热调整；判定依据逐条可追溯
- **告警链路**：异常结果自动生成告警，支持确认（ack）、时间窗收敛（同巡检项 5 分钟内不重复告警）、通知记录
- **AI 诊断**：对告警异步调用 DeepSeek 输出根因/建议/风险等级；幂等防重复调用，失败自动降级不影响主流程
- **巡检报告**：按任务生成健康评分（扣分规则可解释）+ 中文正式报告（HTML 单文件下载，浏览器可打印为 PDF），异常项附整改建议与 AI 辅助分析
- **可视化看板**：健康状态总览、告警级别分布、巡检任务与明细、告警处理一站式操作

## 技术栈

- 后端：Java 17、Spring Boot 3.2.5、spring-jdbc（HikariCP）、H2（系统库）+ oceanbase-client 2.4.9（被巡检库）、springdoc-openapi（Swagger）、Maven
- 前端：Vue 3、Vite 5、vue-router 4、Element Plus、ECharts 5、Axios
- AI：DeepSeek Chat Completions API（可开关、可降级）
- 测试：JUnit 5 + Spring Boot Test（43 个单测，规则/评分/收敛/解析等纯逻辑全覆盖）

## 系统架构

```
┌──────────────────────────────────────────────┐
│        前端 (Vue3 + Element Plus + ECharts)    │
│        看板 / 巡检任务 / 告警处理 / 报告下载      │
└──────────────────────────────────────────────┘
                      ↑ HTTP /api
┌──────────────────────────────────────────────┐
│             后端 (Spring Boot, DDD)            │
│  巡检主流程：触发 → 采集 → 规则判定 → 结果入库    │
│  告警引擎：生成 → 收敛 → 通知 → 确认             │
│  AI 诊断：异步调用 → 结构化解析 → 可降级          │
│  报告：健康评分 → 整改建议 → HTML 导出           │
└──────────────────────────────────────────────┘
        ↑ JDBC（实例纳管表驱动，每实例独立连接池）
┌──────────────────────┐    ┌──────────────────┐
│  OceanBase 集群(被巡检) │    │  H2（系统元数据库） │
│  sys 租户系统视图       │    │  任务/结果/告警/配置 │
└──────────────────────┘    └──────────────────┘
```

> 单链路设计：巡检任务触发时实时采集，指标快照作为该次任务的输入；规则负责"发现"，AI 负责"理解"，人负责"决策"。

## 项目结构（DDD 四层）

```
ob-inspection/
├── src/main/java/com/example/obinspection/
│   ├── application/        # 应用层：AppService / DTO / Assembler
│   ├── domain/             # 领域层：模型、规则、采集器与仓储接口、领域服务（纯 POJO，零 Spring）
│   │   ├── collector/      #   MetricsCollector 接口 + MetricNames 指标常量
│   │   ├── rule/           #   InspectionRule + 慢SQL/活跃会话/合并状态实现
│   │   └── service/        #   AlertGenerator / HealthScoreCalculator / ...
│   ├── infrastructure/     # 基础设施层：JDBC 采集器、实例连接管理、H2 仓储、DeepSeek 客户端
│   ├── interfaces/         # 接口层：Controller + 全局异常处理
│   └── ObInspectionApplication.java
├── src/main/resources/
│   ├── application.yml
│   ├── schema.sql          # 8 张表 DDL（CREATE TABLE IF NOT EXISTS）
│   └── data.sql            # 规则配置 / 系统配置 / 实例种子数据
├── src/test/               # 43 个单元测试
└── frontend/               # Vue3 + Vite 前端
    └── src/{api,router,views,utils}
```

架构铁律：领域层零 Spring 依赖；接口在领域层、实现在基础设施层；Controller 只能走 AppService；主键由 Java 雪花算法生成。

## 快速开始

### 前置要求

- JDK 17+、Maven 3.6+、Node 18+、Docker

### 1. 启动 OceanBase（社区版）

```bash
docker run -d --name obce -p 2881:2881 \
  -e MODE=slim -e OB_SYS_PASSWORD=root \
  oceanbase/oceanbase-ce:latest
```

启动后在 `src/main/resources/data.sql` 的 `inspection_instance` 表中登记实例（已含本地 obce 种子数据：`root@sys`，空密码；多实例直接插行即可）。

### 2. 配置 AI（可选）

```bash
export DEEPSEEK_API_KEY=sk-xxxx   # 不配置则 AI 诊断自动降级，其余功能不受影响
# export DEEPSEEK_ENABLED=false   # 也可直接关闭 AI 模块
```

### 3. 启动后端

```bash
mvn spring-boot:run        # 端口 8080
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev                # 端口 5173，/api 代理到 8080
```

## 使用指南

1. 打开 http://localhost:5173 → 看板查看当前健康状态与告警分布
2. 「巡检」页点击**手动触发巡检** → 展开任务查看每条巡检项的判定明细与依据
3. 「告警」页处理异常：**确认**（填确认人）或 **AI 诊断**（异步生成根因与建议）
4. 「巡检」页对任务点**查看/下载报告**：健康评分 + 判定明细 + 整改建议的正式报告，浏览器 Ctrl+P 可转 PDF

## API 概览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | /api/inspection/trigger | 手动触发巡检（异步执行） |
| GET | /api/inspection/tasks | 巡检任务列表 |
| GET | /api/inspection/tasks/{taskId}/results | 任务巡检明细（含判定依据） |
| GET | /api/inspection/tasks/{taskId}/report | 巡检报告（JSON，含健康评分） |
| GET | /api/inspection/tasks/{taskId}/report/download | 下载正式巡检报告（HTML） |
| GET | /api/inspection/rules | 巡检规则配置 |
| GET | /api/alerts?status=&level= | 告警列表（可过滤） |
| POST | /api/alerts/{id}/ack | 确认告警 |
| POST | /api/alerts/{id}/diagnose | 触发 AI 诊断（异步） |
| GET | /api/alerts/{id}/diagnosis | 查询 AI 诊断结果 |
| GET | /api/dashboard/summary | 看板汇总 |

- Swagger UI：http://localhost:8080/swagger-ui.html
- H2 控制台：http://localhost:8080/h2-console（JDBC URL：`jdbc:h2:file:./data/obinspection`，用户 `sa`，空密码）

## 巡检指标（基于 OB 4.4 实测视图）

| 分类 | 指标 | 采集视图 |
| --- | --- | --- |
| 可用性 | 节点状态/心跳 | `DBA_OB_SERVERS` |
| 资源水位 | CPU/内存/数据盘/日志盘 | `GV$OB_SERVERS` |
| 资源水位 | Memstore 使用率（按租户） | `GV$OB_MEMSTORE` |
| 性能 | 慢 SQL 数（1h 窗口） | `GV$OB_SQL_AUDIT` |
| 性能 | 活跃会话数 | `GV$OB_PROCESSLIST` |
| 稳定性 | 合并状态（按租户） | `CDB_OB_MAJOR_COMPACTION` |
| 合规 | 参数基线（6 项关键参数） | `GV$OB_PARAMETERS` |

> 注：`__all_zone` / `__all_server` 等为 OB 3.x 视图，4.x 已废弃；指标名统一由 `MetricNames` 常量类管理，采集器与规则两侧强一致。

## 健康评分规则

基准 100 分，按巡检结果逐项扣分：WARN 项 -10 / CRITICAL 项 -30，下限 0 分。报告附逐条扣分明细（如「WARN 项 slow_sql.count@obce 扣 10 分」），评分完全可解释。

## 关键设计决策

- **多实例维度前置**：实例连接信息存表驱动，指标 tag 必带 `instance_id`，告警收敛键天然隔离实例
- **AI 可降级**：AI 仅做"解读"不做"判定"；无 key / 超时 / 解析失败均落 FAILED 记录，主流程零依赖
- **告警收敛**：同巡检项窗口期内不重复告警，锚定首次告警时间，复用字段刷新内容
- **采集容错**：实例级失败跳过整实例、指标级失败跳过该指标，巡检系统自身不挂是第一要务

## 测试

```bash
mvn clean test     # 43 个单测：规则判定 / 评分 / 收敛 / 解析 / 建议文案
```

## 安全说明

- DeepSeek API Key 只走环境变量，不落盘、不进仓库
- `inspection_instance` 密码当前明文仅用于本地演示，生产环境必须加密存储并使用只读最小权限巡检账号

## 文档

- 项目总结与设计细节：`docs/PROJECT_SUMMARY.md`
