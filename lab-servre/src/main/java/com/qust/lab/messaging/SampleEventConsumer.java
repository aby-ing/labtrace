package com.qust.lab.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qust.lab.mapper.SampleAuditLogMapper;
import com.qust.lab.mapper.SampleMapper;
import com.qust.lab.pojo.entity.Sample;
import com.qust.lab.pojo.entity.SampleAuditLog;
import com.qust.lab.pojo.event.SampleCreatedEvent;
import com.qust.lab.pojo.event.SampleStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "app.messaging",
        name = "enabled",
        havingValue = "true"
)
public class SampleEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(SampleEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final SampleAuditLogMapper auditLogMapper;
    private final SampleMapper sampleMapper;

    public SampleEventConsumer(
            ObjectMapper objectMapper,
            SampleAuditLogMapper auditLogMapper,
            SampleMapper sampleMapper
    ) {
        this.objectMapper = objectMapper;
        this.auditLogMapper = auditLogMapper;
        this.sampleMapper = sampleMapper;
    }

    @RabbitListener(
            queues = "${app.messaging.sample-created-queue}"
    )
    public void consumeSampleCreated(String message) {
        try {
            SampleCreatedEvent event =
                    objectMapper.readValue(
                            message,
                            SampleCreatedEvent.class
                    );

            if (sampleMapper.selectById(event.getSampleId()) == null) {
                log.info(
                        "样品已经删除，忽略创建审计事件: {}",
                        event.getEventId()
                );
                return;
            }

            SampleAuditLog auditLog = new SampleAuditLog();
            auditLog.setEventId(event.getEventId());
            auditLog.setSampleId(event.getSampleId());
            auditLog.setEventType("SAMPLE_CREATED");
            auditLog.setOperatorId(event.getCreatorId());
            auditLog.setToStatus(event.getStatus());
            auditLog.setRemark("样品创建");
            auditLog.setOccurredAt(event.getOccurredAt());

            insertIgnoreDuplicate(auditLog);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "消费样品创建事件失败",
                    e
            );
        }
    }

    @RabbitListener(
            queues = "${app.messaging.sample-status-changed-queue}"
    )
    public void consumeSampleStatusChanged(String message) {
        try {
            SampleStatusChangedEvent event =
                    objectMapper.readValue(
                            message,
                            SampleStatusChangedEvent.class
                    );

            if (sampleMapper.selectById(event.getSampleId()) == null) {
                log.info(
                        "样品已经删除，忽略状态审计事件: {}",
                        event.getEventId()
                );
                return;
            }

            SampleAuditLog auditLog = new SampleAuditLog();
            auditLog.setEventId(event.getEventId());
            auditLog.setSampleId(event.getSampleId());
            auditLog.setEventType("SAMPLE_STATUS_CHANGED");
            auditLog.setOperatorId(event.getOperatorId());
            auditLog.setToUserId(event.getToUserId());
            auditLog.setFromStatus(event.getFromStatus());
            auditLog.setToStatus(event.getToStatus());
            auditLog.setRemark(event.getRemark());
            auditLog.setOccurredAt(event.getOccurredAt());

            insertIgnoreDuplicate(auditLog);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "消费样品状态事件失败",
                    e
            );
        }
    }

    private void insertIgnoreDuplicate(SampleAuditLog auditLog) {
        try {
            auditLogMapper.insert(auditLog);
        } catch (DuplicateKeyException e) {
            log.info(
                    "审计事件已经处理过，忽略重复消息: {}",
                    auditLog.getEventId()
            );
        }
    }
}
