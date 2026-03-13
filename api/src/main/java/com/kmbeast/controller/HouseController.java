package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.mapper.HouseMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseQueryDto;
import com.kmbeast.pojo.entity.House;
import com.kmbeast.pojo.vo.*;
import com.kmbeast.service.HouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 房屋控制器
 */
@RestController
@RequestMapping("/house")
public class HouseController {

    @Resource
    private HouseService houseService;
    @Autowired
    private HouseMapper houseMapper;

    /**
     * 新增房屋信息
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody House house) {
        return houseService.saveEntity(house);
    }

    /**
     * 删除房屋信息
     */
    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Integer id) {
        houseService.removeById(id);
        return ApiResult.success("房屋删除成功");
    }

    /**
     * 修改房屋信息
     */
    @PutMapping(value = "/update")
    @ResponseBody
    public Result<String> update(@RequestBody House house) {
        return houseService.update(house);
    }

    /**
     * 查询房屋类型列表
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @GetMapping(value = "/houseTypeList")
    @ResponseBody
    public Result<List<SelectedVO>> houseTypeList() {
        return houseService.houseTypeList();
    }

    /**
     * 查询房屋朝向列表
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @GetMapping(value = "/houseDirectionList")
    @ResponseBody
    public Result<List<SelectedVO>> houseDirectionList() {
        return houseService.houseDirectionList();
    }

    /**
     * 查询房屋户型列表
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @GetMapping(value = "/houseSizedList")
    @ResponseBody
    public Result<List<SelectedVO>> houseSizedList() {
        return houseService.houseSizedList();
    }

    /**
     * 查询房屋押金方式列表
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @GetMapping(value = "/houseDepositMethodList")
    @ResponseBody
    public Result<List<SelectedVO>> houseDepositMethodList() {
        return houseService.houseDepositMethodList();
    }

    /**
     * 查询房屋是否临近地铁列表
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @GetMapping(value = "/houseSubwayList")
    @ResponseBody
    public Result<List<SelectedVO>> houseSubwayList() {
        return houseService.houseSubwayList();
    }

    /**
     * 查询房屋装修状态
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @GetMapping(value = "/houseFitmentStatusList")
    @ResponseBody
    public Result<List<SelectedVO>> houseFitmentStatusList() {
        return houseService.houseFitmentStatusList();
    }

    /**
     * 查询房屋租赁方式
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @GetMapping(value = "/houseRentalTypeList")
    @ResponseBody
    public Result<List<SelectedVO>> houseRentalTypeList() {
        return houseService.houseRentalTypeList();
    }

    /**
     * 查询房屋生活设施配置信息项
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @GetMapping(value = "/houseLivingFacilityList")
    @ResponseBody
    public Result<List<LivingFacilityVO>> houseLivingFacilityList() {
        return houseService.houseLivingFacilityList();
    }

    /**
     * 查询房屋面积查询条件范围
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @GetMapping(value = "/houseSizeNumber")
    @ResponseBody
    public Result<List<SelectedVO>> houseSizeNumber() {
        return houseService.houseSizeNumber();
    }

    /**
     * 查询房屋租金查询条件范围
     *
     * @return Result<List < SelectedVO>> 响应结果
     */
    @GetMapping(value = "/houseRentRange")
    @ResponseBody
    public Result<List<SelectedVO>> houseRentRange() {
        return houseService.houseRentRange();
    }

    /**
     * 查询房东自己的房屋信息
     *
     * @param houseQueryDto 查询参数
     * @return Result<List < HouseListItemVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/landlordHouseList")
    @ResponseBody
    public Result<List<HouseListItemVO>> landlordHouseList(@RequestBody HouseQueryDto houseQueryDto) {
        return houseService.landlordHouseList(houseQueryDto);
    }

    /**
     * 房东上架或下架房源操作
     *
     * @param id 房源ID
     * @return Result<String> 响应结果
     */
    @PutMapping(value = "/houseStatusDeal/{id}")
    @ResponseBody
    public Result<String> houseStatusDeal(@PathVariable Integer id) {
        return houseService.houseStatusDeal(id);
    }

    /**
     * 通过ID查询房屋详情信息
     *
     * @param id 房源ID
     * @return Result<HouseVO> 响应结果
     */
    @GetMapping(value = "/getById/{id}")
    @ResponseBody
    public Result<HouseVO> selectById(@PathVariable Integer id) {
        return houseService.selectById(id);
    }

    /**
     * 用户首页查询房屋信息 - 前提是只能查询上架的房屋信息
     *
     * @param houseQueryDto 查询参数
     * @return Result<List < HouseListItemVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/listUser")
    @ResponseBody
    public Result<List<HouseListItemVO>> listUser(@RequestBody HouseQueryDto houseQueryDto) {
        return houseService.listUser(houseQueryDto);
    }

    /**
     * 查询房屋信息
     *
     * @param houseQueryDto 查询参数
     * @return Result<List < HouseListItemVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<HouseListItemVO>> list(@RequestBody HouseQueryDto houseQueryDto) {
        return houseService.list(houseQueryDto);
    }

    /**
     * 统计房屋流量
     *
     * @param houseQueryDto 查询参数
     * @return Result<List < HouseFlowIndexVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/listFlowIndex")
    @ResponseBody
    public Result<List<HouseFlowIndexVO>> listFlowIndex(@RequestBody HouseQueryDto houseQueryDto) {
        return houseService.listFlowIndex(houseQueryDto);
    }

    /**
     * 流量指标可视化
     *
     * @param houseQueryDto 查询参数
     * @return Result<List < ChartVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/listChart")
    @ResponseBody
    public Result<List<ChartVO>> listChart(@RequestBody HouseQueryDto houseQueryDto) {
        return houseService.listChart(houseQueryDto);
    }

    /**
     * 房屋推荐
     *
     * @param count 推荐的条数
     * @return Result<List < HouseListItemVO>> 响应结果
     */
    @GetMapping(value = "/recommend/{count}")
    @ResponseBody
    public Result<List<HouseListItemVO>> recommend(@PathVariable Integer count) {
        return houseService.recommend(count);
    }

}

