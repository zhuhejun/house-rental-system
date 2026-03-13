package com.kmbeast.service;

import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.vo.ChartVO;

import java.util.List;

/**
 * 仪表盘业务逻辑接口
 */
public interface DashboardService {

    Result<List<ChartVO>> mainStatic();

    Result<List<ChartVO>> houseLineChart(Integer days);

    Result<List<ChartVO>> cityHouseRange(Integer limit);

}
