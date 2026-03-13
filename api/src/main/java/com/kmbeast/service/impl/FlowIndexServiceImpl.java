package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.FlowIndexMapper;
import com.kmbeast.mapper.HouseMapper;
import com.kmbeast.mapper.HouseNewsMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.FlowIndexQueryDto;
import com.kmbeast.pojo.dto.HouseNewsQueryDto;
import com.kmbeast.pojo.dto.HouseQueryDto;
import com.kmbeast.pojo.em.FlowIndexEnum;
import com.kmbeast.pojo.entity.FlowIndex;
import com.kmbeast.pojo.vo.HouseListItemVO;
import com.kmbeast.pojo.vo.HouseNewsListVO;
import com.kmbeast.service.FlowIndexService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 流量指标业务逻辑接口实现类
 */
@Service
public class FlowIndexServiceImpl extends ServiceImpl<FlowIndexMapper, FlowIndex> implements FlowIndexService {

    @Resource
    private HouseMapper houseMapper;
    @Resource
    private HouseNewsMapper houseNewsMapper;

    /**
     * 查询流量指标列表
     *
     * @param flowIndexQueryDto 查询条件
     * @return Result<List < FlowIndex>>
     */
    @Override
    public Result<List<FlowIndex>> list(FlowIndexQueryDto flowIndexQueryDto) {
        List<FlowIndex> flowIndexList = this.baseMapper.list(flowIndexQueryDto);
        Integer count = this.baseMapper.listCount(flowIndexQueryDto);
        return ApiResult.success(flowIndexList, count);
    }

    /**
     * 流量指标信新增
     *
     * @param flowIndex 实体
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(FlowIndex flowIndex) {
        AssertUtils.notNull(flowIndex, "实体数据不能为空");
        AssertUtils.notNull(flowIndex.getType(), "请指定流量类型");
        AssertUtils.hasText(flowIndex.getContentType(), "请指定内容模块");
        AssertUtils.notNull(flowIndex.getContentId(), "请指定内容ID");
        if (Objects.equals(flowIndex.getType(), FlowIndexEnum.REMAIN.getType())) {
            AssertUtils.notNull(flowIndex.getTimes(), "停留时长不能为空");
        }
        flowIndex.setUserId(LocalThreadHolder.getUserId()); // 设置上用户ID
        flowIndex.setCreateTime(LocalDateTime.now()); // 设置时间
        save(flowIndex);
        return ApiResult.success();
    }

    /**
     * 浏览操作
     *
     * @param flowIndex 实体
     * @return Result<String>
     */
    @Override
    public Result<String> viewOperation(FlowIndex flowIndex) {
        judge(flowIndex);
        FlowIndexQueryDto flowIndexQueryDto = createQueryDto(
                FlowIndexEnum.VIEW.getType(),
                flowIndex.getContentId(),
                flowIndex.getContentType()
        );
        // 先判断用户是不是已经浏览了这篇信息
        Integer count = this.baseMapper.listCount(flowIndexQueryDto);
        // 如果已经浏览，直接抛出异常即可，不必再次新增浏览行为记录
        AssertUtils.isFalse(count != 0, "已产生浏览行为");
        FlowIndex entity = createEntity(
                FlowIndexEnum.VIEW.getType(),
                flowIndex.getContentId(),
                flowIndex.getContentType()
        );
        save(entity);
        return ApiResult.success();
    }

    private FlowIndex createEntity(Integer type, Integer contentId, String contentType) {
        FlowIndex flowIndex = new FlowIndex();
        flowIndex.setType(type); // 声明这种行为操作是浏览操作
        flowIndex.setContentType(contentType);
        flowIndex.setContentId(contentId);
        flowIndex.setUserId(LocalThreadHolder.getUserId()); // 设置上用户ID
        flowIndex.setCreateTime(LocalDateTime.now()); // 设置上当前时间
        return flowIndex;
    }

    private void judge(FlowIndex flowIndex) {
        AssertUtils.notNull(flowIndex, "实体数据不能为空");
        AssertUtils.hasText(flowIndex.getContentType(), "请指定内容模块");
        AssertUtils.notNull(flowIndex.getContentId(), "请指定内容ID");
    }

    private FlowIndexQueryDto createQueryDto(Integer type, Integer contentId, String contentType) {
        FlowIndexQueryDto flowIndexQueryDto = new FlowIndexQueryDto();
        flowIndexQueryDto.setUserId(LocalThreadHolder.getUserId());
        flowIndexQueryDto.setType(type);
        flowIndexQueryDto.setContentId(contentId);
        flowIndexQueryDto.setContentType(contentType);
        return flowIndexQueryDto;
    }

    /**
     * 收藏操作
     *
     * @param flowIndex 实体
     * @return Result<String>
     */
    @Override
    public Result<String> saveOperation(FlowIndex flowIndex) {
        judge(flowIndex);
        FlowIndexQueryDto flowIndexQueryDto = createQueryDto(
                FlowIndexEnum.COLLECTION.getType(),
                flowIndex.getContentId(),
                flowIndex.getContentType());
        // 先判断用户是不是已经收藏了这篇信息
        List<FlowIndex> flowIndexList = this.baseMapper.list(flowIndexQueryDto);
        if (!flowIndexList.isEmpty()) { // 用户已经收藏这篇信息的情况
            // 取消收藏 - 删除用户关于这一条收藏记录的信息
            removeById(flowIndexList.get(0).getId());
            return ApiResult.success("取消收藏成功");
        }
        FlowIndex entity = createEntity(
                FlowIndexEnum.COLLECTION.getType(),
                flowIndex.getContentId(),
                flowIndex.getContentType()
        );
        save(entity);
        return ApiResult.success("收藏成功");
    }

    /**
     * 停留时长
     *
     * @param flowIndex 实体
     * @return Result<String>
     */
    @Override
    public Result<String> stayOperation(FlowIndex flowIndex) {
        judge(flowIndex);
        AssertUtils.notNull(flowIndex.getTimes(), "时长不为空");
        FlowIndex entity = createEntity(
                FlowIndexEnum.REMAIN.getType(),
                flowIndex.getContentId(),
                flowIndex.getContentType()
        );
        entity.setTimes(flowIndex.getTimes());
        save(entity);
        return ApiResult.success();
    }

    /**
     * 查询用户收藏的房屋数据
     *
     * @param flowIndexQueryDto 查询参数
     * @return Result<List < HouseListItemVO>> 响应结果
     */
    @Override
    public Result<List<HouseListItemVO>> saveListHouse(FlowIndexQueryDto flowIndexQueryDto) {
        flowIndexQueryDto.setContentType("HOUSE_INFO");
        List<FlowIndex> flowIndexList = getFlowList(flowIndexQueryDto);
        if (flowIndexList.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        Integer count = this.baseMapper.listCount(flowIndexQueryDto);
        List<Integer> houseIds = flowIndexList.stream()
                .map(FlowIndex::getContentId)
                .collect(Collectors.toList());
        HouseQueryDto houseQueryDto = new HouseQueryDto();
        houseQueryDto.setIds(houseIds);
        List<HouseListItemVO> houseListItemVOS = houseMapper.list(houseQueryDto);
        return ApiResult.success(houseListItemVOS, count);
    }

    /**
     * 查询用户收藏的房屋资讯数据
     *
     * @param flowIndexQueryDto 查询参数
     * @return Result<List < HouseNewsListVO>> 响应结果
     */
    @Override
    public Result<List<HouseNewsListVO>> saveListHouseNews(FlowIndexQueryDto flowIndexQueryDto) {
        flowIndexQueryDto.setContentType("HOUSE_NEWS");
        List<FlowIndex> flowIndexList = getFlowList(flowIndexQueryDto);
        if (flowIndexList.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        Integer count = this.baseMapper.listCount(flowIndexQueryDto);
        List<Integer> houseNewsId = flowIndexList.stream()
                .map(FlowIndex::getContentId)
                .collect(Collectors.toList());
        HouseNewsQueryDto houseNewsQueryDto = new HouseNewsQueryDto();
        houseNewsQueryDto.setIds(houseNewsId);
        List<HouseNewsListVO> houseNewsListVOS = houseNewsMapper.list(houseNewsQueryDto);
        return ApiResult.success(houseNewsListVOS, count);
    }

    private List<FlowIndex> getFlowList(FlowIndexQueryDto flowIndexQueryDto) {
        flowIndexQueryDto.setType(FlowIndexEnum.COLLECTION.getType()); // 设置为收藏类型
        flowIndexQueryDto.setUserId(LocalThreadHolder.getUserId()); // 设置上操作者ID
        return this.baseMapper.list(flowIndexQueryDto);
    }

}


