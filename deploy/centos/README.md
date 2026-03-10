# CentOS 内网部署说明

## 1. 推荐部署拓扑

- Nginx：对外提供统一访问入口，负责托管前端静态文件，并把 `/api/` 反向代理到 Spring Boot。
- Spring Boot：以 Jar 方式运行，默认监听 `8080`，上下文路径为 `/api`。
- MySQL 8.0：生产数据库，建议与应用同网段部署。
- 上传目录：后端通过 `/api/public/**` 对外提供访问，需持久化保留。

推荐访问链路：

`浏览器 -> Nginx(80/443) -> 前端静态资源`

`浏览器 -> Nginx /api -> Spring Boot(127.0.0.1:8080) -> MySQL(3306)`

## 2. 目录规划

建议在服务器上统一放到 `/opt/student-management`：

```text
/opt/student-management/
├── backend/student-management.jar
├── frontend/
├── nginx/
├── uploads/
├── logs/
├── run/
└── scripts/
```

## 3. 运行前准备

### 3.1 安装运行环境

- JDK 21
- Nginx
- MySQL 8.0

说明：

- 如果内网服务器不能访问公网，建议在有网的构建机先执行 `deploy/centos/build-release.sh`，再把 `release/student-management` 整包拷到服务器。
- 如果公司内网已经有 Maven / npm 私服，也可以直接在内网构建。
- 如果你在 Windows 上构建，优先使用 `deploy/windows/build-release.ps1`，避免 WSL 中 `JAVA_HOME`、换行符和路径差异带来的问题。

Windows 构建示例：

```powershell
cd D:\Learn\crm\student-management-system
powershell -ExecutionPolicy Bypass -File .\deploy\windows\build-release.ps1
```

### 3.2 初始化数据库

创建数据库：

```sql
CREATE DATABASE student_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

导入脚本：

```bash
mysql -uroot -p student_management < /opt/student-management/database/schema.sql
mysql -uroot -p student_management < /opt/student-management/database/data.sql
```

## 4. 部署步骤

### 4.1 创建运行用户和目录

```bash
sudo useradd -r -s /sbin/nologin sms || true
sudo mkdir -p /opt/student-management/{backend,frontend,nginx,uploads,logs,run,scripts,database}
sudo chown -R sms:sms /opt/student-management
```

### 4.2 上传构件

把以下内容放到对应目录：

- `backend/student-management.jar` -> `/opt/student-management/backend/`
- `frontend/dist/*` -> `/opt/student-management/frontend/`
- `database/*.sql` -> `/opt/student-management/database/`
- `deploy/centos/*.sh` -> `/opt/student-management/scripts/`
- `deploy/centos/student-management.service` -> `/opt/student-management/scripts/`
- `deploy/centos/student-management.conf` -> `/opt/student-management/nginx/`

然后复制环境文件：

```bash
cp /opt/student-management/scripts/app.env.example /opt/student-management/scripts/app.env
vim /opt/student-management/scripts/app.env
chmod +x /opt/student-management/scripts/*.sh
```

重点修改：

- `MYSQL_HOST`
- `MYSQL_PORT`
- `MYSQL_DB`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS`

### 4.3 配置 Nginx

复制配置：

```bash
sudo cp /opt/student-management/nginx/student-management.conf /etc/nginx/conf.d/student-management.conf
sudo nginx -t
sudo systemctl restart nginx
```

如果你是直接从仓库拷文件，也可以使用仓库内的 [`student-management.conf`](./student-management.conf)。

### 4.4 启动后端

```bash
sudo -u sms /opt/student-management/scripts/start-backend.sh
sudo -u sms /opt/student-management/scripts/status-backend.sh
```

## 5. systemd 托管

推荐把后端交给 systemd：

```bash
sudo cp /opt/student-management/scripts/student-management.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable student-management
sudo systemctl start student-management
sudo systemctl status student-management
```

## 6. 常用命令

```bash
/opt/student-management/scripts/start-backend.sh
/opt/student-management/scripts/stop-backend.sh
/opt/student-management/scripts/restart-backend.sh
/opt/student-management/scripts/status-backend.sh
tail -f /opt/student-management/logs/console.out
tail -f /opt/student-management/logs/student-management.log
```

## 7. 验收检查

- 前端页面：`http://服务器IP/`
- 后端接口：`http://服务器IP/api`
- 上传资源：`http://服务器IP/api/public/...`

建议检查：

- `ss -lntp | grep 8080`
- `systemctl status nginx`
- `systemctl status student-management`
- `mysql -uroot -p -e "show databases;"`

## 8. 适合内网的发布方式

最稳妥的方式是“两段式发布”：

1. 在有网构建机执行 `deploy/centos/build-release.sh`
2. 把 `release/student-management` 通过 `scp`、制品库或离线介质传到内网服务器
3. 在服务器只做解压、改配置、导库、启动

这样可以避免 CentOS 服务器直接访问公网下载 Maven 和 npm 依赖。
