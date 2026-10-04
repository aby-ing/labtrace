package com.qust.lab.srevice;

import com.qust.lab.pojo.dto.AgentChatRequestDTO;
import com.qust.lab.pojo.vo.AgentChatVO;

public interface AgentService {

    AgentChatVO chat(AgentChatRequestDTO dto);
}
