package com.qust.lab.pojo.dto;

import com.qust.lab.pojo.enums.SampleStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SampleStatusChangeDTO {

    // 目标状态
    @NotNull(message = "目标状态不能为空")
    private SampleStatus status;

    // 新的保管人或接收人
    @NotNull(message = "接收人不能为空")
    @Positive(message = "接收人 ID 必须大于 0")
    private Long toUserId;

    // 状态变更备注
    @Size(max = 255, message = "状态变更备注不能超过 255 个字符")
    private String remark;

    public SampleStatusChangeDTO() {
    }

    public SampleStatus getStatus() {
        return status;
    }

    public void setStatus(SampleStatus status) {
        this.status = status;
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