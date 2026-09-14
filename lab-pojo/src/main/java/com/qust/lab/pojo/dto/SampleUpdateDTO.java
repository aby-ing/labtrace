package com.qust.lab.pojo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SampleUpdateDTO {

    // 修改时没传的字段，保留数据库原值
    @Size(max = 100, message = "样品名称不能超过 100 个字符")
    private String sampleName;

    @Size(max = 100, message = "来源实验室不能超过 100 个字符")
    private String sourceLab;

    @Pattern(
            regexp = "LOW|MEDIUM|HIGH",
            message = "风险等级只能是 LOW、MEDIUM 或 HIGH"
    )
    private String riskLevel;

    @NotNull(message = "version 不能为空")
    @Min(value = 0, message = "version 不能小于 0")
    private Integer version;

    public SampleUpdateDTO() {
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
}