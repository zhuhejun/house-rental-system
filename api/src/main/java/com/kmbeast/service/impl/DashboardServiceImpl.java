package com.kmbeast.service.impl;

import com.kmbeast.mapper.HouseMapper;
import com.kmbeast.mapper.HouseNewsMapper;
import com.kmbeast.mapper.HouseOrderInfoMapper;
import com.kmbeast.mapper.LandlordMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.*;
import com.kmbeast.pojo.vo.ChartVO;
import com.kmbeast.pojo.vo.HouseListItemVO;
import com.kmbeast.service.DashboardService;
import com.kmbeast.utils.AssertUtils;
import com.kmbeast.utils.DateUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 仪表盘业务逻辑实现类
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private LandlordMapper landlordMapper;
    @Resource
    private HouseMapper houseMapper;
    @Resource
    private HouseNewsMapper houseNewsMapper;
    @Resource
    private HouseOrderInfoMapper houseOrderInfoMapper;

    /**
     * 统计静态数据
     *
     * @return Result<List < ChartVO>>
     */
    @Override
    public Result<List<ChartVO>> mainStatic() {
        List<ChartVO> chartVOList = new ArrayList<>();
        // 统计房东数量
        Integer landlordNumber = landlordMapper.listCount(new LandlordQueryDto());
        ChartVO landlordChartVO = new ChartVO("房东人数（个）", landlordNumber);
        chartVOList.add(landlordChartVO);
        // 统计房源数量
        Integer houseNumber = houseMapper.listCount(new HouseQueryDto());
        ChartVO houseChartVO = new ChartVO("房源数目（户）", houseNumber);
        chartVOList.add(houseChartVO);
        // 统计房屋资讯
        Integer houseNewsNumber = houseNewsMapper.listCount(new HouseNewsQueryDto());
        ChartVO houseNewsChartVO = new ChartVO("房屋资讯（篇）", houseNewsNumber);
        chartVOList.add(houseNewsChartVO);
        // 统计预约看房订单
        Integer houseOrderInfoNumber = houseOrderInfoMapper.listCount(new HouseOrderInfoQueryDto());
        ChartVO houseOrderInfoChartVO = new ChartVO("预约看房订单（条）", houseOrderInfoNumber);
        chartVOList.add(houseOrderInfoChartVO);
        return ApiResult.success(chartVOList);
    }

    /**
     * 统计房源存量趋势（首页 - 折线图）
     */
    @Override
    public Result<List<ChartVO>> houseLineChart(Integer days) {
        AssertUtils.notNull(days, "日期不能为空");
        // 调用时间工具类，将传进来的数值天数转成对应的LocalDateTime类型时间范围
        QueryDto queryDto = DateUtil.startAndEndTime(days);
        HouseQueryDto houseQueryDto = new HouseQueryDto();
        houseQueryDto.setStartTime(queryDto.getStartTime());
        houseQueryDto.setEndTime(queryDto.getEndTime());
        // 构造完时间之后，调用房屋查询（时间[字段:create_time]的范围查询）
        List<HouseListItemVO> houseListItemVOS = houseMapper.listCreate(houseQueryDto);

        if (houseListItemVOS.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        List<LocalDateTime> dateTimeList = houseListItemVOS.stream()
                .map(HouseListItemVO::getCreateTime)
                .collect(Collectors.toList());
        List<ChartVO> chartVOS = DateUtil.countDatesWithinRange(days, dateTimeList);
        return ApiResult.success(chartVOS);
    }

    /**
     * 管理员首页 - 城市待租房源分布
     */
    @Override
    public Result<List<ChartVO>> cityHouseRange(Integer limit) {
        AssertUtils.notNull(limit, "查询条数不能为空");
        List<ChartVO> chartVOS = houseMapper.cityHouseRange(limit);
        return ApiResult.success(chartVOS);
    }
}
