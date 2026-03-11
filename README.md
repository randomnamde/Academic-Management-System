# 🎓 教务管理系统（Academic Management System）

<p align="center">
  <img src="frontend/public/favicon.svg" alt="Logo" width="80" height="80">
</p>

<p align="center">
  <b>面向高校的全场景教务协同管理平台</b>
</p>

<p align="center">
  <a href="#功能特性"><img src="https://img.shields.io/badge/功能-全场景覆盖-blue" alt="Features"></a>
  <a href="#技术栈"><img src="https://img.shields.io/badge/技术-前后端分离-green" alt="Tech Stack"></a>
  <a href="#快速开始"><img src="https://img.shields.io/badge/部署-一键启动-orange" alt="Deployment"></a>
  <a href="#测试账号"><img src="https://img.shields.io/badge/演示-在线体验-purple" alt="Demo"></a>
</p>

---

## 📋 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [内网部署（Nginx + 前端）](#内网部署nginx--前端)
- [开发指南](#开发指南)
- [API文档](#api文档)
- [测试账号](#测试账号)
- [更新日志](#更新日志)
- [常见问题](#常见问题)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

---

## 🎯 项目简介

教务管理系统是一个**前后端分离**的高校教务协同平台，采用创新的**五角色统一权限模型**，支持多角色账号并集授权与主角色优先级机制。

### 核心优势

- 🏗️ **五角色权限模型**：学校管理员、学院管理员、班主任、任课教师、学生
- 🔐 **RBAC权限控制**：基于角色的细粒度访问控制
- 📊 **数据可视化**：实时统计看板与数据分析
- 📱 **响应式设计**：支持桌面端和移动端访问
- 🌍 **国际化支持**：中英文双语切换
- 🎨 **现代UI设计**：玻璃质感风格，专业美观

---

## ✨ 功能特性

### 认证与权限
- [x] JWT Token 认证机制
- [x] 五角色权限模型（RBAC）
- [x] 多角色账号并集授权
- [x] 主角色优先级机制
- [x] 数据域权限控制

### 基础数据管理
- [x] 学生信息管理（CRUD）
- [x] 教师信息管理（CRUD）
- [x] 班级信息管理（CRUD）
- [x] 学院信息管理（CRUD）
- [x] 课程信息管理（CRUD）
- [x] 学期管理

### 教学业务
- [x] 课程安排管理
- [x] 成绩录入与查询
- [x] 考勤记录管理
- [x] 请假申请与审批流程
- [x] 公告发布与定向推送

### 数据分析
- [x] 首页数据看板
- [x] 学生成绩分析
- [x] 考勤统计分析
- [x] 风险学生预警
- [x] 课程统计报表

### 个人中心
- [x] 个人资料管理
- [x] 密码修改
- [x] 头像上传
- [x] 语言偏好设置

### 系统管理
- [x] 用户管理
- [x] 角色权限管理
- [x] 系统日志审计
- [x] 系统配置管理

---

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 3.2.12 | 应用框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis-Plus | 3.5.7 | ORM框架 |
| JWT | 0.11.5 | 身份认证 |
| MySQL | 8.0 | 生产数据库 |
| H2 | 2.x | 开发/测试数据库 |
| Maven | 3.9+ | 构建工具 |
| Lombok | 1.18.38 | 代码简化 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.3.4 | 前端框架 |
| Vue Router | 4.2.4 | 路由管理 |
| Vuex | 4.1.0 | 状态管理 |
| Element Plus | 2.3.14 | UI组件库 |
| ECharts | 5.4.3 | 数据可视化 |
| Vite | 5.4.11 | 构建工具 |
| Tailwind CSS | 3.4.17 | CSS框架 |
| Vue I18n | 9.14.1 | 国际化 |

### 测试与工具

| 技术 | 说明 |
|------|------|
| Playwright | E2E端到端测试 |
| ESLint | 代码规范检查 |
| Apache POI | Excel导入导出 |

---

## 📁 项目结构

```
student-management-system/
├── 📂 backend/                    # Spring Boot 后端
│   ├── src/main/java/com/student/
│   │   ├── 📂 config/             # 配置类
│   │   ├── 📂 controller/         # 控制器层
│   │   ├── 📂 service/            # 业务层
│   │   ├── 📂 mapper/             # 数据访问层
│   │   ├── 📂 entity/             # 实体类
│   │   ├── 📂 dto/                # 数据传输对象
│   │   ├── 📂 vo/                 # 视图对象
│   │   ├── 📂 security/           # 安全配置
│   │   └── 📂 util/               # 工具类
│   └── src/main/resources/
│       ├── application.yml        # 主配置文件
│       ├── application-h2.yml     # H2配置
│       └── mapper/                # MyBatis映射文件
│
├── 📂 frontend/                   # Vue 前端
│   ├── src/
│   │   ├── 📂 api/                # API接口
│   │   ├── 📂 components/         # 组件
│   │   ├── 📂 views/              # 页面视图
│   │   ├── 📂 router/             # 路由配置
│   │   ├── 📂 store/              # 状态管理
│   │   ├── 📂 i18n/               # 国际化
│   │   ├── 📂 styles/             # 样式文件
│   │   └── 📂 utils/              # 工具函数
│   ├── public/                    # 静态资源
│   └── tests/                     # 测试文件
│
├── 📂 database/                   # 数据库脚本
│   ├── schema.sql                 # 表结构
│   ├── data.sql                   # 初始化数据
│   └── migrations/                # 迁移脚本
│
└── 📂 .run/                       # IDEA运行配置
```

---

## 🚀 快速开始

### 环境要求

- **JDK**: 21+
- **Maven**: 3.9+
- **Node.js**: 18+ (建议 LTS)
- **npm**: 9+
- **MySQL**: 8.0 (仅MySQL模式需要)

### 方式一：IDEA 一键启动（推荐）

1. 使用 IntelliJ IDEA 打开项目
2. 运行 `.run/Start All (H2)` 配置
3. 访问前端：`http://localhost:3000`
4. 后端API：`http://localhost:8080/api`

### 方式二：命令行启动（H2 内存数据库）

**启动后端：**
```bash
cd backend
mvn spring-boot:run -D"spring-boot.run.profiles"=h2
```

**启动前端：**
```bash
cd frontend
npm install
npm run dev
```

**访问地址：** http://localhost:3000

### 方式三：命令行启动（MySQL 数据库）

1. **初始化数据库：**
```bash
# 在MySQL中执行
source database/schema.sql
source database/data.sql
```

2. **配置数据库连接：**
编辑 `backend/src/main/resources/application.yml`

3. **启动服务：**
```bash
# 后端
cd backend
mvn spring-boot:run

# 前端
cd frontend
npm install
npm run dev
```

### 方式四：WSL 本地开发（MySQL + Redis 都在 WSL）

```bash
cd backend
mvn spring-boot:run -D"spring-boot.run.profiles"=dev
```

如果 `8080` 已被占用，可以改成：

```bash
mvn spring-boot:run -D"spring-boot.run.profiles"=dev -D"spring-boot.run.arguments=--server.port=8081"
```

如果 WSL 中的 MySQL `root` 账号使用了系统认证或不是这个项目的密码，建议先创建一个单独的开发账号：

```sql
CREATE DATABASE IF NOT EXISTS student_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'student_app'@'localhost' IDENTIFIED BY 'student123';
CREATE USER IF NOT EXISTS 'student_app'@'127.0.0.1' IDENTIFIED BY 'student123';
GRANT ALL PRIVILEGES ON student_management.* TO 'student_app'@'localhost';
GRANT ALL PRIVILEGES ON student_management.* TO 'student_app'@'127.0.0.1';
FLUSH PRIVILEGES;
```

然后用这个账号启动后端：

```bash
cd backend
export MYSQL_USERNAME=student_app
export MYSQL_PASSWORD=student123
mvn spring-boot:run -D"spring-boot.run.profiles"=dev
```

---

## 内网部署（Nginx + 前端）

如果你准备把项目部署到企业内网，推荐使用下面这套方式：

- 前端：先在 Windows 或有网构建机执行 `npm run build`，产出 `frontend/dist`
- Web 服务：CentOS 上使用 Nginx 托管前端静态文件
- 后端：Spring Boot 以 Jar 方式运行
- 数据库：MySQL 8.0

推荐访问链路：

`浏览器 -> Nginx(80/443) -> 前端静态资源`

`浏览器 -> Nginx /api -> Spring Boot(127.0.0.1:8080) -> MySQL(3306)`

### 部署前需要准备什么

#### 1. 服务器与系统准备

- 一台 CentOS 服务器
- 已安装 `Nginx`
- 已安装 `JDK 21`
- 已安装 `MySQL 8.0`
- 已开放访问端口：`80`，如果后端直连排障可临时开放 `8080`

#### 2. 前端发布准备

前端在内网服务器上通常**不需要**安装 Node.js，只需要准备构建后的静态文件：

```bash
cd frontend
npm install
npm run build
```

构建完成后，会得到：

- `frontend/dist/index.html`
- `frontend/dist/assets/*`

这些文件上传到 CentOS 后，交给 Nginx 托管即可。

#### 3. Nginx 部署准备

你需要准备一份 Nginx 配置，把：

- `/` 指向前端静态目录
- `/api/` 反向代理到后端 `http://127.0.0.1:8080/api/`

项目里已经提供了可直接使用的配置文件：

- `deploy/centos/student-management.conf`

#### 4. 后端与数据库准备

需要准备：

- 后端 Jar 包：`backend/target/student-management-1.0.0.jar`
- 数据库脚本：`database/schema.sql`、`database/data.sql`
- 生产配置或环境变量

建议提前创建数据库：

```sql
CREATE DATABASE student_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 推荐发布方式

如果 CentOS 服务器不能访问公网，推荐在 Windows 或有网构建机先打好发布包，再上传到内网服务器：

```powershell
cd D:\Learn\crm\student-management-system
powershell -ExecutionPolicy Bypass -File .\deploy\windows\build-release.ps1
```

执行后会生成：

- `release/student-management/backend/`
- `release/student-management/frontend/`
- `release/student-management/database/`
- `release/student-management/scripts/`
- `release/student-management/nginx/`

上传到 CentOS 后，只需要：

1. 放置前端静态文件到 Nginx 目录
2. 放置后端 Jar
3. 导入 MySQL 数据
4. 修改 `app.env`
5. 启动 Nginx 和后端服务

### Nginx 托管前端的最小步骤

```bash
sudo mkdir -p /opt/student-management/frontend
sudo cp -r frontend-dist/* /opt/student-management/frontend/
sudo cp /opt/student-management/nginx/student-management.conf /etc/nginx/conf.d/student-management.conf
sudo nginx -t
sudo systemctl restart nginx
```

说明：

- `frontend-dist/*` 指的是你本地构建好的 `frontend/dist/*`
- Nginx 配置中的 `root` 应该指向 `/opt/student-management/frontend`
- 如果前后端统一走 Nginx 入口，前端不需要再单独配置 `VITE_APP_BASE_API`

### 完整部署文档

更完整的 CentOS 内网部署步骤可以查看：

- `deploy/centos/README.md`

## 💻 开发指南

### 后端开发

```bash
# 编译
cd backend
mvn -q -DskipTests compile

# 运行测试
mvn test

# 打包
mvn -DskipTests package

# 运行
mvn spring-boot:run -D"spring-boot.run.profiles"=dev
```

### 前端开发

```bash
cd frontend

# 安装依赖
npm install

# 开发模式（热重载）
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview

# 代码检查
npm run lint

# E2E测试
npm run test:e2e

# UTF-8编码检查
npm run check:utf8
```

### 端口配置

| 服务 | 端口 | 地址 |
|------|------|------|
| 前端开发服务器 | 3000 | http://localhost:3000 |
| 后端API服务 | 8080 | http://localhost:8080/api |

---

## 📚 API文档

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/refresh | 刷新Token |

### 核心业务接口

| 模块 | 基础路径 | 说明 |
|------|----------|------|
| 学生管理 | /api/students | 学生CRUD操作 |
| 教师管理 | /api/teachers | 教师CRUD操作 |
| 课程管理 | /api/courses | 课程CRUD操作 |
| 成绩管理 | /api/scores | 成绩录入查询 |
| 考勤管理 | /api/attendance | 考勤记录管理 |
| 请假管理 | /api/leave-requests | 请假申请审批 |
| 公告管理 | /api/announcements | 公告发布管理 |
| 数据分析 | /api/analytics | 统计报表数据 |

---

## 🔑 测试账号

系统预置了以下测试账号（H2模式）：

说明：教师登录账号默认等于 `teacher_no`，学生登录账号默认等于 `student_no`。

| 角色 | 账号 | 密码 | 权限说明 |
|------|------|------|----------|
| 🏫 学校管理员 | `admin` | `123456` | 系统最高权限，管理所有数据 |
| 🏢 学院管理员 | `college_admin_cs` | `123456` | 管理学院内所有数据 |
| 👨‍🏫 班主任/教师 | `T00CS20240001` | `123456` | 班级管理 + 课程教学 |
| 👨‍🎓 学生 | `2023SO0001` | `123456` | 查看个人数据，申请请假 |

---

## 📝 更新日志

### 2026-03-04
**权限模型重大升级**
- ✨ 五角色权限模型正式启用
- 🔧 角色代码统一：`SCHOOL_ADMIN`、`COLLEGE_ADMIN`、`HOMEROOM_TEACHER`、`COURSE_TEACHER`、`STUDENT`
- 🗃️ 数据迁移脚本：`database/migrations/20260304_role_refactor.sql`
- 📢 公告系统升级：支持 `ALL + 五角色` 定向推送

### 2026-02-28
**学生端功能优化**
- 📊 分析中心：学生视角改为"仅本人明细"展示
- 🎯 数据口径：首页统计与学生可见范围保持一致
- 🔒 权限控制：学生数据域强制收口，防止越权查看
- 🐛 问题修复：请假审批查询功能修复

---

## ❓ 常见问题

### Q: 无法访问 localhost:3000？
**A:** 
- 确认前端服务已启动（端口3000）
- 确认后端服务已启动（端口8080）
- 检查防火墙设置
- 使用 `http://localhost:3000` 而非 `https://`

### Q: 接口返回 404 或旧数据？
**A:**
- 通常是后端旧进程未重启
- 请完全关闭后端服务后重新启动
- 检查端口是否被占用

### Q: 头像上传后无法显示？
**A:**
- 确认后端服务正常运行
- 检查 `uploads/` 目录是否存在且有写入权限
- 确认文件已正确保存到该目录

### Q: 数据库连接失败？
**A:**
- H2模式：无需额外配置，自动创建内存数据库
- MySQL模式：检查数据库服务是否启动，连接配置是否正确

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

---

## 📄 许可证

本项目基于 [MIT](LICENSE) 许可证开源。

---

<p align="center">
  Made with ❤️ for Education
</p>
