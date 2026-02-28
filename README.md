# 学生管理系统（Student Management System）

一个前后端分离的学生管理系统示例项目，覆盖用户认证、学生/教师/班级/课程管理、成绩与考勤、请假、公告、个人中心等常见校园管理场景。

## 技术栈

- 后端：Java 21、Spring Boot 3.2.x、Spring Security、MyBatis-Plus、JWT
- 数据库：MySQL 8（默认）/ H2（开发与演示）
- 前端：Vue 3、Vue Router、Vuex、Element Plus、ECharts、Axios

## 主要功能

- 登录、注册、JWT 鉴权
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

- 管理员：`demo_admin / 123456`
- 教师：`teacher001 / 123456`
- 学生：`student001 / 123456`

## 最近更新

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
