package com.qust.lab.pojo.vo;

import java.time.LocalDateTime;

public class SampleHandoverVO {

    private Long id;

    private Long sampleId;

    private Long fromUserId;

    private String fromUserName;

    private Long toUserId;

    private String toUserName;

    private String fromStatus;

    private String fromStatusText;

    private String toStatus;

    private String toStatusText;

    private String remark;

    private LocalDateTime handoverTime;

    public SampleHandoverVO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getFromStatusText() {
        return fromStatusText;
    }

    public void setFromStatusText(String fromStatusText) {
        this.fromStatusText = fromStatusText;
    }

    public String getToStatus() {
        return toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public String getToStatusText() {
        return toStatusText;
    }

    public void setToStatusText(String toStatusText) {
        this.toStatusText = toStatusText;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getHandoverTime() {
        return handoverTime;
    }

    public void setHandoverTime(LocalDateTime handoverTime) {
        this.handoverTime = handoverTime;
    }
}