# LabTrace

LabTrace 是一个实验室样品追踪后端项目，当前使用 Spring Boot、MyBatis-Plus、MySQL 和 JWT。

## 1. 初始化数据库

先进入 MySQL：

```bash
sudo mysql
```

然后执行建表脚本：

```sql
source /home/yyc/IdeaProjects/labtrace/db/schema.sql;
source /home/yyc/IdeaProjects/labtrace/db/data.sql;
```

如果数据库表已经存在，只需要执行外键迁移脚本：

```sql
source /home/yyc/IdeaProjects/labtrace/db/migration/V2__add_foreign_keys.sql;
```

这个迁移脚本只需要执行一次。它会保证样品和交接记录引用的用户、样品必须真实存在。

脚本会创建：

- `labtrace` 数据库
- `lab_user` 用户表
- `sample` 样品表
- `sample_handover` 样品交接记录表
- MySQL 连接账号：`labtrace / Labtrace@123456`
- 系统管理员账号：`admin / 123456`

## 2. 启动后端

如果要使用退出登录功能，需要先启动 Redis：

```bash
redis-server
```

```bash
mvn -pl lab-servre spring-boot:run
```

启动成功后访问：

```text
http://localhost:8080/hello
```

## 使用 Docker 启动 MySQL 和 Redis

如果不想使用本机安装的 MySQL 和 Redis，可以用 Docker 启动环境：

```bash
docker compose up -d
```

Docker 环境端口：

- MySQL：`localhost:3307`
- Redis：`localhost:6380`
- RabbitMQ：`localhost:5673`
- RabbitMQ 管理台：`http://localhost:15673`
- MySQL root 账号：`root / Root@123456`
- 项目数据库账号：`labtrace / Labtrace@123456`

使用 Docker 环境启动后端：

```bash
mvn -pl lab-servre spring-boot:run -Dspring-boot.run.profiles=docker
```

Docker 配置会开启 RabbitMQ 消息事件。创建样品成功后，后端会向
`labtrace.events` 交换机发送 `sample.created` 事件。

样品状态变化或交接成功后，会发送 `sample.status.changed` 事件。

消费者会将这两类事件写入 `sample_audit_log` 表，可以通过以下接口查询：

```text
GET /api/samples/{id}/audit-logs
```

查看容器状态：

```bash
docker compose ps
```

停止容器：

```bash
docker compose down
```

## 3. 登录接口

```text
POST http://localhost:8080/api/users/login
```

请求体：

```json
{
  "username": "admin",
  "password": "123456"
}
```

创建样品和样品交接接口需要增加请求头：

```text
Idempotency-Key: 例如 sample-create-001
```

同一个用户使用相同的 `Idempotency-Key` 重复提交时，Redis 会在 10 分钟内阻止重复操作。每次新的业务操作都要生成新的请求编号。

登录成功后，把返回的 `token` 放到后续请求头：

```text
Authorization: Bearer 你的token
```

## 4. 退出登录

```text
POST http://localhost:8080/api/users/logout
```

请求头：

```text
Authorization: Bearer 你的token
```

退出后，这个 token 会被写入 Redis 黑名单。再次使用同一个 token 请求接口时，会返回未登录或已退出。
