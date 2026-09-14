package com.qust.lab.pojo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SampleHandoverCreateDTO {

    // 接收人 ID
    @NotNull(message = "接收人不能为空")
    @Positive(message = "接收人 ID 必须大于 0")
    private Long toUserId;

    // 交接备注
    @Size(max = 255, message = "交接备注不能超过 255 个字符")
    private String remark;

    public SampleHandoverCreateDTO() {
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}