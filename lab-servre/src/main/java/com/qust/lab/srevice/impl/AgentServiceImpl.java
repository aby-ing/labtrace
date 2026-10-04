package com.qust.lab.srevice.impl;

import com.qust.lab.exception.NotFoundException;
import com.qust.lab.exception.ServiceUnavailableException;
import com.qust.lab.pojo.dto.AgentChatRequestDTO;
import com.qust.lab.pojo.vo.AgentChatVO;
import com.qust.lab.pojo.vo.SampleAuditLogVO;
import com.qust.lab.pojo.vo.SampleHandoverVO;
import com.qust.lab.pojo.vo.SampleVO;
import com.qust.lab.srevice.AgentService;
import com.qust.lab.srevice.SampleAuditLogService;
import com.qust.lab.srevice.SampleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class AgentServiceImpl implements AgentService {

    private final SampleService sampleService;
    private final SampleAuditLogService sampleAuditLogService;
    private final RestClient restClient;
    private final boolean enabled;
    private final String apiKey;
    private final String model;
    private final int maxContextLength;

    public AgentServiceImpl(
            SampleService sampleService,
            SampleAuditLogService sampleAuditLogService,
            RestClient.Builder restClientBuilder,
            @Value("${app.agent.enabled:false}") boolean enabled,
            @Value("${app.agent.base-url:https://api.openai.com/v1}")
            String baseUrl,
            @Value("${app.agent.api-key:}") String apiKey,
            @Value("${app.agent.model:gpt-4.1-mini}") String model,
            @Value("${app.agent.max-context-length:12000}")
            int maxContextLength
    ) {
        this.sampleService = sampleService;
        this.sampleAuditLogService = sampleAuditLogService;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.model = model;
        this.maxContextLength = maxContextLength;
    }

    @Override
    public AgentChatVO chat(AgentChatRequestDTO dto) {
        SampleVO sample = sampleService.getById(dto.getSampleId());

        if (sample == null) {
            throw new NotFoundException("样品不存在");
        }

        List<SampleHandoverVO> handovers =
                sampleService.listHandoverHistory(dto.getSampleId());
        List<SampleAuditLogVO> auditLogs =
                sampleAuditLogService.listBySampleId(dto.getSampleId());

        String context = limitContext(
                buildContext(sample, handovers, auditLogs)
        );
        String answer;
        boolean remoteModelUsed = canUseRemoteModel();

        if (remoteModelUsed) {
            answer = callRemoteModel(dto.getQuestion(), context);
        } else {
            answer = buildLocalAnswer(dto.getQuestion(), context);
        }

        AgentChatVO vo = new AgentChatVO();
        vo.setSampleId(dto.getSampleId());
        vo.setQuestion(dto.getQuestion());
        vo.setAnswer(answer);
        vo.setModel(remoteModelUsed ? model : "local-rule-agent");
        vo.setRemoteModelUsed(remoteModelUsed);
        return vo;
    }

    private boolean canUseRemoteModel() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    private String callRemoteModel(String question, String context) {
        Map<String, Object> request = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content",
                                "你是 LabTrace 实验室样品追踪助手。"
                                        + "只能根据用户给你的样品上下文回答，"
                                        + "不要编造不存在的数据。"
                        ),
                        Map.of(
                                "role", "user",
                                "content",
                                "样品上下文：\n" + context
                                        + "\n\n用户问题：\n" + question
                        )
                )
        );

        Map<?, ?> response;

        try {
            response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            throw new ServiceUnavailableException(
                    "Agent 模型暂时不可用，请稍后重试",
                    e
            );
        }

        return readAnswer(response);
    }

    private String limitContext(String context) {
        if (context.length() <= maxContextLength) {
            return context;
        }

        return context.substring(0, maxContextLength)
                + "\n\n[上下文过长，后续记录已省略]";
    }

    private String readAnswer(Map<?, ?> response) {
        if (response == null) {
            throw new IllegalStateException("Agent 模型没有返回内容");
        }

        Object choicesObject = response.get("choices");

        if (!(choicesObject instanceof List<?> choices)
                || choices.isEmpty()) {
            throw new IllegalStateException("Agent 模型返回格式不正确");
        }

        Object firstChoice = choices.get(0);

        if (!(firstChoice instanceof Map<?, ?> choice)) {
            throw new IllegalStateException("Agent 模型返回格式不正确");
        }

        Object messageObject = choice.get("message");

        if (!(messageObject instanceof Map<?, ?> message)) {
            throw new IllegalStateException("Agent 模型返回格式不正确");
        }

        Object content = message.get("content");

        if (!(content instanceof String answer) || answer.isBlank()) {
            throw new IllegalStateException("Agent 模型没有返回回答");
        }

        return answer;
    }

    private String buildLocalAnswer(String question, String context) {
        return "当前还没有开启远程 Agent 模型，下面是系统根据样品数据整理的上下文。\n\n"
                + "你的问题：" + question + "\n\n"
                + context
                + "\n\n如果要让 AI 自动分析这段内容，"
                + "需要配置 app.agent.api-key 并开启 app.agent.enabled。";
    }

    private String buildContext(
            SampleVO sample,
            List<SampleHandoverVO> handovers,
            List<SampleAuditLogVO> auditLogs
    ) {
        StringBuilder builder = new StringBuilder();

        builder.append("样品编号：").append(sample.getSampleNo())
                .append('\n');
        builder.append("样品名称：").append(sample.getSampleName())
                .append('\n');
        builder.append("来源实验室：").append(sample.getSourceLab())
                .append('\n');
        builder.append("风险等级：").append(sample.getRiskLevel())
                .append('\n');
        builder.append("当前状态：").append(sample.getStatusText())
                .append('\n');
        builder.append("创建人：").append(sample.getCreatorName())
                .append('\n');
        builder.append("当前责任人：").append(sample.getCustodianName())
                .append('\n');
        builder.append("创建时间：").append(sample.getCreatedAt())
                .append("\n\n");

        builder.append("交接记录：\n");
        if (handovers.isEmpty()) {
            builder.append("- 暂无交接记录\n");
        } else {
            for (SampleHandoverVO handover : handovers) {
                builder.append("- ")
                        .append(handover.getFromUserName())
                        .append(" -> ")
                        .append(handover.getToUserName())
                        .append("，状态 ")
                        .append(handover.getFromStatusText())
                        .append(" -> ")
                        .append(handover.getToStatusText())
                        .append("，时间：")
                        .append(handover.getHandoverTime())
                        .append("，备注：")
                        .append(handover.getRemark())
                        .append('\n');
            }
        }

        builder.append("\n审计日志：\n");
        if (auditLogs.isEmpty()) {
            builder.append("- 暂无审计日志\n");
        } else {
            for (SampleAuditLogVO log : auditLogs) {
                builder.append("- ")
                        .append(log.getOccurredAt())
                        .append("，")
                        .append(log.getOperatorName())
                        .append("，")
                        .append(log.getEventType())
                        .append("，状态 ")
                        .append(log.getFromStatusText())
                        .append(" -> ")
                        .append(log.getToStatusText())
                        .append("，备注：")
                        .append(log.getRemark())
                        .append('\n');
            }
        }

        return builder.toString();
    }
}
