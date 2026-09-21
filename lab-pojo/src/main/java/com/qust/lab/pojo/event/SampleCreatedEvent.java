package com.qust.lab.pojo.event;

import java.time.LocalDateTime;
import java.util.UUID;

public class SampleCreatedEvent {

    private String eventId;
    private Long sampleId;
    private String sampleNo;
    private String sampleName;
    private Long creatorId;
    private String status;
    private String riskLevel;
    private LocalDateTime createdAt;
    private LocalDateTime occurredAt;

    public SampleCreatedEvent() {
    }

    public SampleCreatedEvent(
            Long sampleId,
            String sampleNo,
            String sampleName,
            Long creatorId,
            String status,
            String riskLevel,
            LocalDateTime createdAt
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.sampleId = sampleId;
        this.sampleNo = sampleNo;
        this.sampleName = sampleName;
        this.creatorId = creatorId;
        this.status = status;
        this.riskLevel = riskLevel;
        this.createdAt = createdAt;
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

    public String getSampleNo() {
        return sampleNo;
    }

    public void setSampleNo(String sampleNo) {
        this.sampleNo = sampleNo;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
