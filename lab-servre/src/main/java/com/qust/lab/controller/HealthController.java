package com.qust.lab.controller;

import com.qust.lab.common.result.Result;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@RestController
public class HealthController {

    private final DataSource dataSource;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final boolean messagingEnabled;

    public HealthController(
            DataSource dataSource,
            StringRedisTemplate stringRedisTemplate,
            RabbitTemplate rabbitTemplate,
            @Value("${app.messaging.enabled:false}")
            boolean messagingEnabled
    ) {
        this.dataSource = dataSource;
        this.stringRedisTemplate = stringRedisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.messagingEnabled = messagingEnabled;
    }

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("database", checkDatabase());
        data.put("redis", checkRedis());
        data.put("rabbitmq", checkRabbitMq());

        return Result.success(data);
    }

    @GetMapping("/health/database")
    public Result<Map<String, Object>> database() {
        return Result.success(checkDatabase());
    }

    @GetMapping("/health/redis")
    public Result<Map<String, Object>> redis() {
        return Result.success(checkRedis());
    }

    @GetMapping("/health/rabbitmq")
    public Result<Map<String, Object>> rabbitMq() {
        return Result.success(checkRabbitMq());
    }

    private Map<String, Object> checkDatabase() {
        Map<String, Object> data = new LinkedHashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            data.put("connected", connection.isValid(2));
            data.put("database", connection.getCatalog());
        } catch (Exception e) {
            data.put("connected", false);
            data.put("error", "数据库暂时不可用");
        }

        return data;
    }

    private Map<String, Object> checkRedis() {
        Map<String, Object> data = new LinkedHashMap<>();

        try (RedisConnection connection = Objects.requireNonNull(
                stringRedisTemplate.getConnectionFactory()
        ).getConnection()) {
            data.put("connected", "PONG".equals(connection.ping()));
        } catch (Exception e) {
            data.put("connected", false);
            data.put("error", "Redis 暂时不可用");
        }

        return data;
    }

    private Map<String, Object> checkRabbitMq() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", messagingEnabled);

        if (!messagingEnabled) {
            data.put("connected", false);
            data.put("message", "RabbitMQ 消息功能未开启");
            return data;
        }

        try {
            Boolean connected = rabbitTemplate.execute(channel ->
                    channel != null && channel.isOpen()
            );
            data.put("connected", Boolean.TRUE.equals(connected));
        } catch (Exception e) {
            data.put("connected", false);
            data.put("error", "RabbitMQ 暂时不可用");
        }

        return data;
    }
}
