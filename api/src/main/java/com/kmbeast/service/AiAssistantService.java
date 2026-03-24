package com.kmbeast.service;

import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.AiAssistantChatDto;
import com.kmbeast.pojo.vo.AiAssistantResponseVO;

/**
 * AI 助手服务
 */
public interface AiAssistantService {

    Result<AiAssistantResponseVO> chat(AiAssistantChatDto chatDto);
}
