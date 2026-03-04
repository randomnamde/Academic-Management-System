# 学生管理系统（Student Management System）

一个前后端分离的学生管理系统示例项目，覆盖用户认证、学生/教师/班级/课程管理、成绩与考勤、请假、公告、个人中心等常见校园管理场景。

## 技术栈

- 后端：Java 21、Spring Boot 3.2.x、Spring Security、MyBatis-Plus、JWT
- 数据库：MySQL 8（默认）/ H2（开发与演示）
- 前端：Vue 3、Vue Router、Vuex、Element Plus、ECharts、Axios

## 主要功能

- 登录、注册、JWT 鉴权
- 五角色权限模型：学校管理员、学院管理员、班主任、任课教师、学生
- 学生管理、教师管理、班级管理、课程管理
- 成绩管理、考勤管理、请假管理
- 公告管理与首页看板
- 个人中心（资料修改、密码修改、头像上传）
- 登录页「智慧校园管控中枢」实时指标展示

## 项目结构

```text
student-management-system/
├─ backend/      # Spring Boot 后端
├─ frontend/     # Vue 前端
├─ database/     # MySQL 建表与初始化脚本
└─ .run/         # IDEA 运行配置（含 H2 一键启动）
```

## 环境要求

- JDK 21+
- Maven 3.9+
- Node.js 18+（建议 LTS）
- npm 9+
- MySQL 8（仅 MySQL 模式需要）

## 快速启动

### 方式一：IDEA 一键启动（推荐，H2 内存库）

1. 打开项目后，运行 `.run/Start All (H2)`。
2. 前端地址：`http://localhost:3000`
3. 后端地址：`http://localhost:8080/api`

### 方式二：命令行启动（H2）

后端：

```powershell
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=h2"
```

前端：

```powershell
cd frontend
npm install
npm run serve
```

### 方式三：命令行启动（MySQL）

1. 在 MySQL 中执行：
   - `database/schema.sql`
   - `database/data.sql`
2. 按需修改 `backend/src/main/resources/application.yml` 中数据库连接信息。
3. 启动后端与前端：

```powershell
cd backend
mvn spring-boot:run
```

```powershell
cd frontend
npm install
npm run serve
```

## 默认端口与地址

- 前端开发服务：`3000`
- 后端服务：`8080`
- 后端上下文：`/api`
- 前端代理：`/api -> http://localhost:8080/api`

## 测试账号（H2 初始化数据）

- 学校管理员：`admin / 123456`
- 学院管理员：`college_admin_cs / 123456`
- 班主任/任课教师（双角色）：`teacher001 / 123456`
- 学生：`student001 / 123456`

## 最近更新

- 2026-03-04：权限模型升级为五角色并切断旧三角色兼容。
  - 角色：统一为 `SCHOOL_ADMIN`、`COLLEGE_ADMIN`、`HOMEROOM_TEACHER`、`COURSE_TEACHER`、`STUDENT`。
  - 鉴权：路由与接口权限改为五角色矩阵，旧 `ADMIN/TEACHER` 不再作为运行时角色。
  - 数据：新增迁移脚本 `database/migrations/20260304_role_refactor.sql`，包含用户主角色回填与公告目标角色拆分迁移。
  - 公告：`targetRole` 升级为 `ALL + 五角色`，历史 `ADMIN/TEACHER` 公告按双角色复制拆分。

- 2026-02-28：学生分析中心风险榜单改为“仅本人明细”展示。
  - 后端：`/analytics/risk-students` 在学生角色下切换为明细模式，按风险类型返回本人数据：
    - `low_score`：按课程维度去重，返回低分课程、最低分、低分次数。
    - `abnormal_attendance`：返回本人异常考勤明细（日期、课程、状态）。
    - `approval_overdue`：仅返回本人已超时（`PENDING` 且超过 48 小时）的审批记录，并返回超时小时数。
  - 数据结构：`RiskStudentDTO` 新增 `courseName`、`score`、`attendanceDate`、`attendanceStatus`、`leaveRequestId`、`submitTime`、`overdue`、`overdueHours` 字段。
  - 前端：分析中心风险榜单在学生视角按 `riskType` 切换专用列（课程/考勤/审批超时明细），教师与管理员维持原聚合榜单。
  - 测试：新增 3 个集成测试覆盖学生低分课程明细、异常考勤明细、仅本人超时审批明细。

- 2026-02-28：完成学生端“首页 + 分析中心”权限与口径收敛。
  - 首页：学生“我的信息/任课教师数/我的课程数/我的班级数”支持点击跳转；我的信息不显示计数，其余按本人范围统计并回显。
  - 菜单与路由：学生可从首页进入教师/课程/班级页面，但侧栏隐藏这三项菜单；页面统一只读，后端继续做范围校验兜底。
  - 数据口径：`/dashboard/overview` 与学生可见范围保持一致；学生无班级归属时 `classCount = 0`。
  - 分析中心：学生隐藏“学生规模”，审批指标改为“我的待办审批”（`PENDING + REJECTED`，不含 `APPROVED`），低分指标改为“低分课程统计数”（按 `courseId` 去重）。
  - 测试：扩展 `SecurityScopeIntegrationTest`，覆盖学生审批计数新口径与低分课程去重统计。

- 2026-02-28：收紧学生权限展示范围（首页总览与分析中心仅展示本人相关数据）。
  - 后端：分析中心接口在学生角色下强制使用本人数据域，忽略 `classId`、`teacherId` 跨域参数，防止越权查看。
  - 前端：首页学生视角改为“我的数据”（任课教师数、我的课程数、我的班级数、我的运营概览），不再展示全局统计分布。
  - 测试：新增学生跨域参数访问分析接口的集成测试，验证数据域收口持续生效。

- 2026-02-28：修复「请假审批」页面在 `pending` 页签下点击“查询”无效的问题。
  - 修复前：审批角色在 `pending` 页签会始终调用 `/leave-request/pending`，导致查询条件被忽略。
  - 修复后：仅在未设置查询条件时使用快捷接口；带条件查询时走分页列表接口，查询按钮生效。

## 常见问题

- `localhost` 拒绝连接：
  - 确认前端在 `3000` 端口、后端在 `8080` 端口监听。
  - 浏览器访问 `http://localhost:3000`（不是 `http://localhost`）。
- 新接口返回 404 或旧数据：
  - 通常是后端旧进程未重启，请重启后端服务。
- 上传头像后无法访问：
  - 检查后端是否正常启动，`uploads/` 目录是否有文件。

## 常用命令

```powershell
# 后端编译
cd backend
mvn -q -DskipTests compile

# 前端构建
cd frontend
npm run build
```

## 说明

- `frontend/dist` 为构建后的静态资源目录。
- `uploads/` 为运行时上传目录，已在 `.gitignore` 中忽略。
