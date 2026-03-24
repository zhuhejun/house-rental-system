package com.kmbeast.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.AreaMapper;
import com.kmbeast.mapper.HouseMapper;
import com.kmbeast.mapper.RepairOrderMapper;
import com.kmbeast.mapper.RentalBillMapper;
import com.kmbeast.mapper.RentalContractMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.AiAssistantChatDto;
import com.kmbeast.pojo.dto.HouseQueryDto;
import com.kmbeast.pojo.dto.RepairOrderQueryDto;
import com.kmbeast.pojo.dto.RentalBillQueryDto;
import com.kmbeast.pojo.dto.RentalContractQueryDto;
import com.kmbeast.pojo.em.HouseDirectionEnum;
import com.kmbeast.pojo.em.HouseDepositEnum;
import com.kmbeast.pojo.em.HouseFitmentEnum;
import com.kmbeast.pojo.em.HouseSizedEnum;
import com.kmbeast.pojo.em.HouseStatusEnum;
import com.kmbeast.pojo.em.RepairOrderStatusEnum;
import com.kmbeast.pojo.em.RepairPaymentStatusEnum;
import com.kmbeast.pojo.em.RentalTypeEnum;
import com.kmbeast.pojo.em.RentalBillPayStatusEnum;
import com.kmbeast.pojo.em.RentalBillTypeEnum;
import com.kmbeast.pojo.em.RentalContractStatusEnum;
import com.kmbeast.pojo.em.UtilityPaymentModeEnum;
import com.kmbeast.pojo.entity.Area;
import com.kmbeast.pojo.vo.AiAssistantResponseVO;
import com.kmbeast.pojo.vo.AiHouseSearchFilterVO;
import com.kmbeast.pojo.vo.HouseListItemVO;
import com.kmbeast.pojo.vo.RepairOrderVO;
import com.kmbeast.pojo.vo.RentalBillVO;
import com.kmbeast.pojo.vo.RentalContractVO;
import com.kmbeast.service.AiAssistantService;
import com.kmbeast.utils.AssertUtils;
import com.kmbeast.utils.DashScopeAiClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI 助手第一版实现
 */
@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final String SCENE_HOUSE_SEARCH = "house-search";
    private static final String SCENE_BUSINESS_QA = "business-qa";
    private static final String SCENE_GENERAL_QA = "general-qa";
    private static final int DEFAULT_RESULT_LIMIT = 6;
    private static final Pattern RENT_RANGE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[-~到至]\\s*(\\d+(?:\\.\\d+)?)\\s*(万|w|W|元|块)?");
    private static final Pattern MAX_RENT_PATTERN = Pattern.compile("(?:预算|不超过|最多|租金|月租)?\\s*(\\d+(?:\\.\\d+)?)\\s*(万|w|W|元|块)?\\s*(?:以内|以下|之内|左右|上下)?");
    private static final Pattern SIZE_RANGE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[-~到至]\\s*(\\d+(?:\\.\\d+)?)\\s*(?:㎡|平|平米)");
    private static final Pattern MIN_SIZE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:㎡|平|平米)(?:以上|起)");
    private static final Pattern MAX_SIZE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:㎡|平|平米)(?:以内|以下)");

    @Resource
    private HouseMapper houseMapper;
    @Resource
    private AreaMapper areaMapper;
    @Resource
    private RentalContractMapper rentalContractMapper;
    @Resource
    private RentalBillMapper rentalBillMapper;
    @Resource
    private RepairOrderMapper repairOrderMapper;
    @Resource
    private DashScopeAiClient dashScopeAiClient;

    @Override
    public Result<AiAssistantResponseVO> chat(AiAssistantChatDto chatDto) {
        AssertUtils.notNull(chatDto, "请求不能为空");
        AssertUtils.hasText(chatDto.getScene(), "请先选择助手场景");
        AssertUtils.hasText(chatDto.getMessage(), "请输入你想咨询的内容");
        String scene = chatDto.getScene().trim();
        if (SCENE_HOUSE_SEARCH.equals(scene)) {
            return ApiResult.success(handleHouseSearch(chatDto.getMessage()));
        }
        if (SCENE_BUSINESS_QA.equals(scene)) {
            return ApiResult.success(handleBusinessQa(chatDto.getMessage()));
        }
        if (SCENE_GENERAL_QA.equals(scene)) {
            return ApiResult.success(handleGeneralQa(chatDto.getMessage()));
        }
        return ApiResult.error("暂不支持当前助手场景");
    }

    private AiAssistantResponseVO handleHouseSearch(String message) {
        String text = message == null ? "" : message.trim();
        if (shouldRedirectToBusinessQa(text)) {
            return buildSceneRedirectResponse(
                    SCENE_HOUSE_SEARCH,
                    "场景引导",
                    "这个问题更适合放在“业务问答”里处理，我可以结合平台里的合同、押金、账单、退租和报修规则来回答你。",
                    SCENE_BUSINESS_QA,
                    "业务问答",
                    Arrays.asList("退租流程怎么走？", "我现在下一步做什么？", "报修费用什么时候支付？")
            );
        }
        if (shouldRedirectToGeneralQa(text)) {
            return buildSceneRedirectResponse(
                    SCENE_HOUSE_SEARCH,
                    "场景引导",
                    "这个问题不像找房需求，更适合切到“通用问答”直接让千问回答，比如天气、常识、写作或闲聊。",
                    SCENE_GENERAL_QA,
                    "通用问答",
                    Arrays.asList("桂林的天气怎么样？", "帮我写一段礼貌的请假说明")
            );
        }
        AiHouseSearchFilterVO filters = parseHouseSearchFilters(message);
        HouseQueryDto queryDto = buildHouseQuery(filters);
        List<HouseListItemVO> houseList = houseMapper.list(queryDto);
        enrichHouseList(houseList);
        if (filters.getFacilities() != null && !filters.getFacilities().isEmpty()) {
            houseList = houseList.stream()
                    .filter(item -> containsFacilities(item.getLivingFacilities(), filters.getFacilities()))
                    .collect(Collectors.toList());
        }
        if (houseList.size() > DEFAULT_RESULT_LIMIT) {
            houseList = houseList.subList(0, DEFAULT_RESULT_LIMIT);
        }

        AiAssistantResponseVO responseVO = new AiAssistantResponseVO();
        responseVO.setScene(SCENE_HOUSE_SEARCH);
        responseVO.setIntent("智能找房");
        responseVO.setParsedFilters(filters);
        responseVO.setHouseList(houseList);
        responseVO.setSuggestions(Arrays.asList(
                "再加一个预算上限，比如“3000以内”",
                "补充户型，例如“一室一厅”或“两室一厅”",
                "说明偏好设施，例如“带空调和阳台”"
        ));
        String localAnswer = buildHouseSearchAnswer(filters, houseList);
        String aiAnswer = buildHouseSearchAiAnswer(message, filters, houseList, localAnswer);
        responseVO.setProvider(StringUtils.hasText(aiAnswer) ? "千问" : "本地规则兜底");
        responseVO.setAnswer(StringUtils.hasText(aiAnswer) ? aiAnswer : localAnswer);
        return responseVO;
    }

    private AiAssistantResponseVO handleBusinessQa(String message) {
        String text = message == null ? "" : message.trim();
        if (shouldRedirectToHouseSearch(text)) {
            return buildSceneRedirectResponse(
                    SCENE_BUSINESS_QA,
                    "场景引导",
                    "这个问题更像找房需求，切到“智能找房”后我可以按区域、预算、户型和设施去筛真实房源。",
                    SCENE_HOUSE_SEARCH,
                    "智能找房",
                    Arrays.asList("帮我找深圳南山 3000 以内的一室一厅", "预算 5000，想要整租，最好带空调和阳台")
            );
        }
        if (shouldRedirectToGeneralQa(text)) {
            return buildSceneRedirectResponse(
                    SCENE_BUSINESS_QA,
                    "场景引导",
                    "这个问题不属于平台业务流程，更适合切到“通用问答”直接让千问回答。",
                    SCENE_GENERAL_QA,
                    "通用问答",
                    Arrays.asList("桂林的天气怎么样？", "什么是协同过滤推荐？")
            );
        }
        UserBusinessContext context = buildUserBusinessContext();
        AiAssistantResponseVO responseVO = new AiAssistantResponseVO();
        responseVO.setScene(SCENE_BUSINESS_QA);
        responseVO.setSuggestions(buildBusinessQaSuggestions(context));

        if (containsAny(text, "我下一步", "我现在", "我的待办", "待办", "当前要做什么", "下一步做什么")) {
            responseVO.setIntent("个人待办");
            String localAnswer = buildPendingActionAnswer(context);
            responseVO.setAnswer(rewriteBusinessAnswer(text, context, localAnswer, responseVO));
            return responseVO;
        }

        if (containsAny(text, "退租", "解约")) {
            responseVO.setIntent("退租流程");
            String localAnswer = buildTerminateAnswer(context);
            responseVO.setAnswer(rewriteBusinessAnswer(text, context, localAnswer, responseVO));
            return responseVO;
        }
        if (containsAny(text, "报修", "维修")) {
            responseVO.setIntent("报修流程");
            String localAnswer = buildRepairAnswer(context);
            responseVO.setAnswer(rewriteBusinessAnswer(text, context, localAnswer, responseVO));
            return responseVO;
        }
        if (containsAny(text, "押金", "合同确认", "签合同")) {
            responseVO.setIntent("合同与押金");
            String localAnswer = buildContractAnswer(context);
            responseVO.setAnswer(rewriteBusinessAnswer(text, context, localAnswer, responseVO));
            return responseVO;
        }
        if (containsAny(text, "水电", "自行缴费", "房东结算")) {
            responseVO.setIntent("水电支付");
            String localAnswer = buildUtilityAnswer(context);
            responseVO.setAnswer(rewriteBusinessAnswer(text, context, localAnswer, responseVO));
            return responseVO;
        }
        if (containsAny(text, "账单", "房租", "支付")) {
            responseVO.setIntent("账单支付");
            String localAnswer = buildBillAnswer(context);
            responseVO.setAnswer(rewriteBusinessAnswer(text, context, localAnswer, responseVO));
            return responseVO;
        }
        responseVO.setIntent("平台问答");
        String localAnswer = buildGenericQaAnswer(context);
        responseVO.setAnswer(rewriteBusinessAnswer(text, context, localAnswer, responseVO));
        return responseVO;
    }

    private AiAssistantResponseVO handleGeneralQa(String message) {
        String text = message == null ? "" : message.trim();
        if (shouldRedirectToHouseSearch(text)) {
            return buildSceneRedirectResponse(
                    SCENE_GENERAL_QA,
                    "场景引导",
                    "这个问题更适合切到“智能找房”，那边会按真实房源数据帮你筛选，不会只给泛泛建议。",
                    SCENE_HOUSE_SEARCH,
                    "智能找房",
                    Arrays.asList("帮我找北京市的房源", "预算 3000，找一室一厅")
            );
        }
        if (shouldRedirectToBusinessQa(text)) {
            return buildSceneRedirectResponse(
                    SCENE_GENERAL_QA,
                    "场景引导",
                    "这个问题更适合切到“业务问答”，那边会按平台里的合同、账单、退租和报修规则给你准确回复。",
                    SCENE_BUSINESS_QA,
                    "业务问答",
                    Arrays.asList("退租流程怎么走？", "我现在有待支付账单吗？")
            );
        }
        AiAssistantResponseVO responseVO = new AiAssistantResponseVO();
        responseVO.setScene(SCENE_GENERAL_QA);
        responseVO.setIntent("通用问答");
        responseVO.setSuggestions(Arrays.asList(
                "桂林今天的天气怎么样？",
                "帮我写一段礼貌的请假说明",
                "解释一下什么是协同过滤推荐",
                "周末适合做什么放松一下？"
        ));

        String aiAnswer = buildGeneralQaAnswer(text);
        if (StringUtils.hasText(aiAnswer)) {
            responseVO.setProvider("千问");
            responseVO.setAnswer(aiAnswer);
            return responseVO;
        }

        responseVO.setProvider("本地规则兜底");
        responseVO.setAnswer("当前通用问答需要千问服务支持。你可以稍后再试，或者切换到“智能找房”“业务问答”继续使用平台内的找房和流程咨询能力。");
        return responseVO;
    }

    private AiAssistantResponseVO buildSceneRedirectResponse(String scene,
                                                             String intent,
                                                             String answer,
                                                             String recommendedScene,
                                                             String recommendedSceneLabel,
                                                             List<String> suggestions) {
        AiAssistantResponseVO responseVO = new AiAssistantResponseVO();
        responseVO.setScene(scene);
        responseVO.setIntent(intent);
        responseVO.setAnswer(answer);
        responseVO.setProvider("场景引导");
        responseVO.setRecommendedScene(recommendedScene);
        responseVO.setRecommendedSceneLabel(recommendedSceneLabel);
        responseVO.setSuggestions(suggestions);
        return responseVO;
    }

    private AiHouseSearchFilterVO parseHouseSearchFilters(String message) {
        String text = message == null ? "" : message.trim();
        AiHouseSearchFilterVO filters = new AiHouseSearchFilterVO();
        resolveArea(text, filters);
        resolveRent(text, filters);
        resolveSize(text, filters);
        resolveSized(text, filters);
        resolveRentalType(text, filters);
        resolveDirection(text, filters);
        resolveFitment(text, filters);
        resolveSubway(text, filters);
        resolveFacilities(text, filters);
        return filters;
    }

    private void resolveArea(String text, AiHouseSearchFilterVO filters) {
        List<Area> areaList = areaMapper.selectList(null);
        if (areaList == null || areaList.isEmpty()) {
            return;
        }
        Map<Integer, Area> areaMap = areaList.stream().collect(Collectors.toMap(Area::getId, item -> item));
        List<Area> matchedList = areaList.stream()
                .filter(area -> matchAreaScore(text, area, areaMap) > 0)
                .sorted((a, b) -> {
                    int scoreCompare = Integer.compare(matchAreaScore(b, b, areaMap, text), matchAreaScore(a, a, areaMap, text));
                    if (scoreCompare != 0) {
                        return scoreCompare;
                    }
                    int depthCompare = Integer.compare(getAreaDepth(b, areaMap), getAreaDepth(a, areaMap));
                    if (depthCompare != 0) {
                        return depthCompare;
                    }
                    return Integer.compare(b.getName().length(), a.getName().length());
                })
                .collect(Collectors.toList());
        if (matchedList.isEmpty()) {
            return;
        }

        Area target = matchedList.get(0);
        filters.setAreaId(target.getId());
        List<Area> chain = buildAreaChain(target, areaMap);
        if (chain.size() == 1) {
            filters.setCity(target.getName());
            return;
        }
        if (chain.size() == 2) {
            filters.setCity(chain.get(0).getName());
            filters.setDistrict(chain.get(1).getName());
            return;
        }
        filters.setCity(chain.get(chain.size() - 2).getName());
        filters.setDistrict(chain.get(chain.size() - 1).getName());
    }

    private int matchAreaScore(String text, Area area, Map<Integer, Area> areaMap) {
        return matchAreaScore(area, area, areaMap, text);
    }

    private int matchAreaScore(Area sortTarget, Area area, Map<Integer, Area> areaMap, String text) {
        if (!StringUtils.hasText(text) || area == null || !StringUtils.hasText(area.getName())) {
            return -1;
        }
        int depth = getAreaDepth(sortTarget, areaMap);
        String fullName = area.getName().trim();
        if (text.contains(fullName)) {
            return 1000 + depth * 10 + fullName.length();
        }
        for (String alias : buildAreaAliases(fullName)) {
            if (alias.length() < 2) {
                continue;
            }
            if (text.contains(alias)) {
                return 500 + depth * 10 + alias.length();
            }
        }
        return -1;
    }

    private List<String> buildAreaAliases(String fullName) {
        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        String normalized = normalizeAreaName(fullName);
        if (StringUtils.hasText(normalized)) {
            aliases.add(normalized);
        }
        if (fullName.endsWith("市") || fullName.endsWith("区") || fullName.endsWith("县")) {
            String trimmed = fullName.substring(0, fullName.length() - 1).trim();
            if (trimmed.length() >= 2) {
                aliases.add(trimmed);
            }
        }
        return new ArrayList<>(aliases);
    }

    private void resolveRent(String text, AiHouseSearchFilterVO filters) {
        Matcher rangeMatcher = RENT_RANGE_PATTERN.matcher(text);
        if (rangeMatcher.find() && containsAny(text, "预算", "租金", "月租", "元", "块", "万", "w", "W")) {
            filters.setMinRent(toMoney(rangeMatcher.group(1), rangeMatcher.group(3)));
            filters.setMaxRent(toMoney(rangeMatcher.group(2), rangeMatcher.group(3)));
            return;
        }

        Matcher maxMatcher = MAX_RENT_PATTERN.matcher(text);
        BigDecimal maxRent = null;
        while (maxMatcher.find()) {
            String full = maxMatcher.group(0);
            if (!containsAny(full, "预算", "不超过", "最多", "以内", "以下", "之内", "左右", "上下", "租金", "月租")) {
                continue;
            }
            maxRent = toMoney(maxMatcher.group(1), maxMatcher.group(2));
            break;
        }
        if (maxRent != null) {
            filters.setMinRent(BigDecimal.ZERO);
            filters.setMaxRent(maxRent);
        }
    }

    private void resolveSize(String text, AiHouseSearchFilterVO filters) {
        Matcher rangeMatcher = SIZE_RANGE_PATTERN.matcher(text);
        if (rangeMatcher.find()) {
            filters.setMinSizeNumber(Double.parseDouble(rangeMatcher.group(1)));
            filters.setMaxSizeNumber(Double.parseDouble(rangeMatcher.group(2)));
            return;
        }
        Matcher minMatcher = MIN_SIZE_PATTERN.matcher(text);
        if (minMatcher.find()) {
            filters.setMinSizeNumber(Double.parseDouble(minMatcher.group(1)));
        }
        Matcher maxMatcher = MAX_SIZE_PATTERN.matcher(text);
        if (maxMatcher.find()) {
            filters.setMaxSizeNumber(Double.parseDouble(maxMatcher.group(1)));
        }
    }

    private void resolveSized(String text, AiHouseSearchFilterVO filters) {
        Map<Integer, String> sizedMap = new LinkedHashMap<>();
        sizedMap.put(1, "一室一厅");
        sizedMap.put(2, "两室一厅");
        sizedMap.put(3, "三室一厅");
        sizedMap.put(4, "单间");
        sizedMap.put(5, "四室一厅");
        for (Map.Entry<Integer, String> entry : sizedMap.entrySet()) {
            if (text.contains(entry.getValue())) {
                filters.setSizedId(entry.getKey());
                filters.setSizedName(HouseSizedEnum.getDetail(entry.getKey()));
                return;
            }
        }
        if (containsAny(text, "大单间", "单间")) {
            filters.setSizedId(4);
            filters.setSizedName(HouseSizedEnum.getDetail(4));
        }
    }

    private void resolveRentalType(String text, AiHouseSearchFilterVO filters) {
        if (text.contains("合租")) {
            filters.setRentalType(2);
            filters.setRentalTypeName(RentalTypeEnum.getDetail(2));
            return;
        }
        if (text.contains("整租")) {
            filters.setRentalType(1);
            filters.setRentalTypeName(RentalTypeEnum.getDetail(1));
        }
    }

    private void resolveDirection(String text, AiHouseSearchFilterVO filters) {
        LinkedHashMap<Integer, String> directionMap = new LinkedHashMap<>();
        directionMap.put(6, "南北通透");
        directionMap.put(5, "坐北朝南");
        directionMap.put(3, "朝南");
        directionMap.put(4, "朝北");
        directionMap.put(1, "朝东");
        directionMap.put(2, "朝西");
        for (Map.Entry<Integer, String> entry : directionMap.entrySet()) {
            if (text.contains(entry.getValue()) || text.contains(HouseDirectionEnum.getDetail(entry.getKey()))) {
                filters.setDirectionId(entry.getKey());
                filters.setDirectionName(HouseDirectionEnum.getDetail(entry.getKey()));
                return;
            }
        }
    }

    private void resolveFitment(String text, AiHouseSearchFilterVO filters) {
        if (text.contains("精装")) {
            filters.setFitmentStatusId(3);
            filters.setFitmentStatusName(HouseFitmentEnum.getDetail(3));
            return;
        }
        if (text.contains("简装")) {
            filters.setFitmentStatusId(2);
            filters.setFitmentStatusName(HouseFitmentEnum.getDetail(2));
            return;
        }
        if (text.contains("毛坯")) {
            filters.setFitmentStatusId(1);
            filters.setFitmentStatusName(HouseFitmentEnum.getDetail(1));
        }
    }

    private void resolveSubway(String text, AiHouseSearchFilterVO filters) {
        if (containsAny(text, "地铁", "近地铁", "临近地铁")) {
            filters.setIsSubway(true);
        }
    }

    private void resolveFacilities(String text, AiHouseSearchFilterVO filters) {
        List<String> facilities = new ArrayList<>();
        List<String> candidates = Arrays.asList("空调", "阳台", "衣柜", "床", "冰箱", "洗衣机", "热水器", "宽带", "沙发", "电视", "暖气", "燃气灶", "可做饭");
        for (String candidate : candidates) {
            if (text.contains(candidate)) {
                facilities.add(candidate);
            }
        }
        if (!facilities.isEmpty()) {
            filters.setFacilities(facilities);
        }
    }

    private HouseQueryDto buildHouseQuery(AiHouseSearchFilterVO filters) {
        HouseQueryDto queryDto = new HouseQueryDto();
        queryDto.setCurrent(0);
        queryDto.setSize(80);
        queryDto.setStatus(HouseStatusEnum.STATUS_1.getType());
        List<Integer> areaIds = buildAreaIds(filters.getAreaId());
        if (!areaIds.isEmpty()) {
            queryDto.setAreaIds(areaIds);
        } else {
            queryDto.setAreaId(filters.getAreaId());
        }
        queryDto.setDirectionId(filters.getDirectionId());
        queryDto.setSizedId(filters.getSizedId());
        queryDto.setRentalType(filters.getRentalType());
        queryDto.setFitmentStatusId(filters.getFitmentStatusId());
        queryDto.setIsSubway(filters.getIsSubway());
        if (filters.getMinRent() != null || filters.getMaxRent() != null) {
            queryDto.setMinRent(filters.getMinRent() == null ? BigDecimal.ZERO : filters.getMinRent());
            queryDto.setMaxRent(filters.getMaxRent() == null ? new BigDecimal("99999999") : filters.getMaxRent());
        }
        if (filters.getMinSizeNumber() != null || filters.getMaxSizeNumber() != null) {
            queryDto.setMinSizeNumber(filters.getMinSizeNumber() == null ? 0D : filters.getMinSizeNumber());
            queryDto.setMaxSizeNumber(filters.getMaxSizeNumber() == null ? 99999D : filters.getMaxSizeNumber());
        }
        return queryDto;
    }

    private List<Integer> buildAreaIds(Integer areaId) {
        if (areaId == null) {
            return Collections.emptyList();
        }
        List<Area> areaList = areaMapper.selectList(null);
        if (areaList == null || areaList.isEmpty()) {
            return Collections.singletonList(areaId);
        }
        Map<Integer, List<Integer>> childrenMap = new HashMap<>();
        for (Area area : areaList) {
            if (area.getParentId() == null) {
                continue;
            }
            childrenMap.computeIfAbsent(area.getParentId(), key -> new ArrayList<>()).add(area.getId());
        }
        LinkedHashSet<Integer> result = new LinkedHashSet<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(areaId);
        while (!stack.isEmpty()) {
            Integer currentId = stack.pop();
            if (!result.add(currentId)) {
                continue;
            }
            List<Integer> children = childrenMap.get(currentId);
            if (children == null || children.isEmpty()) {
                continue;
            }
            for (Integer childId : children) {
                stack.push(childId);
            }
        }
        return new ArrayList<>(result);
    }

    private void enrichHouseList(List<HouseListItemVO> houseList) {
        if (houseList == null) {
            return;
        }
        for (HouseListItemVO item : houseList) {
            if (item.getDirectionId() != null) {
                item.setDirectionName(HouseDirectionEnum.getDetail(item.getDirectionId()));
            }
            if (item.getFitmentStatusId() != null) {
                item.setFitmentStatusName(HouseFitmentEnum.getDetail(item.getFitmentStatusId()));
            }
            if (item.getDepositMethodId() != null) {
                item.setDepositMethodName(HouseDepositEnum.getDetail(item.getDepositMethodId()));
            }
        }
    }

    private boolean containsFacilities(String livingFacilities, List<String> facilities) {
        if (livingFacilities == null || livingFacilities.trim().isEmpty()) {
            return false;
        }
        JSONArray array = JSONArray.parseArray(livingFacilities);
        if (array == null || array.isEmpty()) {
            return false;
        }
        Set<String> selectedFacilities = new HashSet<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            if (object == null) {
                continue;
            }
            if (Boolean.TRUE.equals(object.getBoolean("selected"))) {
                selectedFacilities.add(object.getString("label"));
            }
        }
        return selectedFacilities.containsAll(facilities);
    }

    private String buildHouseSearchAnswer(AiHouseSearchFilterVO filters, List<HouseListItemVO> houseList) {
        StringBuilder builder = new StringBuilder("我已经按你的描述做了一次智能筛选");
        List<String> parts = new ArrayList<>();
        if (filters.getCity() != null) {
            parts.add(filters.getCity());
        }
        if (filters.getDistrict() != null) {
            parts.add(filters.getDistrict());
        }
        if (filters.getMaxRent() != null) {
            parts.add("预算不超过 " + filters.getMaxRent().stripTrailingZeros().toPlainString() + " 元");
        }
        if (filters.getSizedName() != null) {
            parts.add(filters.getSizedName());
        }
        if (filters.getFacilities() != null && !filters.getFacilities().isEmpty()) {
            parts.add("偏好设施：" + String.join("、", filters.getFacilities()));
        }
        if (!parts.isEmpty()) {
            builder.append("，条件包括：").append(String.join("，", parts));
        }
        builder.append("。");

        if (houseList == null || houseList.isEmpty()) {
            builder.append(" 当前没有筛到完全匹配的房源，你可以适当放宽预算、区域或设施条件，我再帮你继续找。");
        } else {
            builder.append(" 目前为你找到 ").append(houseList.size()).append(" 套更匹配的房源，已经按结果展示在下方。");
        }
        return builder.toString();
    }

    private String buildHouseSearchAiAnswer(String message,
                                            AiHouseSearchFilterVO filters,
                                            List<HouseListItemVO> houseList,
                                            String localAnswer) {
        if (!dashScopeAiClient.isConfigured()) {
            return null;
        }
        String systemPrompt = "你是海螺租房平台的智能找房助手。"
                + "你只能基于提供的筛选条件和候选房源回答，不要编造不存在的房源、价格和条件。"
                + "请用中文输出，控制在120字以内，语气自然，先概括匹配情况，再给一条下一步建议。";
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("用户原始需求：").append(message).append("\n")
                .append("系统已解析筛选条件：").append(JSONObject.toJSONString(filters)).append("\n")
                .append("系统规则版回答：").append(localAnswer).append("\n")
                .append("候选房源：").append(buildHouseCandidatesSummary(houseList)).append("\n")
                .append("请在不改变事实的前提下润色这段回答。");
        return safeChat(systemPrompt, userPrompt.toString());
    }

    private String rewriteBusinessAnswer(String question,
                                         UserBusinessContext context,
                                         String localAnswer,
                                         AiAssistantResponseVO responseVO) {
        if (!dashScopeAiClient.isConfigured()) {
            responseVO.setProvider("本地规则兜底");
            return localAnswer;
        }
        String systemPrompt = "你是海螺租房平台的AI助手。"
                + "你只能根据提供的系统规则答案和用户当前状态摘要回答，不要编造任何新流程、金额或状态。"
                + "如果状态不足以支持额外结论，就保持保守。请用中文输出，控制在160字以内。";
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("用户问题：").append(question).append("\n")
                .append("当前用户状态摘要：").append(context == null ? "游客或未识别登录状态" : context.toPromptSummary()).append("\n")
                .append("系统规则答案：").append(localAnswer).append("\n")
                .append("请在不改变事实的前提下，把系统规则答案改写得更自然、更像真人客服。");
        String aiAnswer = safeChat(systemPrompt, userPrompt.toString());
        responseVO.setProvider(StringUtils.hasText(aiAnswer) ? "千问" : "本地规则兜底");
        return StringUtils.hasText(aiAnswer) ? aiAnswer : localAnswer;
    }

    private String buildGeneralQaAnswer(String question) {
        if (!dashScopeAiClient.isConfigured()) {
            return null;
        }
        String systemPrompt = "你是海螺租房平台内置的通用AI助手。"
                + "当前场景是通用问答，可以回答天气、常识、写作、闲聊、解释概念等普通问题。"
                + "不要把用户问题强行理解成找房、合同、账单、退租或报修问题。"
                + "如果用户明显在问租房平台规则，可以简短提示他切换到“业务问答”或“智能找房”场景。"
                + "请始终用中文回答，语气自然，控制在180字以内。";
        String userPrompt = "用户问题：" + question;
        return safeChat(systemPrompt, userPrompt);
    }

    private boolean shouldRedirectToHouseSearch(String text) {
        return !shouldRedirectToBusinessQa(text) && containsAny(text,
                "找房", "房源", "租房", "租个房", "想租", "预算", "一室", "两室", "三室",
                "整租", "合租", "近地铁", "地铁房", "带阳台", "带空调", "小区", "朝向", "精装");
    }

    private boolean shouldRedirectToBusinessQa(String text) {
        return containsAny(text,
                "合同", "押金", "账单", "退租", "解约", "报修", "水电", "自行缴费", "房东结算",
                "支付", "预约看房", "租客", "房东", "管理员审核", "我的报修", "我的账单");
    }

    private boolean shouldRedirectToGeneralQa(String text) {
        return !shouldRedirectToBusinessQa(text) && !shouldRedirectToHouseSearch(text) && containsAny(text,
                "天气", "气温", "下雨", "新闻", "翻译", "作文", "写一段", "写一篇", "总结一下",
                "什么意思", "是什么", "为什么", "怎么理解", "历史", "数学", "电影", "周末", "旅游", "闲聊");
    }

    private String safeChat(String systemPrompt, String userPrompt) {
        try {
            String content = dashScopeAiClient.chat(systemPrompt, userPrompt);
            return StringUtils.hasText(content) ? content.trim() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String buildHouseCandidatesSummary(List<HouseListItemVO> houseList) {
        if (houseList == null || houseList.isEmpty()) {
            return "无匹配房源";
        }
        return houseList.stream()
                .limit(DEFAULT_RESULT_LIMIT)
                .map(item -> item.getName() + "（" + item.getRent() + "元/月，"
                        + item.getSizeNumber() + "㎡，"
                        + (item.getCityAreaName() == null ? "-" : item.getCityAreaName()) + "）")
                .collect(Collectors.joining("；"));
    }

    private List<Area> buildAreaChain(Area target, Map<Integer, Area> areaMap) {
        List<Area> chain = new ArrayList<>();
        Area current = target;
        Set<Integer> visited = new HashSet<>();
        while (current != null && !visited.contains(current.getId())) {
            chain.add(current);
            visited.add(current.getId());
            current = current.getParentId() == null ? null : areaMap.get(current.getParentId());
        }
        Collections.reverse(chain);
        return chain;
    }

    private int getAreaDepth(Area target, Map<Integer, Area> areaMap) {
        int depth = 0;
        Area current = target;
        Set<Integer> visited = new HashSet<>();
        while (current != null && !visited.contains(current.getId())) {
            depth++;
            visited.add(current.getId());
            current = current.getParentId() == null ? null : areaMap.get(current.getParentId());
        }
        return depth;
    }

    private BigDecimal toMoney(String number, String unit) {
        BigDecimal value = new BigDecimal(number);
        if ("万".equalsIgnoreCase(unit) || "w".equalsIgnoreCase(unit)) {
            return value.multiply(new BigDecimal("10000"));
        }
        return value;
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private UserBusinessContext buildUserBusinessContext() {
        Integer userId = LocalThreadHolder.getUserId();
        if (userId == null) {
            return null;
        }
        UserBusinessContext context = new UserBusinessContext();
        context.setUserId(userId);

        RentalContractQueryDto contractQueryDto = new RentalContractQueryDto();
        contractQueryDto.setTenantUserId(userId);
        contractQueryDto.setTenantVisible(true);
        contractQueryDto.setCurrent(0);
        contractQueryDto.setSize(8);
        List<RentalContractVO> contracts = rentalContractMapper.list(contractQueryDto);
        context.setContracts(contracts == null ? Collections.emptyList() : contracts);

        RentalBillQueryDto billQueryDto = new RentalBillQueryDto();
        billQueryDto.setTenantUserId(userId);
        billQueryDto.setCurrent(0);
        billQueryDto.setSize(8);
        List<RentalBillVO> bills = rentalBillMapper.list(billQueryDto);
        context.setBills(bills == null ? Collections.emptyList() : bills);

        RepairOrderQueryDto repairOrderQueryDto = new RepairOrderQueryDto();
        repairOrderQueryDto.setTenantUserId(userId);
        repairOrderQueryDto.setCurrent(0);
        repairOrderQueryDto.setSize(8);
        List<RepairOrderVO> repairOrders = repairOrderMapper.list(repairOrderQueryDto);
        context.setRepairOrders(repairOrders == null ? Collections.emptyList() : repairOrders);
        return context;
    }

    private List<String> buildBusinessQaSuggestions(UserBusinessContext context) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("我现在下一步做什么？");
        suggestions.add("合同确认后下一步做什么？");
        suggestions.add("退租流程怎么走？");
        suggestions.add("报修费用什么时候支付？");
        if (context != null && !context.getPendingBills().isEmpty()) {
            suggestions.set(1, "我现在有待支付账单吗？");
        }
        return suggestions;
    }

    private String buildPendingActionAnswer(UserBusinessContext context) {
        if (context == null) {
            return "如果你登录后再来问我“我现在下一步做什么”，我可以结合你当前账号下的合同、待支付账单和报修工单，直接告诉你最优先要处理的事项。";
        }
        List<String> actions = context.buildActionItems();
        if (actions.isEmpty()) {
            return "结合你当前账号的合同、账单和报修数据来看，暂时没有明显待处理事项。你现在可以继续看房、等待房东处理报修，或者直接问我某一类流程，比如“退租怎么走”或“水电费怎么交”。";
        }
        StringBuilder builder = new StringBuilder("结合你当前账号的真实状态，我建议你按这个顺序处理：");
        for (int i = 0; i < actions.size(); i++) {
            builder.append("\n").append(i + 1).append(". ").append(actions.get(i));
        }
        return builder.toString();
    }

    private String buildContractAnswer(UserBusinessContext context) {
        String base = "当前合同流程是：房东发起合同 -> 管理员审核 -> 租客确认 -> 待支付押金 -> 押金支付成功 -> 合同生效。押金方式使用固定选项，例如押一付一、押一付三、押二付一、押二付三，押金金额由系统按月租自动计算。";
        if (context == null) {
            return base;
        }
        RentalContractVO actionableContract = context.getPrimaryActionContract();
        if (actionableContract == null) {
            return base + " 你当前账号下最近没有待确认或待支付押金的合同。";
        }
        if (Objects.equals(actionableContract.getStatus(), RentalContractStatusEnum.STATUS_2.getType())) {
            return "就你当前账号来看，最近一份合同 " + actionableContract.getContractNo() + " 正处于“待租客确认”。你先去“我的合同”里确认合同，确认后系统会进入押金支付。";
        }
        if (Objects.equals(actionableContract.getStatus(), RentalContractStatusEnum.STATUS_3.getType())) {
            return "就你当前账号来看，最近一份合同 " + actionableContract.getContractNo() + " 正处于“待支付押金”。你先完成押金支付，支付成功后合同才会正式生效，并自动生成首期租金账单。";
        }
        if (Objects.equals(actionableContract.getStatus(), RentalContractStatusEnum.STATUS_4.getType())) {
            return base + " 你当前最近一份合同 " + actionableContract.getContractNo() + " 已经生效，可以继续关注租赁账单、报修或退租流程。";
        }
        return base + " 你当前最近一份合同 " + actionableContract.getContractNo() + " 的状态是“"
                + RentalContractStatusEnum.getDetail(actionableContract.getStatus()) + "”。";
    }

    private String buildUtilityAnswer(UserBusinessContext context) {
        String base = "水电费目前支持两种模式：自行缴费、房东结算。自行缴费时，租户自己完成缴费并上传凭证；房东结算时，水费和电费会并入租赁账单，由租户统一支付。";
        if (context == null || context.getLatestContract() == null) {
            return base;
        }
        RentalContractVO latestContract = context.getLatestContract();
        return base + " 结合你当前最近一份合同 " + latestContract.getContractNo() + "，系统记录的水电支付方式是“"
                + UtilityPaymentModeEnum.getDetail(latestContract.getUtilityPaymentMode()) + "”。";
    }

    private String buildBillAnswer(UserBusinessContext context) {
        String base = "合同生效后会生成押金单和首期租金单，后续账单可由房东按周期发起。租户可在“我的账单”中完成支付宝支付并刷新支付状态；报修费用则在“我的报修”里直接支付，不和租金账单混在一起。";
        if (context == null) {
            return base;
        }
        List<RentalBillVO> pendingBills = context.getPendingBills();
        if (pendingBills.isEmpty()) {
            return base + " 你当前账号下暂时没有待支付的租赁账单。";
        }
        RentalBillVO latestPendingBill = pendingBills.get(0);
        return "你当前有 " + pendingBills.size() + " 笔待支付租赁账单。最近一笔是“"
                + RentalBillTypeEnum.getDetail(latestPendingBill.getBillType()) + "”，账单号 "
                + latestPendingBill.getBillNo() + "，应付金额 ¥"
                + latestPendingBill.getTotalAmount().stripTrailingZeros().toPlainString()
                + "。你可以直接去“我的账单”完成支付。" + " " + base;
    }

    private String buildRepairAnswer(UserBusinessContext context) {
        String base = "报修第一版已经支持完整闭环：租户发起报修 -> 房东受理/处理中 -> 房东提交处理结果。若维修费用大于 0，工单会进入待支付，租户在“我的报修”里直接支付；支付完成后，租户再确认完成，工单才会结束。";
        if (context == null) {
            return base;
        }
        RepairOrderVO repairOrder = context.getPrimaryRepairOrder();
        if (repairOrder == null) {
            return base + " 你当前账号下暂时没有需要跟进的报修工单。";
        }
        if (Objects.equals(repairOrder.getStatus(), RepairOrderStatusEnum.STATUS_4.getType())
                && Objects.equals(repairOrder.getPaymentStatus(), RepairPaymentStatusEnum.STATUS_1.getType())) {
            return "你当前有 1 个报修工单待支付维修费，工单号 " + repairOrder.getRepairNo() + "，金额 ¥"
                    + repairOrder.getRepairAmount().stripTrailingZeros().toPlainString()
                    + "。先在“我的报修”里完成支付，支付成功后再确认工单完成。";
        }
        if (Objects.equals(repairOrder.getStatus(), RepairOrderStatusEnum.STATUS_4.getType())
                && Objects.equals(repairOrder.getPaymentStatus(), RepairPaymentStatusEnum.STATUS_2.getType())) {
            return "你当前有 1 个报修工单待租户确认完成，工单号 " + repairOrder.getRepairNo()
                    + "。维修费已支付，确认现场处理无误后就可以完成这笔报修。";
        }
        return base + " 你当前最近一笔报修工单 " + repairOrder.getRepairNo()
                + " 的状态是“" + RepairOrderStatusEnum.getDetail(repairOrder.getStatus()) + "”。";
    }

    private String buildTerminateAnswer(UserBusinessContext context) {
        String base = "未到期退租当前走这条链路：已生效 -> 退租申请中 -> 待退租审核 -> 待退押 -> 待审核退押 -> 已退租。管理员驳回后，房东可以根据审核意见重新提交；管理员通过退租后，房东还需要完成退押并上传凭证，审核通过后合同才真正结束。";
        if (context == null) {
            return base;
        }
        RentalContractVO terminateContract = context.getPrimaryTerminationContract();
        if (terminateContract == null) {
            return base + " 你当前账号下暂时没有正在退租中的合同。";
        }
        return "你当前最近一份退租中的合同是 " + terminateContract.getContractNo()
                + "，状态为“" + RentalContractStatusEnum.getDetail(terminateContract.getStatus()) + "”。"
                + buildTerminateStatusHint(terminateContract.getStatus()) + " " + base;
    }

    private String buildGenericQaAnswer(UserBusinessContext context) {
        if (context != null) {
            List<String> actions = context.buildActionItems();
            if (!actions.isEmpty()) {
                return "我已经能结合你当前账号的真实业务状态来回答了。你现在最优先的一项待办是：“"
                        + actions.get(0) + "”。如果你愿意，也可以继续直接问我“我现在下一步做什么”“我有没有待支付账单”或者“我的报修到哪一步了”。";
            }
        }
        return "我现在更擅长回答租房平台内的具体流程问题，比如智能找房、合同确认、押金支付、租赁账单、退租流程和报修处理。登录后再来问我“我现在下一步做什么”，我还能结合你当前账号的合同、账单和报修状态直接回答。";
    }

    private String buildTerminateStatusHint(Integer status) {
        if (Objects.equals(status, RentalContractStatusEnum.STATUS_9.getType())) {
            return "下一步需要房东提交协商后的退押金额和退租凭证。";
        }
        if (Objects.equals(status, RentalContractStatusEnum.STATUS_10.getType())) {
            return "当前在等管理员审核退租申请。";
        }
        if (Objects.equals(status, RentalContractStatusEnum.STATUS_11.getType())) {
            return "退租审核已经通过，下一步需要房东完成退押。";
        }
        if (Objects.equals(status, RentalContractStatusEnum.STATUS_12.getType())) {
            return "房东已经提交退押凭证，当前在等管理员审核退押。";
        }
        return "";
    }

    private String normalizeAreaName(String name) {
        if (name == null) {
            return "";
        }
        return name.replace("特别行政区", "")
                .replace("自治区", "")
                .replace("自治州", "")
                .replace("省", "")
                .replace("市", "")
                .replace("区", "")
                .replace("县", "")
                .trim();
    }

    private static class UserBusinessContext {
        private Integer userId;
        private List<RentalContractVO> contracts = Collections.emptyList();
        private List<RentalBillVO> bills = Collections.emptyList();
        private List<RepairOrderVO> repairOrders = Collections.emptyList();

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public void setContracts(List<RentalContractVO> contracts) {
            this.contracts = contracts;
        }

        public void setBills(List<RentalBillVO> bills) {
            this.bills = bills;
        }

        public void setRepairOrders(List<RepairOrderVO> repairOrders) {
            this.repairOrders = repairOrders;
        }

        public RentalContractVO getLatestContract() {
            return contracts.isEmpty() ? null : contracts.get(0);
        }

        public List<RentalBillVO> getPendingBills() {
            return bills.stream()
                    .filter(item -> Objects.equals(item.getPayStatus(), RentalBillPayStatusEnum.STATUS_1.getType()))
                    .collect(Collectors.toList());
        }

        public RentalContractVO getPrimaryActionContract() {
            return contracts.stream()
                    .filter(item -> Objects.equals(item.getStatus(), RentalContractStatusEnum.STATUS_2.getType())
                            || Objects.equals(item.getStatus(), RentalContractStatusEnum.STATUS_3.getType())
                            || Objects.equals(item.getStatus(), RentalContractStatusEnum.STATUS_4.getType()))
                    .findFirst()
                    .orElse(getLatestContract());
        }

        public RentalContractVO getPrimaryTerminationContract() {
            return contracts.stream()
                    .filter(item -> Objects.equals(item.getStatus(), RentalContractStatusEnum.STATUS_9.getType())
                            || Objects.equals(item.getStatus(), RentalContractStatusEnum.STATUS_10.getType())
                            || Objects.equals(item.getStatus(), RentalContractStatusEnum.STATUS_11.getType())
                            || Objects.equals(item.getStatus(), RentalContractStatusEnum.STATUS_12.getType()))
                    .findFirst()
                    .orElse(null);
        }

        public RepairOrderVO getPrimaryRepairOrder() {
            return repairOrders.stream()
                    .filter(item -> !Objects.equals(item.getStatus(), RepairOrderStatusEnum.STATUS_5.getType())
                            && !Objects.equals(item.getStatus(), RepairOrderStatusEnum.STATUS_6.getType())
                            && !Objects.equals(item.getStatus(), RepairOrderStatusEnum.STATUS_7.getType()))
                    .findFirst()
                    .orElse(repairOrders.isEmpty() ? null : repairOrders.get(0));
        }

        public List<String> buildActionItems() {
            List<String> actions = new ArrayList<>();
            RentalContractVO contract = getPrimaryActionContract();
            if (contract != null && Objects.equals(contract.getStatus(), RentalContractStatusEnum.STATUS_2.getType())) {
                actions.add("合同 " + contract.getContractNo() + " 还在待租客确认，先去“我的合同”确认合同。");
            }
            if (contract != null && Objects.equals(contract.getStatus(), RentalContractStatusEnum.STATUS_3.getType())) {
                actions.add("合同 " + contract.getContractNo() + " 还在待支付押金，先完成押金支付让合同生效。");
            }
            List<RentalBillVO> pendingBills = getPendingBills();
            if (!pendingBills.isEmpty()) {
                RentalBillVO bill = pendingBills.get(0);
                actions.add("你有 " + pendingBills.size() + " 笔待支付账单，最近一笔是 " + bill.getBillNo()
                        + "，金额 ¥" + bill.getTotalAmount().stripTrailingZeros().toPlainString() + "。");
            }
            RepairOrderVO repairOrder = getPrimaryRepairOrder();
            if (repairOrder != null) {
                if (Objects.equals(repairOrder.getStatus(), RepairOrderStatusEnum.STATUS_4.getType())
                        && Objects.equals(repairOrder.getPaymentStatus(), RepairPaymentStatusEnum.STATUS_1.getType())) {
                    actions.add("报修工单 " + repairOrder.getRepairNo() + " 待支付维修费，先去“我的报修”完成支付。");
                } else if (Objects.equals(repairOrder.getStatus(), RepairOrderStatusEnum.STATUS_4.getType())
                        && Objects.equals(repairOrder.getPaymentStatus(), RepairPaymentStatusEnum.STATUS_2.getType())) {
                    actions.add("报修工单 " + repairOrder.getRepairNo() + " 已处理完，等待你确认完成。");
                } else if (Objects.equals(repairOrder.getStatus(), RepairOrderStatusEnum.STATUS_1.getType())
                        || Objects.equals(repairOrder.getStatus(), RepairOrderStatusEnum.STATUS_2.getType())
                        || Objects.equals(repairOrder.getStatus(), RepairOrderStatusEnum.STATUS_3.getType())) {
                    actions.add("报修工单 " + repairOrder.getRepairNo() + " 当前状态为“"
                            + RepairOrderStatusEnum.getDetail(repairOrder.getStatus()) + "”，可以继续跟进处理进度。");
                }
            }
            RentalContractVO terminationContract = getPrimaryTerminationContract();
            if (terminationContract != null) {
                actions.add("合同 " + terminationContract.getContractNo() + " 正在退租流程中，当前状态是“"
                        + RentalContractStatusEnum.getDetail(terminationContract.getStatus()) + "”。");
            }
            return actions;
        }

        public String toPromptSummary() {
            StringBuilder summary = new StringBuilder();
            if (!contracts.isEmpty()) {
                RentalContractVO latestContract = contracts.get(0);
                summary.append("最近合同：")
                        .append(latestContract.getContractNo())
                        .append("，状态=")
                        .append(RentalContractStatusEnum.getDetail(latestContract.getStatus()))
                        .append("；");
            } else {
                summary.append("当前没有合同；");
            }
            List<RentalBillVO> pendingBills = getPendingBills();
            summary.append("待支付租赁账单数=").append(pendingBills.size()).append("；");
            RepairOrderVO repairOrder = getPrimaryRepairOrder();
            if (repairOrder != null) {
                summary.append("最近报修=")
                        .append(repairOrder.getRepairNo())
                        .append("，状态=")
                        .append(RepairOrderStatusEnum.getDetail(repairOrder.getStatus()))
                        .append("，支付状态=")
                        .append(RepairPaymentStatusEnum.getDetail(repairOrder.getPaymentStatus()))
                        .append("；");
            } else {
                summary.append("当前没有活动报修；");
            }
            return summary.toString();
        }
    }
}
