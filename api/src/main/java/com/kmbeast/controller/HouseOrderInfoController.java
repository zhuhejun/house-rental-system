package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseOrderInfoQueryDto;
import com.kmbeast.pojo.em.DateTimeSplitEnum;
import com.kmbeast.pojo.entity.HouseOrderInfo;
import com.kmbeast.pojo.vo.HouseOrderInfoVO;
import com.kmbeast.pojo.vo.SelectedVO;
import com.kmbeast.service.HouseOrderInfoService;
import com.kmbeast.utils.DateUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 预约看房控制器
 */
@RestController
@RequestMapping("/house-order-info")
public class HouseOrderInfoController {

    @Resource
    private HouseOrderInfoService houseOrderInfoService;

    /**
     * 新增预约看房信息
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody HouseOrderInfo houseOrderInfo) {
        return houseOrderInfoService.saveEntity(houseOrderInfo);
    }

    /**
     * 生成预约看房时间段
     */
    @GetMapping(value = "/split")
    @ResponseBody
    public Result<List<SelectedVO>> split() {
        DateTimeSplitEnum[] dateTimeSplitEnums = DateTimeSplitEnum.values();
        List<SelectedVO> selectedVOS = new ArrayList<>();
        for (DateTimeSplitEnum dateTimeSplitEnum : dateTimeSplitEnums) {
            SelectedVO selectedVO = new SelectedVO(dateTimeSplitEnum.getType(), dateTimeSplitEnum.getDetail());
            selectedVOS.add(selectedVO);
        }
        return ApiResult.success(selectedVOS);
    }

    /**
     * 生成一段时间内的日期
     */
    @GetMapping(value = "/{days}")
    @ResponseBody
    public Result<List<String>> generateFutureDates(@PathVariable Integer days) {
        return ApiResult.success(DateUtil.generateFutureDates(days));
    }


    /**
     * 修改预约看房信息
     */
    @PutMapping(value = "/update")
    @ResponseBody
    public Result<String> update(@RequestBody HouseOrderInfo houseOrderInfo) {
        return houseOrderInfoService.updateEntity(houseOrderInfo);
    }


    /**
     * 删除预约看房信息
     */
    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Integer id) {
        houseOrderInfoService.removeById(id);
        return ApiResult.success("预约看房删除成功");
    }

    /**
     * 查询用户自己的预约看房信息
     *
     * @param houseOrderInfoQueryDto 查询参数
     * @return Result<List < HouseOrderInfoVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/listUser")
    @ResponseBody
    public Result<List<HouseOrderInfoVO>> listUser(@RequestBody HouseOrderInfoQueryDto houseOrderInfoQueryDto) {
        houseOrderInfoQueryDto.setUserId(LocalThreadHolder.getUserId());
        return houseOrderInfoService.list(houseOrderInfoQueryDto);
    }

    /**
     * 查询房东名下维护的房屋所产生的预约看房信息
     *
     * @param houseOrderInfoQueryDto 查询参数
     * @return Result<List < HouseOrderInfoVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/listLandlord")
    @ResponseBody
    public Result<List<HouseOrderInfoVO>> listLandlord(@RequestBody HouseOrderInfoQueryDto houseOrderInfoQueryDto) {
        return houseOrderInfoService.listLandlord(houseOrderInfoQueryDto);
    }


    /**
     * 查询预约看房信息
     *
     * @param houseOrderInfoQueryDto 查询参数
     * @return Result<List < HouseOrderInfoVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<HouseOrderInfoVO>> list(@RequestBody HouseOrderInfoQueryDto houseOrderInfoQueryDto) {
        return houseOrderInfoService.list(houseOrderInfoQueryDto);
    }

}

