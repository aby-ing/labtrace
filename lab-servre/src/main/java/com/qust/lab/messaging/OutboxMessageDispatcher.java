package com.qust.lab.messaging;

import com.qust.lab.mapper.OutboxMessageMapper;
import com.qust.lab.pojo.entity.OutboxMessage;
import com.qust.lab.srevice.SampleEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(
        prefix = "app.messaging",
        name = "enabled",
        havingValue = "true"
)
public class OutboxMessageDispatcher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxMessageDispatcher.class);

    private final OutboxMessageMapper outboxMessageMapper;
    private final SampleEventPublisher sampleEventPublisher;
    private final int maxErrorLength;

    public OutboxMessageDispatcher(
            OutboxMessageMapper outboxMessageMapper,
            SampleEventPublisher sampleEventPublisher,
            @Value("${app.messaging.max-error-length:500}")
            int maxErrorLength
    ) {
        this.outboxMessageMapper = outboxMessageMapper;
        this.sampleEventPublisher = sampleEventPublisher;
        this.maxErrorLength = maxErrorLength;
    }

    @Scheduled(fixedDelayString = "${app.messaging.dispatch-delay-ms:5000}")
    public void dispatch() {
        List<OutboxMessage> messages =
                outboxMessageMapper.selectReadyMessages();

        for (OutboxMessage message : messages) {
            if (outboxMessageMapper.markSending(message.getId()) != 1) {
                continue;
            }

            try {
                sampleEventPublisher.publish(message);
                outboxMessageMapper.markPublished(message.getId());
            } catch (Exception e) {
                String error = shortenError(e.getMessage());
                outboxMessageMapper.markFailed(
                        message.getId(),
                        error
                );
                log.error(
                        "Outbox 消息发送失败，稍后重试: {}",
                        message.getEventId(),
                        e
                );
            }
        }
    }

    private String shortenError(String error) {
        if (error == null || error.isBlank()) {
            return "未知消息发送错误";
        }

        if (error.length() <= maxErrorLength) {
            return error;
        }

        return error.substring(0, maxErrorLength);
    }
}
