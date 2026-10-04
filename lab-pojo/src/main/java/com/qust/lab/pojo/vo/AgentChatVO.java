package com.qust.lab.pojo.vo;

public class AgentChatVO {

    private Long sampleId;
    private String question;
    private String answer;
    private String model;
    private Boolean remoteModelUsed;

    public AgentChatVO() {
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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Boolean getRemoteModelUsed() {
        return remoteModelUsed;
    }

    public void setRemoteModelUsed(Boolean remoteModelUsed) {
        this.remoteModelUsed = remoteModelUsed;
    }
}
