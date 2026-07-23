# OceanBase 智能巡检系统（ob-inspection）

基于指标采集 + 规则引擎 + AI 诊断的 OceanBase 智能巡检系统。当前为**项目骨架（Scaffold）**：分层结构、接口契约、DDL 与空方法已就绪，业务逻辑全部以 `// TODO` 标注，待后续实现。

## 技术栈

- 后端：Java 17、Spring Boot 3.2.5、spring-jdbc（HikariCP）、H2（系统库）+ oceanbase-client 2.4.9（被巡检库）、springdoc-openapi 2.3.0（Swagger）、Maven
- 前端：Vue 3、Vite 5、vue-router 4、Element Plus、ECharts 5、Axios

## 目录结构

```
ob-inspection/
├── pom.xml
├── src/main/java/com/example/obinspection/
│   ├── application/        # 应用层：AppService / DTO / Assembler
│   ├── domain/             # 领域层：model、规则、采集器与仓储接口、领域服务、事件（纯 POJO，无 Spring 依赖）
│   ├── infrastructure/     # 基础设施层：JDBC 采集器、H2 仓储实现、DeepSeek 客户端、通知器、配置
│   ├── interfaces/         # 接口层：Controller + 全局异常处理
│   └── ObInspectionApplication.java
├── src/main/resources/
│   ├── application.yml     # 多数据源配置（H2 主库 + OB 懒加载）
│   ├── schema.sql          # 7 张表 DDL（CREATE TABLE IF NOT EXISTS）
│   └── data.sql            # 规则配置 / 系统配置初始化数据
└── frontend/               # Vue3 + Vite 前端骨架
    └── src/{api,router,views}
```

## 启动步骤

1. **启动 OceanBase（骨架阶段可跳过）**

   OB 数据源为懒加载（`@Lazy`），未启动 OceanBase 时应用也可正常运行。

   ```bash
   docker run -d --name oceanbase -p 2881:2881 \
     -e MODE=slim -e OB_SYS_PASSWORD=root \
     oceanbase/oceanbase-ce:latest
   ```

2. **启动后端（需要 JDK 17）**

   ```bash
   # Git Bash 下先指定 JDK 17
   export JAVA_HOME="D:\\programWork\\zyb\\tools\\jdk-17.0.2"
   export PATH="$JAVA_HOME/bin:$PATH"

   cd ob-inspection
   mvn -s ../tools/settings.xml clean compile
   mvn -s ../tools/settings.xml spring-boot:run
   ```

3. **启动前端**

   ```bash
   cd frontend
   npm install        # 太慢可加 --registry=https://registry.npmmirror.com
   npm run dev        # 开发模式，/api 代理到 http://localhost:8080
   npm run build      # 生产构建
   ```

## 常用地址

- Swagger UI：http://localhost:8080/swagger-ui.html
- OpenAPI JSON：http://localhost:8080/v3/api-docs
- H2 控制台：http://localhost:8080/h2-console
  - JDBC URL：`jdbc:h2:file:./data/obinspection`
  - 用户名：`sa`，密码留空

## 接口概览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | /api/inspection/trigger | 手动触发巡检 |
| GET  | /api/inspection/tasks | 巡检任务列表 |
| GET  | /api/inspection/tasks/{taskId}/results | 任务巡检结果 |
| GET  | /api/alerts | 告警列表 |
| POST | /api/alerts/{id}/ack | 确认告警 |
| POST | /api/alerts/{id}/diagnose | 触发 AI 诊断 |
| GET  | /api/alerts/{id}/diagnosis | 查询 AI 诊断结果 |
| GET  | /api/dashboard/summary | 看板汇总 |
