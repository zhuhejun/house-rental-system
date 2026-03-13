package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.FlowIndexMapper;
import com.kmbeast.mapper.HouseMapper;
import com.kmbeast.mapper.LandlordMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.FlowIndexQueryDto;
import com.kmbeast.pojo.dto.HouseQueryDto;
import com.kmbeast.pojo.dto.LandlordQueryDto;
import com.kmbeast.pojo.dto.QueryDto;
import com.kmbeast.pojo.em.*;
import com.kmbeast.pojo.entity.FlowIndex;
import com.kmbeast.pojo.entity.House;
import com.kmbeast.pojo.vo.*;
import com.kmbeast.service.FlowIndexService;
import com.kmbeast.service.HouseService;
import com.kmbeast.utils.AssertUtils;
import com.kmbeast.utils.DateUtil;
import com.kmbeast.utils.UserBasedCFUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 房屋业务逻辑接口实现类
 */
@Service
public class HouseServiceImpl extends ServiceImpl<HouseMapper, House> implements HouseService {

    @Resource
    private LandlordMapper landlordMapper;
    @Resource
    private FlowIndexService flowIndexService;
    @Resource
    private FlowIndexMapper flowIndexMapper;

    /**
     * 房屋列表查询
     *
     * @param houseQueryDto 查询条件类
     * @return Result<List < HouseListItemVO>>
     */
    @Override
    public Result<List<HouseListItemVO>> list(HouseQueryDto houseQueryDto) {
        List<HouseListItemVO> houseListItemVOS = this.baseMapper.list(houseQueryDto);
        dealHouseStatus(houseListItemVOS);
        Integer count = this.baseMapper.listCount(houseQueryDto);
        return ApiResult.success(houseListItemVOS, count);
    }

    private void dealHouseStatus(List<HouseListItemVO> houseListItemVOS) {
        for (HouseListItemVO houseListItemVO : houseListItemVOS) {
            // 通过朝向ID，设置朝向文本字样
            if (Objects.nonNull(houseListItemVO.getDirectionId())) {
                String detail = HouseDirectionEnum.getDetail(houseListItemVO.getDirectionId());
                houseListItemVO.setDirectionName(detail);
            }
            // 通过押金方式ID，设置押金方式文本字样
            if (Objects.nonNull(houseListItemVO.getDepositMethodId())) {
                String detail = HouseDepositEnum.getDetail(houseListItemVO.getDepositMethodId());
                houseListItemVO.setDepositMethodName(detail);
            }
            // 通过装修状态ID，设置装修状态文本字样
            if (Objects.nonNull(houseListItemVO.getFitmentStatusId())) {
                String detail = HouseFitmentEnum.getDetail(houseListItemVO.getFitmentStatusId());
                houseListItemVO.setFitmentStatusName(detail);
            }
        }
    }

    private void dealHouseVOStatus(List<HouseVO> houseVOS) {
        for (HouseVO houseVO : houseVOS) {
            // 通过朝向ID，设置朝向文本字样
            if (Objects.nonNull(houseVO.getDirectionId())) {
                String detail = HouseDirectionEnum.getDetail(houseVO.getDirectionId());
                houseVO.setDirectionName(detail);
            }
            // 通过押金方式ID，设置押金方式文本字样
            if (Objects.nonNull(houseVO.getDepositMethodId())) {
                String detail = HouseDepositEnum.getDetail(houseVO.getDepositMethodId());
                houseVO.setDepositMethodName(detail);
            }
            // 通过装修状态ID，设置装修状态文本字样
            if (Objects.nonNull(houseVO.getFitmentStatusId())) {
                String detail = HouseFitmentEnum.getDetail(houseVO.getFitmentStatusId());
                houseVO.setFitmentStatusName(detail);
            }
            // 通过房屋类型ID，设置房屋类型文本字样
            if (Objects.nonNull(houseVO.getTypeId())) {
                String detail = HouseTypeEnum.getDetail(houseVO.getTypeId());
                houseVO.setTypeName(detail);
            }
            // 通过户型类型ID，设置户型文本字样
            if (Objects.nonNull(houseVO.getSizedId())) {
                String detail = HouseSizedEnum.getDetail(houseVO.getTypeId());
                houseVO.setSizedName(detail);
            }
            // 设置租赁方式
            if (Objects.nonNull(houseVO.getRentalType())) {
                String detail = RentalTypeEnum.getDetail(houseVO.getRentalType());
                houseVO.setRentalTypeName(detail);
            }
        }
    }

    /**
     * 房屋状态修改
     *
     * @param house 房屋信息
     * @return Result<String>
     */
    @Override
    public Result<String> update(House house) {
        paramJudge(house);
        updateById(house);
        return ApiResult.success("房屋信息修改成功");
    }

    /**
     * 参数校验
     *
     * @param house 房屋实体信息
     */
    private void paramJudge(House house) {
        AssertUtils.hasText(house.getName(), "房屋名称不能为空");
        AssertUtils.hasText(house.getCover(), "房屋封面不能为空");
        AssertUtils.hasText(house.getFloor(), "请填写楼层");
        AssertUtils.hasText(house.getCovers(), "房屋实况图不能为空");
        AssertUtils.notNull(house.getTypeId(), "请设置房屋类型");
        AssertUtils.notNull(house.getSizeNumber(), "请填写房屋产权面积");
        AssertUtils.notNull(house.getDirectionId(), "请设置房屋朝向");
        AssertUtils.notNull(house.getSizedId(), "请设置户型");
        AssertUtils.notNull(house.getRent(), "请设置租金");
        AssertUtils.notNull(house.getDepositMethodId(), "请设置押金方式");
        AssertUtils.notNull(house.getIsSubway(), "请设置是否临近地铁");
        if (house.getIsSubway()) { // 一旦你设置临近地铁，就要补充线路
            AssertUtils.notNull(house.getSubwayLine(), "请设置地铁线路");
        }
        AssertUtils.notNull(house.getFitmentStatusId(), "请设置装修类型");
        AssertUtils.notNull(house.getRentalType(), "请设置租赁方式");
        // 名称长度显示
        AssertUtils.isTrue(house.getName().length() < 30, "房屋名称最多30个字"); // 名称长度校验
        // 实况图上传数量显示
        String covers = house.getCovers();
        long count = covers.chars().filter(c -> c == ',').count();
        AssertUtils.isTrue(count < 6, "实况图最多上传6张");
        // 楼层只能设置为高中低
        AssertUtils.isTrue(house.getFloor().length() <= 1, "楼层只能补充为高或者低、中");
    }

    /**
     * 房屋状态新增
     *
     * @param house 房屋信息
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(House house) {
        paramJudge(house);
        // 设置上当前新增的房东ID
        LandlordVO landlordVO = getLandlord();
        AssertUtils.isTrue(landlordVO.getIsAudit(), "房东认证信息待审核中，请稍后再试");
        house.setLandlordId(landlordVO.getId()); // 认证通过，设置上查出来的房东ID
        house.setCreateTime(LocalDateTime.now()); // 设置上当前的操作时间
        house.setStatus(HouseStatusEnum.STATUS_1.getType()); // 刚开始新增的房屋信息就是待租状态
        save(house);
        return ApiResult.success("房屋新增成功");
    }

    /**
     * 取得房东ID
     *
     * @return Integer
     */
    private LandlordVO getLandlord() {
        LandlordQueryDto landlordQueryDto = new LandlordQueryDto();
        landlordQueryDto.setUserId(LocalThreadHolder.getUserId()); // 设置上当前新增房屋信息的房东信息ID
        List<LandlordVO> landlordVOS = landlordMapper.list(landlordQueryDto);
        AssertUtils.isFalse(landlordVOS.isEmpty(), "房东信息异常，非法操作");
        return landlordVOS.get(0); // 用户自己申请的房东信息
    }

    /**
     * 查询房屋类型列表
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @Override
    public Result<List<SelectedVO>> houseTypeList() {
        List<SelectedVO> selectedVOS = Arrays.stream(HouseTypeEnum.values())
                .map(houseTypeEnum -> new SelectedVO(houseTypeEnum.getType(), houseTypeEnum.getDetail()))
                .collect(Collectors.toList());
        return ApiResult.success(selectedVOS);
    }

    /**
     * 查询房屋朝向列表
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @Override
    public Result<List<SelectedVO>> houseDirectionList() {
        List<SelectedVO> selectedVOS = Arrays.stream(HouseDirectionEnum.values())
                .map(houseDirectionEnum -> new SelectedVO(houseDirectionEnum.getType(), houseDirectionEnum.getDetail()))
                .collect(Collectors.toList());
        return ApiResult.success(selectedVOS);
    }

    /**
     * 查询房屋户型列表
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @Override
    public Result<List<SelectedVO>> houseSizedList() {
        List<SelectedVO> selectedVOS = Arrays.stream(HouseSizedEnum.values())
                .map(houseSizedEnum -> new SelectedVO(houseSizedEnum.getType(), houseSizedEnum.getDetail()))
                .collect(Collectors.toList());
        return ApiResult.success(selectedVOS);
    }

    /**
     * 查询房屋押金方式列表
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @Override
    public Result<List<SelectedVO>> houseDepositMethodList() {
        List<SelectedVO> selectedVOS = Arrays.stream(HouseDepositEnum.values())
                .map(houseDepositEnum -> new SelectedVO(houseDepositEnum.getType(), houseDepositEnum.getDetail()))
                .collect(Collectors.toList());
        return ApiResult.success(selectedVOS);
    }

    /**
     * 查询房屋是否临近地铁列表
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @Override
    public Result<List<SelectedVO>> houseSubwayList() {
        List<SelectedVO> selectedVOS = Arrays.stream(IsSubwayEnum.values())
                .map(isSubwayEnum -> new SelectedVO(isSubwayEnum.getType(), isSubwayEnum.getDetail()))
                .collect(Collectors.toList());
        return ApiResult.success(selectedVOS);
    }

    /**
     * 查询房屋装修状态
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @Override
    public Result<List<SelectedVO>> houseFitmentStatusList() {
        List<SelectedVO> selectedVOS = Arrays.stream(HouseFitmentEnum.values())
                .map(houseFitmentEnum -> new SelectedVO(houseFitmentEnum.getType(), houseFitmentEnum.getDetail()))
                .collect(Collectors.toList());
        return ApiResult.success(selectedVOS);
    }

    /**
     * 查询房屋租赁方式
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @Override
    public Result<List<SelectedVO>> houseRentalTypeList() {
        List<SelectedVO> selectedVOS = Arrays.stream(RentalTypeEnum.values())
                .map(rentalTypeEnum -> new SelectedVO(rentalTypeEnum.getType(), rentalTypeEnum.getDetail()))
                .collect(Collectors.toList());
        return ApiResult.success(selectedVOS);
    }

    /**
     * 查询房屋生活设施配置信息项
     *
     * @return Result<List < LivingFacilityVO>> 响应结果
     */
    @Override
    public Result<List<LivingFacilityVO>> houseLivingFacilityList() {
        List<LivingFacilityVO> livingFacilityVOS = Arrays.stream(LivingFacilitiesEnum.values())
                .map(livingFacilitiesEnum -> new LivingFacilityVO(livingFacilitiesEnum.getType(), livingFacilitiesEnum.getSelected()))
                .collect(Collectors.toList());
        return ApiResult.success(livingFacilityVOS);
    }

    /**
     * 查询房东自己的房屋信息
     *
     * @param houseQueryDto 查询参数
     * @return Result<List < HouseListItemVO>> 响应结果
     */
    @Override
    public Result<List<HouseListItemVO>> landlordHouseList(HouseQueryDto houseQueryDto) {
        // 通过当前用户ID查询房东ID
        LandlordVO landlordVO = getLandlord();
        houseQueryDto.setLandlordId(landlordVO.getId());
        List<HouseListItemVO> houseListItemVOS = this.baseMapper.list(houseQueryDto);
        dealHouseStatus(houseListItemVOS);
        Integer count = this.baseMapper.listCount(houseQueryDto);
        return ApiResult.success(houseListItemVOS, count);
    }

    /**
     * 房东上架或下架房源操作
     *
     * @param id 房源ID
     * @return Result<String> 响应结果
     */
    @Override
    public Result<String> houseStatusDeal(Integer id) {
        AssertUtils.notNull(id, "房源ID不能为空");
        // 先通过ID查询房源信息
        House house = getById(id);
        AssertUtils.notNull(house, "房源信息异常");
        AssertUtils.notNull(house.getLandlordId(), "房源信息异常");
        AssertUtils.isFalse(Objects.equals(house.getStatus(), HouseStatusEnum.STATUS_3.getType()), "已出租房源不能上下架");
        boolean equals = Objects.equals(house.getStatus(), HouseStatusEnum.STATUS_1.getType());
        house.setStatus(equals
                ? HouseStatusEnum.STATUS_2.getType()
                : HouseStatusEnum.STATUS_1.getType());
        // 如果是管理员审核，不需要进行房东的校验
        if (Objects.equals(LocalThreadHolder.getRoleId(), RoleEnum.ADMIN.getRole())) {
            updateById(house);
            return ApiResult.success(equals ? "房屋已下架" : "房屋已上架");
        }
        // 如果是房东自己，执行下列的校验
        Integer landlordId = house.getLandlordId(); // 取得房东ID
        LandlordVO landlord = getLandlord(); // 当前操作者认证的房东信息
        AssertUtils.notNull(landlord, "房东信息异常");
        AssertUtils.isTrue(landlord.getIsAudit(), "房东信息未审核");
        AssertUtils.equals(landlordId, landlord.getId(), "非法操作");
        updateById(house);
        return ApiResult.success(equals ? "房屋已下架" : "房屋已上架");
    }

    /**
     * 通过ID查询房屋详情信息
     *
     * @param id 房源ID
     * @return Result<HouseVO> 响应结果
     */
    @Override
    public Result<HouseVO> selectById(Integer id) {
        HouseVO houseVO = this.baseMapper.getById(id);
        List<HouseVO> houseVOList = new ArrayList<>();
        houseVOList.add(houseVO);
        dealHouseVOStatus(houseVOList);
        return ApiResult.success(houseVO);
    }

    /**
     * 查询房屋面积查询条件范围
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @Override
    public Result<List<SelectedVO>> houseSizeNumber() {
        List<SelectedVO> selectedVOS = Arrays.stream(HouseSizeNumberEnum.values())
                .map(houseSizeNumberEnum -> new SelectedVO(houseSizeNumberEnum.getType(), houseSizeNumberEnum.getDetail()))
                .collect(Collectors.toList());
        return ApiResult.success(selectedVOS);
    }

    /**
     * 查询房屋租金查询条件范围
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @Override
    public Result<List<SelectedVO>> houseRentRange() {
        List<SelectedVO> selectedVOS = Arrays.stream(HouseRentEnum.values())
                .map(houseRentEnum -> new SelectedVO(houseRentEnum.getType(), houseRentEnum.getDetail()))
                .collect(Collectors.toList());
        return ApiResult.success(selectedVOS);
    }

    /**
     * 用户首页查询房屋信息 - 前提是只能查询上架的房屋信息
     *
     * @param houseQueryDto 查询参数
     * @return Result<List < HouseListItemVO>> 响应结果
     */
    @Override
    public Result<List<HouseListItemVO>> listUser(HouseQueryDto houseQueryDto) {
        houseQueryDto.setStatus(HouseStatusEnum.STATUS_1.getType()); // 用户首页只能查询待租的房源信息
        List<HouseListItemVO> houseListItemVOS = this.baseMapper.list(houseQueryDto);
        dealHouseStatus(houseListItemVOS);
        dealFlowIndex(houseListItemVOS);
        Integer count = this.baseMapper.listCount(houseQueryDto);
        return ApiResult.success(houseListItemVOS, count);
    }

    /**
     * 记录展示流量
     */
    private void dealFlowIndex(List<HouseListItemVO> houseListItemVOS) {
        List<FlowIndex> flowIndexList = new ArrayList<>();
        for (HouseListItemVO houseListItemVO : houseListItemVOS) {
            FlowIndex flowIndex = new FlowIndex();
            flowIndex.setCreateTime(LocalDateTime.now());
            flowIndex.setContentType("HOUSE_INFO");
            flowIndex.setType(FlowIndexEnum.SHOW.getType());
            flowIndex.setContentId(houseListItemVO.getId());
            flowIndex.setUserId(LocalThreadHolder.getUserId() == null ? -1 : LocalThreadHolder.getUserId());
            flowIndexList.add(flowIndex);
        }
        flowIndexService.saveBatch(flowIndexList);
        flowIndexList.clear();
    }

    /**
     * 统计房屋流量
     *
     * @param houseQueryDto 查询参数
     * @return Result<List < HouseFlowIndexVO>> 响应结果
     */
    @Override
    public Result<List<HouseFlowIndexVO>> listFlowIndex(HouseQueryDto houseQueryDto) {
        // 前提是房东来查的
        // 1. 通过用户ID去查找用户自己已经认证的房东信息，取出他的房东ID
        LandlordVO landlord = getLandlord();
        AssertUtils.isTrue(landlord.getIsAudit(), "房东认证信息待审核，请稍后再试");
        // 2. 凭着查出来的房东ID，去房屋信息表查询该房东自己名下维护的所有房屋ID，取列表
        List<Integer> houseIds = this.baseMapper.selectIdsByLandlordId(landlord.getId());
        if (houseIds.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        HouseQueryDto queryDto = new HouseQueryDto();
        queryDto.setIds(houseIds);
        List<HouseFlowIndexVO> houseFlowIndexVOS = this.baseMapper.flowIndexList(queryDto);
        dealClickRate(houseFlowIndexVOS);
        Integer count = this.baseMapper.listCount(queryDto);
        return ApiResult.success(houseFlowIndexVOS, count);
    }

    /**
     * 计算并设置点击率（点击率 = 阅读量 / 展现量 * 100%）
     *
     * @param houseFlowIndexVOS 流量数据列表
     */
    private void dealClickRate(List<HouseFlowIndexVO> houseFlowIndexVOS) {
        if (houseFlowIndexVOS == null || houseFlowIndexVOS.isEmpty()) {
            return;
        }

        for (HouseFlowIndexVO vo : houseFlowIndexVOS) {
            // 确保展现量和阅读量都不为null且展现量不为0
            if (vo.getShowNumber() != null && vo.getViewNumber() != null
                    && vo.getShowNumber() != 0) {
                // 计算点击率（百分比形式，保留2位小数）
                double rate = (double) vo.getViewNumber() / vo.getShowNumber() * 100;
                vo.setClickRate(Double.parseDouble(String.format("%.2f", rate)));
            } else {
                // 如果数据不完整，点击率设为0
                vo.setClickRate(0.0);
            }
        }
    }

    /**
     * 流量指标可视化
     *
     * @param houseQueryDto 查询参数
     * @return Result<List < ChartVO>> 响应结果
     */
    @Override
    public Result<List<ChartVO>> listChart(HouseQueryDto houseQueryDto) {
        flowIndexListParamJudge(houseQueryDto);
        FlowIndexQueryDto flowIndexQueryDto = createFlowIndexQueryDto(houseQueryDto);
        List<FlowIndex> flowIndexList = flowIndexMapper.list(flowIndexQueryDto);
        if (flowIndexList == null || flowIndexList.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        // 只取出里面的创建时间，形成一个新的数组
        List<LocalDateTime> dateTimeList = flowIndexList.stream()
                .map(FlowIndex::getCreateTime)
                .collect(Collectors.toList());
        List<ChartVO> chartVOS = DateUtil.countDatesWithinRange(houseQueryDto.getDays(), dateTimeList);
        return ApiResult.success(chartVOS);
    }

    private void flowIndexListParamJudge(HouseQueryDto houseQueryDto) {
        AssertUtils.notNull(houseQueryDto, "查询条件不能为空");
        AssertUtils.notNull(houseQueryDto.getId(), "内容ID不能为空");
        AssertUtils.notNull(houseQueryDto.getType(), "流量类型不能为空");
        AssertUtils.notNull(houseQueryDto.getDays(), "查询天数不能为空");
    }

    private FlowIndexQueryDto createFlowIndexQueryDto(HouseQueryDto houseQueryDto) {
        FlowIndexQueryDto flowIndexQueryDto = new FlowIndexQueryDto();
        flowIndexQueryDto.setContentType("HOUSE_INFO");
        flowIndexQueryDto.setContentId(houseQueryDto.getId());
        flowIndexQueryDto.setType(houseQueryDto.getType());
        QueryDto queryDto = DateUtil.startAndEndTime(houseQueryDto.getDays());
        flowIndexQueryDto.setStartTime(queryDto.getStartTime());
        flowIndexQueryDto.setEndTime(queryDto.getEndTime());
        return flowIndexQueryDto;
    }

    /**
     * 房屋推荐
     *
     * @param count 推荐的条数
     * @return Result<List < HouseListItemVO>> 响应结果
     */
    @Override
    public Result<List<HouseListItemVO>> recommend(Integer count) {
        AssertUtils.notNull(count, "推荐条数不能为空");
        FlowIndexQueryDto flowIndexQueryDto = new FlowIndexQueryDto();
        flowIndexQueryDto.setContentType("HOUSE_INFO"); // 做的是房屋的推荐，所以模块设置为房屋模块
        // 获取所有的房屋ID列表
        List<Integer> houseIds = this.baseMapper.getIds();
        if (houseIds.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        // 获取用户对于房屋资讯的评分
        List<ScoreVO> scores = flowIndexMapper.getScores(flowIndexQueryDto);
        if (scores.isEmpty()) {
            List<HouseListItemVO> houseListItemVOS = queryHouseNews(houseIds.size() > count ? houseIds.subList(0, count) : houseIds);
            return ApiResult.success(houseListItemVOS);
        }
        // 构建用户对于物品评分的指定数据结构
        List<UserBasedCFUtil.Score> scoreList = scores.stream().map(score -> new UserBasedCFUtil.Score(
                score.getUserId(),
                score.getContentId(),
                score.getScore()
        )).collect(Collectors.toList());
        // 构建用户对于房屋的评分矩阵
        Map<Integer, Map<Integer, Double>> userItemMatrix =
                UserBasedCFUtil.buildUserItemMatrix(houseIds, scoreList);
        // 创建协同过滤工具实例
        UserBasedCFUtil cfUtil = new UserBasedCFUtil(userItemMatrix);
        // 算出向指定这个用户推荐的房屋资讯
        List<Integer> itemIds = cfUtil.recommendItems(LocalThreadHolder.getUserId(), count);
        // 冷启动：用户是新用户，没有一丁点儿的互动数据，何谈兴趣？何谈推荐？
        if (itemIds.isEmpty()) {
            // 如果最初查出来的房屋的ID列表，比咱们接口需要的更多，则截取，反之直接用
            List<HouseListItemVO> houseListItemVOS = queryHouseNews(houseIds.size() > count ? houseIds.subList(0, count) : houseIds);
            return ApiResult.success(houseListItemVOS);
        }
        List<HouseListItemVO> houseListItemVOS = queryHouseNews(itemIds);
        return ApiResult.success(houseListItemVOS);
    }

    /**
     * 通过房屋资讯的ID列表查询房屋资讯
     *
     * @param ids 房屋资讯ID列表
     * @return List<HouseNewsListVO>
     */
    private List<HouseListItemVO> queryHouseNews(List<Integer> ids) {
        HouseQueryDto houseQueryDto = new HouseQueryDto();
        houseQueryDto.setIds(ids);
        return this.baseMapper.list(houseQueryDto);
    }


}
