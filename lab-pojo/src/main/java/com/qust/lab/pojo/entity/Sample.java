package com.qust.lab.pojo.entity;
import java.time.LocalDateTime;
public class Sample {
    private Long id;

    // 对外展示和扫码使用的唯一编号
    private String sampleNo;

    // 样品名称
    private String sampleName;

    // 样品产生的实验室
    private String sourceLab;

    // 创建样品的用户 ID
    private Long creatorId;

    // 当前保管人 ID
    private Long custodianId;

    // 当前状态：CREATED、HANDED_OVER、STORED、TESTING、COMPLETED、ABNORMAL
    private String status;

    // 风险等级：LOW、MEDIUM、HIGH
    private String riskLevel;

    // 乐观锁版本号
    private Integer version;

    private LocalDateTime createdAt;

    public Sample() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSourceLab() {
        return sourceLab;
    }

    public void setSourceLab(String sourceLab) {
        this.sourceLab = sourceLab;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getCustodianId() {
        return custodianId;
    }

    public void setCustodianId(Long custodianId) {
        this.custodianId = custodianId;
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
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
