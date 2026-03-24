package com.kmbeast.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * AI 助手响应
 */
@Data
public class AiAssistantResponseVO {

    private String scene;

    private String intent;

    private String answer;

    private String provider;

    private String recommendedScene;

    private String recommendedSceneLabel;

    private AiHouseSearchFilterVO parsedFilters;

    private List<String> suggestions;

    private List<HouseListItemVO> houseList;
}
