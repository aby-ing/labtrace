package com.qust.lab.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
        prefix = "app.messaging",
        name = "enabled",
        havingValue = "true"
)
public class RabbitMqConfig {

    @Bean
    public TopicExchange labtraceEventExchange(
            @Value("${app.messaging.exchange}") String exchange
    ) {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue sampleCreatedQueue(
            @Value("${app.messaging.sample-created-queue}") String queue
    ) {
        return new Queue(queue, true);
    }

    @Bean
    public Binding sampleCreatedBinding(
            Queue sampleCreatedQueue,
            TopicExchange labtraceEventExchange,
            @Value("${app.messaging.sample-created-routing-key}")
            String routingKey
    ) {
        return BindingBuilder
                .bind(sampleCreatedQueue)
                .to(labtraceEventExchange)
                .with(routingKey);
    }

    @Bean
    public Queue sampleStatusChangedQueue(
            @Value("${app.messaging.sample-status-changed-queue}")
            String queue
    ) {
        return new Queue(queue, true);
    }

    @Bean
    public Binding sampleStatusChangedBinding(
            Queue sampleStatusChangedQueue,
            TopicExchange labtraceEventExchange,
            @Value("${app.messaging.sample-status-changed-routing-key}")
            String routingKey
    ) {
        return BindingBuilder
                .bind(sampleStatusChangedQueue)
                .to(labtraceEventExchange)
                .with(routingKey);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
