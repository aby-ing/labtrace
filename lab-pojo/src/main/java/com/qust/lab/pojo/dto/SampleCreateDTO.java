package com.qust.lab.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SampleCreateDTO {

    @NotBlank(message = "样品名称不能为空")
    @Size(max = 100, message = "样品名称不能超过 100 个字符")
    private String sampleName;

    @NotBlank(message = "来源实验室不能为空")
    @Size(max = 100, message = "来源实验室不能超过 100 个字符")
    private String sourceLab;

    @NotBlank(message = "风险等级不能为空")
    @Pattern(
            regexp = "LOW|MEDIUM|HIGH",
            message = "风险等级只能是 LOW、MEDIUM 或 HIGH"
    )
    private String riskLevel;

    public SampleCreateDTO() {
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
}