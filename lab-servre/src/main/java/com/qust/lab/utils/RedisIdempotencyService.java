package com.qust.lab.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class RedisIdempotencyService {

    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration LOCK_TTL = Duration.ofMinutes(10);

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                        return redis.call('del', KEYS[1])
                    end
                    return 0
                    """,
                    Long.class
            );

    private final StringRedisTemplate stringRedisTemplate;

    public RedisIdempotencyService(
            StringRedisTemplate stringRedisTemplate
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public LockToken acquire(
            Long userId,
            String operation,
            String requestId
    ) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("当前用户信息无效");
        }

        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException(
                    "请提供 Idempotency-Key 请求编号"
            );
        }

        if (requestId.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency-Key 不能超过 100 个字符"
            );
        }

        String key = KEY_PREFIX
                + operation
                + ":user:"
                + userId
                + ":request:"
                + requestId.trim();

        String value = UUID.randomUUID().toString();

        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, value, LOCK_TTL);

        if (!Boolean.TRUE.equals(acquired)) {
            throw new IllegalArgumentException(
                    "请求正在处理或已经提交，请勿重复提交"
            );
        }

        return new LockToken(key, value);
    }

    public void release(LockToken lockToken) {
        if (lockToken == null) {
            return;
        }

        stringRedisTemplate.execute(
                RELEASE_SCRIPT,
                List.of(lockToken.key()),
                lockToken.value()
        );
    }

    public record LockToken(String key, String value) {
    }
}
