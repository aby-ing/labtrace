package com.qust.lab.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SamplePageQueryDTO {

    // 第几页
    @Min(value = 1, message = "页码必须大于 0")
    private Long page = 1L;

    // 每页条数
    @Min(value = 1, message = "每页条数必须大于 0")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Long pageSize = 10L;

    // 样品名称，支持模糊查询
    @Size(max = 100, message = "样品名称查询条件不能超过 100 个字符")
    private String sampleName;

    // 样品状态
    @Pattern(
            regexp = "CREATED|HANDED_OVER|STORED|TESTING|COMPLETED|ABNORMAL",
            message = "样品状态不合法"
    )
    private String status;

    // 风险等级
    @Pattern(
            regexp = "LOW|MEDIUM|HIGH",
            message = "风险等级只能是 LOW、MEDIUM 或 HIGH"
    )
    private String riskLevel;

    public SamplePageQueryDTO() {
    }

    public Long getPage() {
        return page;
    }

    public void setPage(Long page) {
        this.page = page;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
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
}
