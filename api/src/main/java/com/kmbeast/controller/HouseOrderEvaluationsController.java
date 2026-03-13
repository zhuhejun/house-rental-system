package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseOrderEvaluationsQueryDto;
import com.kmbeast.pojo.entity.HouseOrderEvaluations;
import com.kmbeast.pojo.vo.HouseOrderEvaluationsVO;
import com.kmbeast.service.HouseOrderEvaluationsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 预约看房评价控制器
 */
@RestController
@RequestMapping("/house-order-evaluations")
public class HouseOrderEvaluationsController {

    @Resource
    private HouseOrderEvaluationsService houseOrderEvaluationsService;

    /**
     * 新增预约看房评价信息
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody HouseOrderEvaluations houseOrderEvaluations) {
        return houseOrderEvaluationsService.saveEntity(houseOrderEvaluations);
    }

    /**
     * 删除预约看房评价信息
     */
    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Integer id) {
        houseOrderEvaluationsService.removeById(id);
        return ApiResult.success("预约看房评价删除成功");
    }

    /**
     * 查询用户自己的预约看房评价信息
     *
     * @param queryDto 查询参数
     * @return Result<List < HouseOrderEvaluationsVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/listUser")
    @ResponseBody
    public Result<List<HouseOrderEvaluationsVO>> listUser(@RequestBody HouseOrderEvaluationsQueryDto queryDto) {
        queryDto.setUserId(LocalThreadHolder.getUserId());
        return houseOrderEvaluationsService.list(queryDto);
    }

    /**
     * 查询预约看房评价信息
     *
     * @param queryDto 查询参数
     * @return Result<List < HouseOrderEvaluationsVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<HouseOrderEvaluationsVO>> list(@RequestBody HouseOrderEvaluationsQueryDto queryDto) {
        return houseOrderEvaluationsService.list(queryDto);
    }

    /**
     * 查询房源所产生的预约看房评价信息
     *
     * @param houseId 房屋ID
     * @return Result<List < HouseOrderEvaluationsVO>> 响应结果
     */
    @Pager
    @GetMapping(value = "/houseList/{houseId}")
    @ResponseBody
    public Result<List<HouseOrderEvaluationsVO>> houseList(@PathVariable Integer houseId) {
        return houseOrderEvaluationsService.houseList(houseId);
    }

}

