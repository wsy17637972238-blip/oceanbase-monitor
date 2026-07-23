# OceanBase 智能巡检系统 Demo

基于 OceanBase CE + Spring Boot 的数据库智能巡检系统演示项目。

## 技术栈

- **数据库**：OceanBase CE 4.4（Docker，MySQL 租户模式）
- **后端**：Spring Boot 2.7 + JdbcTemplate（JDK 16）
- **前端**：原生 HTML/JS（Spring Boot 静态页面，无需单独部署）

## 项目结构

```
ObDemo/
├── pom.xml                                # Maven 配置
└── src/main/
    ├── java/com/example/obinspection/
    │   ├── ObInspectionApplication.java   # 启动类
    │   ├── domain/
    │   │   ├── model/                     # 领域实体（7 张表 + Metric 值对象）
    │   │   ├── model/enums/               # 状态枚举
    │   │   ├── repository/                # 仓储接口（领域层，只定义接口）
    │   │   └── service/IdGenerator.java   # 主键生成器（应用层生成 ID）
    │   └── web/DemoController.java        # Demo 查询接口 /api/rules、/api/configs
    └── resources/
        ├── schema.sql                     # 建表 DDL（7 张表，含反范式说明注释）
        ├── data.sql                       # 初始化数据（规则配置 + 系统配置）
        ├── application.yml                # 数据源配置
        └── static/index.html              # 前端展示页
```

## 数据库表

| 表 | 说明 |
|---|---|
| inspection_task | 巡检任务主表 |
| inspection_result | 巡检结果明细表 |
| inspection_alert | 告警记录表 |
| ai_diagnosis | AI 智能诊断结果表 |
| inspection_rule_config | 巡检规则配置表 |
| alert_notification | 告警通知记录表 |
| system_config | 系统配置表 |

## 快速开始

### 1. 启动 OceanBase

```bash
docker start obce
# 等待 1~2 分钟，看到 "boot success!" 表示就绪
docker logs obce --tail 5
```

### 2. 初始化数据库（首次）

```bash
mysql -h127.0.0.1 -P2881 -uroot@test --default-character-set=utf8mb4 \
  -e "CREATE DATABASE IF NOT EXISTS obinspection_check;"
mysql -h127.0.0.1 -P2881 -uroot@test --default-character-set=utf8mb4 \
  obinspection_check < src/main/resources/schema.sql
mysql -h127.0.0.1 -P2881 -uroot@test --default-character-set=utf8mb4 \
  obinspection_check < src/main/resources/data.sql
```

> 注意：客户端必须指定 `utf8mb4`，否则中文数据会报 Incorrect string value。

### 3. 构建并启动后端

```bash
# 打包（Maven 绿色版位于 ../tools/，使用阿里云镜像加速）
../tools/apache-maven-3.9.9/bin/mvn -s ../tools/settings.xml package -DskipTests

# 运行
java -jar target/ob-inspection-demo-0.0.1-SNAPSHOT.jar
```

### 4. 访问页面

浏览器打开 http://localhost:8080 ，可查看巡检规则配置与系统配置（数据实时来自 OceanBase）。

### 停止

- 后端：终端按 `Ctrl + C`
- 数据库：`docker stop obce`

## 设计说明

- 主键由应用层 `IdGenerator` 生成，数据库不使用自增列（兼容 Oracle 租户）
- 日期默认值统一 `CURRENT_TIMESTAMP`，不使用 `NOW()`/`SYSDATE`
- 外键仅逻辑约束，不建物理外键
- 部分字段为反范式设计（空间换时间），理由见 `schema.sql` 与实体类注释
