# LabTrace

LabTrace 是一个实验室样品追踪后端项目，当前使用 Spring Boot、MyBatis-Plus、MySQL，Redis 和 JWT。

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

如果数据库表已经存在，按顺序执行尚未运行过的迁移脚本：

```sql
source /home/yyc/IdeaProjects/labtrace/db/migration/V2__add_foreign_keys.sql;
source /home/yyc/IdeaProjects/labtrace/db/migration/V3__create_sample_audit_log.sql;
source /home/yyc/IdeaProjects/labtrace/db/migration/V4__create_outbox_message.sql;
source /home/yyc/IdeaProjects/labtrace/db/migration/V5__recover_stale_outbox_messages.sql;
```

其中 V2 只需执行一次；已有 Outbox 表的环境至少需要执行 V5。
这些脚本目前由管理员手动执行，不会在应用启动时自动迁移。

脚本会创建：

- `labtrace` 数据库
- `lab_user` 用户表
- `sample` 样品表
- `sample_handover` 样品交接记录表
- MySQL 连接账号：`labtrace / Labtrace@123456`
- 系统管理员账号：`admin / 123456`

如果是从 GitHub 重新拉取项目，可以参考
`lab-servre/src/main/resources/application.yml.example`
创建本机的 `application.yml`，再按实际环境修改数据库、Redis 和 JWT 配置。

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

检查本机环境是否连通：

```text
GET http://localhost:8080/health
GET http://localhost:8080/health/database
GET http://localhost:8080/health/redis
GET http://localhost:8080/health/rabbitmq
```

其中 `/health` 会一次性返回 MySQL、Redis 和 RabbitMQ 的状态。
如果本地默认配置没有开启 RabbitMQ，`rabbitmq.enabled` 显示 `false` 是正常的。

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
- RabbitMQ 账号：`labtrace / LabtraceMq@123456`
- MySQL root 账号：`root / Root@123456`
- 项目数据库账号：`labtrace / Labtrace@123456`

以上账号密码仅用于本地开发。部署到共享、测试或生产环境时，请通过
`MYSQL_ROOT_PASSWORD`、`MYSQL_PASSWORD`、`RABBITMQ_USERNAME`、
`RABBITMQ_PASSWORD` 和 `JWT_SECRET` 环境变量覆盖默认值。

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

Outbox 消息如果处于 `SENDING` 状态超过 5 分钟，会被后台任务重新发送。
发送后会等待 RabbitMQ 确认；未确认或没有路由到队列的消息保留在 Outbox 中重试。
消费者通过事件编号避免重复写入审计日志。

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

## 5. Agent 接口

Agent 接口需要先登录，并携带登录返回的 Token。

```text
POST http://localhost:8080/api/agent/chat
Authorization: Bearer 你的token
Content-Type: application/json
```

请求体：

```json
{
  "sampleId": 1,
  "question": "这个样品当前是什么状态，经过了几次交接？"
}
```

Agent 会读取样品信息、交接历史和审计日志。
默认情况下 `AGENT_ENABLED=false`，系统会返回本地整理的样品上下文，不会调用远程模型。

如果要开启远程模型，启动后端前设置：

```bash
export AGENT_ENABLED=true
export AGENT_API_KEY="你的模型 API Key"
export AGENT_BASE_URL="https://api.openai.com/v1"
export AGENT_MODEL="gpt-4.1-mini"
```

然后重新启动后端。
