package com.qust.lab.pojo.event;

import java.time.LocalDateTime;
import java.util.UUID;

public class SampleStatusChangedEvent {

    private String eventId;
    private Long sampleId;
    private Long operatorId;
    private Long toUserId;
    private String fromStatus;
    private String toStatus;
    private String remark;
    private LocalDateTime occurredAt;

    public SampleStatusChangedEvent() {
    }

    public SampleStatusChangedEvent(
            Long sampleId,
            Long operatorId,
            Long toUserId,
            String fromStatus,
            String toStatus,
            String remark
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.sampleId = sampleId;
        this.operatorId = operatorId;
        this.toUserId = toUserId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.remark = remark;
        this.occurredAt = LocalDateTime.now();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
