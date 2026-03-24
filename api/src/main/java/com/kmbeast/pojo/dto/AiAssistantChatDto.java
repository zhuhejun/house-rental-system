package com.kmbeast.pojo.dto;

import lombok.Data;

/**
 * AI 助手对话入参
 */
@Data
public class AiAssistantChatDto {

    /**
     * 场景：house-search / business-qa
     */
    private String scene;

    /**
     * 用户输入内容
     */
    private String message;
}
