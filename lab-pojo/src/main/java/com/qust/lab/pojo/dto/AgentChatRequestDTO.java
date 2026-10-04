package com.qust.lab.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AgentChatRequestDTO {

    @NotNull(message = "样品 ID 不能为空")
    @Positive(message = "样品 ID 必须大于 0")
    private Long sampleId;

    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题不能超过 500 个字符")
    private String question;

    public AgentChatRequestDTO() {
    }

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
