package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseOrderStatusQueryDto;
import com.kmbeast.pojo.vo.HouseOrderStatusVO;
import com.kmbeast.service.HouseOrderStatusService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 预约看房状态流转评价控制器
 */
@RestController
@RequestMapping("/house-order-status")
public class HouseOrderStatusController {

    @Resource
    private HouseOrderStatusService houseOrderStatusService;

    /**
     * 查询预约看房状态流转评价信息
     *
     * @param queryDto 查询参数
     * @return Result<List < HouseOrderStatusVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<HouseOrderStatusVO>> list(@RequestBody HouseOrderStatusQueryDto queryDto) {
        return houseOrderStatusService.list(queryDto);
    }

}

