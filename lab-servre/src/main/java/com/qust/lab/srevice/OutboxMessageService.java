package com.qust.lab.srevice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qust.lab.mapper.OutboxMessageMapper;
import com.qust.lab.pojo.entity.OutboxMessage;
import com.qust.lab.pojo.event.SampleCreatedEvent;
import com.qust.lab.pojo.event.SampleStatusChangedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OutboxMessageService {

    private final OutboxMessageMapper outboxMessageMapper;
    private final ObjectMapper objectMapper;
    private final boolean messagingEnabled;
    private final String exchange;
    private final String sampleCreatedRoutingKey;
    private final String sampleStatusChangedRoutingKey;

    public OutboxMessageService(
            OutboxMessageMapper outboxMessageMapper,
            ObjectMapper objectMapper,
            @Value("${app.messaging.enabled:false}")
            boolean messagingEnabled,
            @Value("${app.messaging.exchange:labtrace.events}")
            String exchange,
            @Value("${app.messaging.sample-created-routing-key:sample.created}")
            String sampleCreatedRoutingKey,
            @Value("${app.messaging.sample-status-changed-routing-key:sample.status.changed}")
            String sampleStatusChangedRoutingKey
    ) {
        this.outboxMessageMapper = outboxMessageMapper;
        this.objectMapper = objectMapper;
        this.messagingEnabled = messagingEnabled;
        this.exchange = exchange;
        this.sampleCreatedRoutingKey = sampleCreatedRoutingKey;
        this.sampleStatusChangedRoutingKey =
                sampleStatusChangedRoutingKey;
    }

    public void saveSampleCreated(SampleCreatedEvent event) {
        if (!messagingEnabled) {
            return;
        }

        save(
                event.getEventId(),
                "SAMPLE_CREATED",
                sampleCreatedRoutingKey,
                event
        );
    }

    public void saveSampleStatusChanged(
            SampleStatusChangedEvent event
    ) {
        if (!messagingEnabled) {
            return;
        }

        save(
                event.getEventId(),
                "SAMPLE_STATUS_CHANGED",
                sampleStatusChangedRoutingKey,
                event
        );
    }

    private void save(
            String eventId,
            String eventType,
            String routingKey,
            Object event
    ) {
        try {
            OutboxMessage message = new OutboxMessage();
            message.setEventId(eventId);
            message.setEventType(eventType);
            message.setExchangeName(exchange);
            message.setRoutingKey(routingKey);
            message.setPayload(objectMapper.writeValueAsString(event));
            message.setStatus("PENDING");
            message.setRetryCount(0);

            outboxMessageMapper.insert(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "消息事件序列化失败",
                    e
            );
        }
    }
}
