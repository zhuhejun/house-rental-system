package com.kmbeast.controller;

import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.AiAssistantChatDto;
import com.kmbeast.pojo.vo.AiAssistantResponseVO;
import com.kmbeast.service.AiAssistantService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * AI 助手控制器
 */
@RestController
@RequestMapping("/ai-assistant")
public class AiAssistantController {

    @Resource
    private AiAssistantService aiAssistantService;

    @PostMapping(value = "/chat")
    @ResponseBody
    public Result<AiAssistantResponseVO> chat(@RequestBody AiAssistantChatDto chatDto) {
        return aiAssistantService.chat(chatDto);
    }
}
