package com.qust.lab.controller;

import com.qust.lab.common.result.Result;
import com.qust.lab.pojo.dto.AgentChatRequestDTO;
import com.qust.lab.pojo.vo.AgentChatVO;
import com.qust.lab.srevice.AgentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/chat")
    public Result<AgentChatVO> chat(
            @Valid @RequestBody AgentChatRequestDTO dto
    ) {
        return Result.success(agentService.chat(dto));
    }
}
