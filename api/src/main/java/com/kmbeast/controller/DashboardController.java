package com.kmbeast.controller;

import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.vo.ChartVO;
import com.kmbeast.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    /**
     * 统计静态数据
     */
    @GetMapping(value = "/mainStatic")
    @ResponseBody
    public Result<List<ChartVO>> mainStatic() {
        return dashboardService.mainStatic();
    }

    /**
     * 统计房源存量趋势（首页 - 折线图）
     */
    @GetMapping(value = "/houseLineChart/{days}")
    @ResponseBody
    public Result<List<ChartVO>> houseLineChart(@PathVariable Integer days) {
        return dashboardService.houseLineChart(days);
    }

    /**
     * 管理员首页 - 城市待租房源分布
     */
    @GetMapping(value = "/cityHouseRange/{limit}")
    @ResponseBody
    public Result<List<ChartVO>> cityHouseRange(@PathVariable Integer limit) {
        return dashboardService.cityHouseRange(limit);
    }

}

