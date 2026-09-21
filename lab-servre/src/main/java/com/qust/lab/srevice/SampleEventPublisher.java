package com.qust.lab.srevice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qust.lab.pojo.entity.OutboxMessage;
import com.qust.lab.pojo.event.SampleCreatedEvent;
import com.qust.lab.pojo.event.SampleStatusChangedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SampleEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final boolean messagingEnabled;
    private final String exchange;
    private final String routingKey;
    private final String statusChangedRoutingKey;

    public SampleEventPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${app.messaging.enabled:false}")
            boolean messagingEnabled,
            @Value("${app.messaging.exchange:labtrace.events}")
            String exchange,
            @Value("${app.messaging.sample-created-routing-key:sample.created}")
            String routingKey,
            @Value("${app.messaging.sample-status-changed-routing-key:sample.status.changed}")
            String statusChangedRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.messagingEnabled = messagingEnabled;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.statusChangedRoutingKey = statusChangedRoutingKey;
    }

    public void publishSampleCreated(SampleCreatedEvent event) {
        if (!messagingEnabled) {
            return;
        }

        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "样品创建事件序列化失败",
                    e
            );
        }
    }

    public void publish(OutboxMessage message) {
        rabbitTemplate.convertAndSend(
                message.getExchangeName(),
                message.getRoutingKey(),
                message.getPayload()
        );
    }

    public void publishSampleStatusChanged(
            SampleStatusChangedEvent event
    ) {
        if (!messagingEnabled) {
            return;
        }

        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(
                    exchange,
                    statusChangedRoutingKey,
                    message
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "样品状态变化事件序列化失败",
                    e
            );
        }
    }
}
