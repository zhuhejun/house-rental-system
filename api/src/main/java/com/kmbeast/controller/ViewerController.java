package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseNewsQueryDto;
import com.kmbeast.pojo.dto.HouseQueryDto;
import com.kmbeast.pojo.entity.HouseNews;
import com.kmbeast.pojo.vo.HouseListItemVO;
import com.kmbeast.pojo.vo.HouseNewsListVO;
import com.kmbeast.pojo.vo.HouseVO;
import com.kmbeast.pojo.vo.SelectedVO;
import com.kmbeast.service.HouseNewsService;
import com.kmbeast.service.HouseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 游客控制器
 */
@RestController
@RequestMapping("/viewer")
public class ViewerController {

    @Resource
    private HouseService houseService;
    @Resource
    private HouseNewsService houseNewsService;

    /**
     * 查询房屋信息
     *
     * @param houseQueryDto 查询参数
     * @return Result<List < HouseListItemVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/listHouse")
    @ResponseBody
    public Result<List<HouseListItemVO>> list(@RequestBody HouseQueryDto houseQueryDto) {
        return houseService.list(houseQueryDto);
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
     * 通过ID查询房屋详情信息
     *
     * @param id 房源ID
     * @return Result<HouseVO> 响应结果
     */
    @GetMapping(value = "/getHouseById/{id}")
    @ResponseBody
    public Result<HouseVO> selectById(@PathVariable Integer id) {
        return houseService.selectById(id);
    }

    /**
     * 查询房屋资讯信息
     *
     * @param houseNewsQueryDto 查询参数
     * @return Result<List < FlowIndex>> 响应结果
     */
    @Pager
    @PostMapping(value = "/listHouseNews")
    @ResponseBody
    public Result<List<HouseNewsListVO>> list(@RequestBody HouseNewsQueryDto houseNewsQueryDto) {
        return houseNewsService.list(houseNewsQueryDto);
    }

    /**
     * 通过ID查询资讯详情信息
     */
    @GetMapping(value = "/getNewsById/{id}")
    @ResponseBody
    public Result<HouseNews> getById(@PathVariable Integer id) {
        return houseNewsService.selectById(id);
    }

}

