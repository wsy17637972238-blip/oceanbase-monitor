# OceanBase 智能巡检系统 — 项目设计与进度总结

> 生成时间：2026-07-23 | 更新：2026-07-24（阶段2完成；多实例纳管 + MetricNames 常量化架构改造）
> 项目路径：`D:/programWork/zyb/ob-inspection`

---

## 一、项目背景与目的

OceanBase 数据库运维中需要定期巡检健康状况（慢 SQL、活跃会话、合并状态等）。传统方式是 DBA 手动查系统视图，效率低、易遗漏、发现问题后依赖个人经验分析根因。

本项目构建一条 **定时采集 → 规则判断 → 告警 → AI 诊断 → 看板展示** 的自动化巡检链路：

1. **采集**：JDBC 直连 OceanBase 采集指标（预留 OCP 云管控平台方式）
2. **规则巡检**：可配置规则（阈值存库）对指标做判断
3. **告警**：异常结果生成告警事件，支持确认（ack）、收敛、通知
4. **AI 诊断**：告警触发后异步调用 DeepSeek，输出根因、风险等级、处理建议

**系统的智能分层：规则负责"发现"，AI 负责"理解"，人负责"决策"。**

## 二、最终形态：看板为入口，以事件为单位

```
看板（Dashboard）              ← 入口总览：当前健不健康
 ├── 巡检事件（InspectionTask）      ← 每次巡检执行 = 一个事件（有生命周期）
 │    └── 巡检项结果（InspectionResult） ← 事件明细（每条规则一行）
 └── 告警事件（Alert）               ← 每个异常 = 一个事件（PENDING → ACKED）
      └── AI 诊断（AiDiagnosis）     ← 挂在告警事件上
```

三个前端页面分工：

| 页面 | 职责 | 回答的问题 |
|---|---|---|
| 看板 `/dashboard` | 全局汇总 + 趋势图 | 现在有没有事 |
| 巡检 `/inspection` | 任务列表 + 明细（追溯视角） | 历次检查做了什么 |
| 告警 `/alerts` | 告警待办 + ack + AI 诊断（处理视角） | 有哪些问题要处理、怎么处理 |

巡检侧与告警侧分开的原因：巡检是"查账"（翻历史），告警是"干活"（待办清单），一次任务产生 0~N 条告警。

## 三、技术架构

- **后端**：Spring Boot 3.2.5 + Spring JDBC（HikariCP）+ H2（业务库）+ oceanbase-client 2.4.9 + springdoc-openapi（Swagger）
- **前端**：Vue3 + Vite + Element Plus + ECharts 5 + Axios（`/api` 代理到 8080）
- **架构**：DDD 四层，铁律：domain 零 Spring 依赖；domain 只定义接口由 infrastructure 实现；Controller 必经 AppService；主键由 Java 生成（IdGenerator）

```
interfaces (Controller) → application (AppService) → domain (接口/规则/模型)
                                                    ← infrastructure (H2仓储/采集器/AI/通知)
```

双数据源：H2（`jdbc:h2:file:./data/obinspection`，@Primary）存业务数据（含被巡检实例纳管表 `inspection_instance`）；OceanBase 被巡检实例不写死在配置，由 `ObInstanceConnectionManager` 按实例动态建 HikariCP 连接池（懒建、缓存复用、失败按实例容错），所有指标 tags 带 `instance_id` / `instance_name`。

指标名与标签键统一走 `domain.collector.MetricNames` 常量类，采集器产出与规则消费双侧对齐，禁止字符串字面量。

## 四、数据库设计（H2，8 张表）

| 表 | 用途 |
|---|---|
| `inspection_task` | 巡检任务（事件） |
| `inspection_result` | 每条规则的检查结果 |
| `inspection_alert` | 告警事件 |
| `ai_diagnosis` | AI 诊断结果 |
| `inspection_rule_config` | 规则配置（含阈值，已初始化 3 条规则） |
| `alert_notification` | 通知记录 |
| `system_config` | 系统配置（采集器类型、收敛时间、保留天数等） |
| `inspection_instance` | 被巡检实例纳管（连接信息；密码明文仅限演示，生产需加密 + 只读最小权限账号） |

## 五、核心设计：采集什么、怎么判断告警

> 以下视图与 SQL 已在本地 oceanbase-ce 容器（obce）上实测验证。

### 5.1 统一告警判断模型

- **数值型指标**：`value >= critical阈值` → CRITICAL；`>= warn阈值` → WARN；否则 OK
- **状态型指标**：不等于期望值 → 按严重度表判定（如合并状态：`IS_ERROR='YES'` → CRITICAL；`STATUS != 'IDLE'` → WARN）
- `InspectionResult.status` ∈ {OK, WARN, CRITICAL}
- **AlertGenerator**：仅 WARN/CRITICAL 结果生成 Alert，级别直接映射（INFO 留给通知类事件）
- **任务总体状态** = 所有结果中最严重级别
- **收敛**：同 `item_name` 在 `alert.convergence.minutes`（默认 5 分钟）内已有 PENDING 告警 → 不重复生成

### 5.2 核心三指标（两种部署场景通用）

| 指标 | 数据源视图 | 采集 SQL 要点 | 阈值（warn/critical） |
|---|---|---|---|
| 慢 SQL 数 | `oceanbase.GV$OB_SQL_AUDIT` | `ELAPSED_TIME > 1000000`（微秒）且 `REQUEST_TIME` 在最近 1 小时内 | 5 / 20 |
| 活跃会话数 | `oceanbase.GV$OB_PROCESSLIST` | `COMMAND <> 'Sleep'` | 50 / 100 |
| 合并状态 | `CDB_OB_MAJOR_COMPACTION`（sys）/ `DBA_OB_MAJOR_COMPACTION`（租户） | `STATUS`、`IS_ERROR`、`IS_SUSPENDED` 列 | 非 IDLE 告警 |

实测结果（2026-07-23，obce 容器）：慢 SQL 1 条、活跃会话 1、3 个租户合并状态全部 `IDLE/NO`。

### 5.3 场景 A：本地社区版（root@sys，sys 租户）

sys 租户可见全部集群级视图，除核心三指标外可加：

| 指标 | 视图 | 说明 |
|---|---|---|
| 数据磁盘使用率 | `GV$OB_SERVERS` | `DATA_DISK_IN_USE / DATA_DISK_CAPACITY`，warn 70% / critical 90% |
| 日志盘使用率 | `GV$OB_SERVERS` | `LOG_DISK_IN_USE / LOG_DISK_CAPACITY` |
| 服务器健康 | `GV$OB_SERVERS` | `DATA_DISK_HEALTH_STATUS` |
| Unit 状态 | `GV$OB_UNITS` | `STATUS != 'NORMAL'`，含 `MAX_CPU`、`MEMORY_SIZE` 等资源分配 |

`collector_type = jdbc`。本地演示环境阈值可调低，方便造数据演示。

### 5.4 场景 B：企业版单业务租户（无 sys 权限）

实测业务租户（root@test）可见性：

- **可见**：`GV$OB_SQL_AUDIT`（仅本租户数据）、`GV$OB_PROCESSLIST`（仅本会话）、`DBA_OB_MAJOR_COMPACTION`、`GV$OB_UNITS`
- **不可见**：`CDB_OB_*`、`GV$OB_SERVERS`、`DBA_OB_SERVERS`

结论：

1. 核心三指标 SQL **无需修改**，视图自动只返回本租户数据（合并状态换用 `DBA_` 前缀视图）
2. 主机级指标（机器 CPU、磁盘、IO）SQL 层拿不到 → 企业版通常配套 **OCP**，用其 REST API 采集（`collector_type = ocp` 的预留用途，用已配置的 RestTemplate 调用）
3. 生产环境阈值更严格，告警级别映射通知渠道（WARN 只看板，CRITICAL 推钉钉/邮件）

### 5.5 AI 诊断定位

AI **不参与"要不要告警"**（阈值判断必须快、稳、确定），只负责**告警生成后**的根因分析与处理建议：

```
规则判断异常 → 生成告警 → 通知运维
                          └→ AI 诊断（@Async 异步，不阻塞告警主流程）
                             输入：告警内容 + 相关指标
                             输出：root_cause / suggestions / risk_level
```

## 六、当前进度

### 已完成

- [x] 项目骨架（62 个 Java 文件 + 前端骨架），编译通过、可启动、Swagger 可访问
- [x] 双数据源连通（H2 + OceanBase Docker 容器 obce）
- [x] 阶段 1：H2 仓储读链路 —— `H2InspectionRuleConfigRepository` / `H2SystemConfigRepository` 真实实现，`GET /api/inspection/rules` 返回 3 条规则配置
- [x] 阶段 2：`JdbcMetricsCollector` 采集器 —— 7 类指标（慢 SQL/活跃会话/合并状态/节点状态/节点资源/MemStore/参数基线）实测采集 19 项，启动日志打印真实数字；单指标失败容错不中断整体
- [x] 架构改造（2026-07-24）：多实例纳管 —— `inspection_instance` 表 + `ObInstanceConnectionManager` 按实例动态建池，采集循环遍历 enabled 实例，指标带 `instance_id`/`instance_name` 标签
- [x] 架构改造（2026-07-24）：指标名常量化 —— `domain.collector.MetricNames`（含标签键），采集器全部改用常量
- [x] 修复：OB 密码配置（本地 oceanbase-ce 镜像 root@sys 为空密码）
- [x] 修复：初始化脚本编码（Windows 默认 GBK 读 UTF-8 的 data.sql 导致中文乱码，已改为显式 UTF-8）
- [x] 修复：IdGenerator 重启主键冲突隐患（AtomicLong 改为简化雪花算法：毫秒时间戳 << 12 | 毫秒内序列，纯 POJO 无依赖）

### 待完成（实施路线）

| 阶段 | 内容 | 验收标准 |
|---|---|---|
| 3 | 三条规则判断 + 巡检主流程（**核心闭环**） | trigger 后 tasks/results 接口有真实数据 |
| 4 | 告警链路（生成、通知、ack、收敛） | 阈值调低可触发告警并确认 |
| 5 | AI 诊断（DeepSeek 真实调用） | 告警诊断返回根因与建议 |
| 6 | 前端三页面（告警页 → 巡检页 → 看板） | 图表表格展示真实数据 |
| 7 | 巡检报告导出（健康评分 + PDF/HTML 正式报告） | 按任务导出报告 |

原则：**先让数据流起来（2✅→3），再让系统聪明起来（4→5），最后让它好看（6→7）。**

## 七、改进清单（面向实习答辩）

1. ~~IdGenerator 重启冲突~~（已修复）
2. 准备现场演示剧本：手工造慢 SQL → 触发巡检 → 看板变红 → 告警 → AI 建议
3. 规则引擎补单元测试（纯逻辑，最适合写测试）
4. 告警收敛逻辑落地（`alert.convergence.minutes` 已有配置位）
5. 前端看板打磨（ECharts 趋势图 + 状态卡片，决定观感）
6. README 补架构图 + docker-compose 一键起环境

## 八、运行方式

```bash
# 后端（需 JDK 17，便携版在 tools/jdk-17.0.2）
export JAVA_HOME="D:\programWork\zyb\tools\jdk-17.0.2"
cd ob-inspection
mvn -s ../tools/settings.xml spring-boot:run

# 前端
cd ob-inspection/frontend
npm install && npm run dev
```

| 地址 | 说明 |
|---|---|
| http://localhost:5173 | 前端页面 |
| http://localhost:8080/swagger-ui.html | Swagger 接口文档（按 巡检管理/告警管理/看板 三组展示） |
| http://localhost:8080/h2-console | H2 控制台（JDBC URL：`jdbc:h2:file:./data/obinspection`，用户 sa，空密码） |
