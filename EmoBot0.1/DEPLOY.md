# EmoBot 部署指南

## 一、本地打包

在项目目录执行：

```bash
mvn clean package -DskipTests
```

生成的 JAR 位于：`target/emobot-1.0.0.jar`

---

## 二、Docker 部署（Spring Boot build-image）

无需手写 Dockerfile，使用 Spring Boot 自带的 Cloud Native Buildpacks 构建镜像。

### 1. 前提

- 已安装 Docker，且 Docker 守护进程在运行
- 已安装 Maven
- **Docker 29+**：需 Spring Boot 3.4.12+，否则会报「client version 1.24 is too old」

### 2. 构建镜像

```bash
mvn spring-boot:build-image
```

构建完成后镜像名为 `emobot:1.0.0`。

### 3. 运行容器

**方式 A：直接传环境变量**

```bash
docker run -d --name emobot -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DASHSCOPE_API_KEY=sk-你的key \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://RDS地址:5432/emobot \
  -e SPRING_DATASOURCE_USERNAME=用户名 \
  -e SPRING_DATASOURCE_PASSWORD=密码 \
  emobot:1.0.0
```

**方式 B：使用 .env 文件**

```bash
# 创建 .env（不要提交到 Git）
echo "DASHSCOPE_API_KEY=sk-xxx" > .env
echo "SPRING_DATASOURCE_URL=jdbc:postgresql://xxx:5432/emobot" >> .env
echo "SPRING_DATASOURCE_USERNAME=user" >> .env
echo "SPRING_DATASOURCE_PASSWORD=pass" >> .env

docker run -d --name emobot -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --env-file .env \
  emobot:1.0.0
```

### 4. 推送到镜像仓库（可选）

```bash
# 打 tag
docker tag emobot:1.0.0 你的仓库/emobot:1.0.0

# 推送
docker push 你的仓库/emobot:1.0.0
```

在服务器上拉取并运行：

```bash
docker pull 你的仓库/emobot:1.0.0
docker run -d -p 8080:8080 --env-file .env -e SPRING_PROFILES_ACTIVE=prod --name emobot emobot:1.0.0
```

### 5. 无法访问 Docker Hub 时的替代方案

若报错 `failed to resolve reference`、`connectex`、`connection attempt failed`，多因无法访问 `registry-1.docker.io`（如国内网络）。可选：

**方案 A：配置 Docker 镜像加速**

1. 打开 Docker Desktop → **Settings** → **Docker Engine**
2. 删除无效镜像（如 `docker.mirrors.ustc.edu.cn` 已失效），换成以下之一：

```json
{
  "registry-mirrors": [
    "https://docker.xuanyuan.me"
  ]
}
```

其他可尝试：`https://dockerpull.cn`、`https://docker.1ms.run`、`https://registry.cn-hangzhou.aliyuncs.com`（阿里云镜像需在控制台单独开通）

3. 点击 **Apply & Restart**，再执行 `mvn spring-boot:build-image`

**方案 B：使用传统 Dockerfile（不依赖 buildpacks）**

Dockerfile 使用 `mcr.microsoft.com/openjdk/jdk:21-ubuntu`，国内通常比 Docker Hub 更易拉取。

```bash
# 1. 先打包
mvn clean package -DskipTests

# 2. 构建镜像（使用项目根目录的 Dockerfile）
docker build -t emobot:1.0.0 .

# 3. 运行
docker run -d -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod --env-file .env emobot:1.0.0
```

若微软镜像也拉取失败，可改为 `eclipse-temurin:21-jre`（需能访问 Docker Hub 或配置镜像加速）。

---

## 三、服务器环境要求

| 项目 | 要求 |
|------|------|
| 操作系统 | Linux（推荐 Ubuntu 22.04 / CentOS 7+） |
| JDK | **JDK 21** 或以上 |
| 内存 | 建议 ≥ 512MB（RAG 场景建议 ≥ 1GB） |
| 网络 | 能访问阿里云通义 API、RDS PostgreSQL |

### 安装 JDK 21（若未安装）

**Ubuntu / Debian：**
```bash
sudo apt update
sudo apt install openjdk-21-jdk -y
java -version
```

**CentOS / RHEL：**
```bash
sudo yum install java-21-openjdk java-21-openjdk-devel -y
java -version
```

---

## 四、上传到服务器

### 方式 1：scp

```bash
scp target/emobot-1.0.0.jar user@你的服务器IP:/home/user/emobot/
```

### 方式 2：rsync

```bash
rsync -avz target/emobot-1.0.0.jar user@你的服务器IP:/home/user/emobot/
```

### 服务器目录建议

```bash
ssh user@你的服务器IP

# 创建目录
mkdir -p ~/emobot
cd ~/emobot
# 将 JAR 放在此目录
```

---

## 五、服务器配置

### 1. 创建环境变量文件

```bash
cd ~/emobot
nano .env
```

内容示例（按实际修改）：

```bash
# 通义 API Key
export DASHSCOPE_API_KEY=sk-你的API密钥

# PostgreSQL（阿里云 RDS）
export SPRING_DATASOURCE_URL=jdbc:postgresql://你的RDS地址.xxx.rds.aliyuncs.com:5432/emobot
export SPRING_DATASOURCE_USERNAME=你的数据库用户名
export SPRING_DATASOURCE_PASSWORD=你的数据库密码
```

保存后设置权限：

```bash
chmod 600 .env
```

### 2. 阿里云 RDS 准备

- 实例：PostgreSQL 14+，内核 ≥ 20230430
- 在 RDS 控制台 **插件管理** 中安装 `vector`（pgvector）
- 高权限账号执行（若需）：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

- 安全组：放行应用服务器访问 RDS 的 5432 端口

---

## 六、运行方式

### 方式 A：直接运行（测试用）

```bash
cd ~/emobot
source .env
java -jar emobot-1.0.0.jar --spring.profiles.active=prod
```

Ctrl+C 停止。

### 方式 B：后台运行（nohup）

```bash
cd ~/emobot
source .env
nohup java -jar emobot-1.0.0.jar --spring.profiles.active=prod > emobot.log 2>&1 &
echo $! > emobot.pid
```

停止：

```bash
kill $(cat ~/emobot/emobot.pid)
```

### 方式 C：systemd 服务（推荐，开机自启）

创建服务文件：

```bash
sudo nano /etc/systemd/system/emobot.service
```

内容：

```ini
[Unit]
Description=EmoBot 情感指导助手
After=network.target

[Service]
Type=simple
User=你的用户名
WorkingDirectory=/home/你的用户名/emobot
EnvironmentFile=/home/你的用户名/emobot/.env
ExecStart=/usr/bin/java -jar /home/你的用户名/emobot/emobot-1.0.0.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启用并启动：

```bash
sudo systemctl daemon-reload
sudo systemctl enable emobot
sudo systemctl start emobot
sudo systemctl status emobot
```

常用命令：

```bash
sudo systemctl stop emobot    # 停止
sudo systemctl restart emobot # 重启
sudo journalctl -u emobot -f   # 查看日志
```

---

## 七、Nginx 反向代理（可选）

如需域名访问或 HTTPS，可配置 Nginx：

### 1. 安装 Nginx

```bash
# Ubuntu
sudo apt install nginx -y

# CentOS
sudo yum install nginx -y
```

### 2. 配置站点

```bash
sudo nano /etc/nginx/conf.d/emobot.conf
```

示例配置（替换 `你的域名` 和 `你的服务器IP`）：

```nginx
server {
    listen 80;
    server_name 你的域名;   # 如 emobot.example.com，或直接用 IP

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300;
        proxy_connect_timeout 300;
        proxy_send_timeout 300;
    }
}
```

### 3. 检查并重载

```bash
sudo nginx -t
sudo systemctl reload nginx
```

### 4. 前端 API 地址

前端打包在 JAR 内，通过 Nginx 访问时，API 请求会发到同一域名，无需额外配置。若通过 `http://你的域名/` 访问，`/api/chat` 会正确转发到后端。

### 5. HTTPS（建议生产使用）

可使用 Let's Encrypt：

```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d 你的域名
```

---

## 八、防火墙

如开启防火墙，需放行：

```bash
# Ubuntu (ufw)
sudo ufw allow 80/tcp
sudo ufw allow 22/tcp
sudo ufw enable

# CentOS (firewalld)
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --reload
```

直接访问 8080 时：`sudo ufw allow 8080/tcp` 或对应 firewalld 规则。

---

## 九、不启用 RAG 时的部署

若不使用 PostgreSQL，可用默认 profile 或 dev：

```bash
java -jar emobot-1.0.0.jar
# 或
java -jar emobot-1.0.0.jar --spring.profiles.active=dev
```

此时无需配置数据源，但 RAG 功能不可用。

---

## 十、checklist

- [ ] JDK 21 已安装
- [ ] JAR 已上传到服务器
- [ ] `.env` 已配置（DASHSCOPE_API_KEY、数据库连接）
- [ ] RDS 已安装 pgvector 插件
- [ ] RDS 安全组已放行应用服务器
- [ ] systemd 服务已配置并启动
- [ ] Nginx 已配置（若使用域名/HTTPS）
- [ ] 防火墙已放行 80/8080
