package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.FlowIndexQueryDto;
import com.kmbeast.pojo.entity.FlowIndex;
import com.kmbeast.pojo.vo.HouseListItemVO;
import com.kmbeast.pojo.vo.HouseNewsListVO;
import com.kmbeast.service.FlowIndexService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

/**
 * 流量指标控制器
 */
@RestController
@RequestMapping("/flow-index")
public class FlowIndexController {

    @Resource
    private FlowIndexService flowIndexService;

    /**
     * 新增流量指标信息
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody FlowIndex flowIndex) {
        return flowIndexService.saveEntity(flowIndex);
    }

    /**
     * 浏览操作
     */
    @PostMapping(value = "/viewOperation")
    @ResponseBody
    public Result<String> viewOperation(@RequestBody FlowIndex flowIndex) {
        return flowIndexService.viewOperation(flowIndex);
    }

    /**
     * 停留操作
     */
    @PostMapping(value = "/stayOperation")
    @ResponseBody
    public Result<String> stayOperation(@RequestBody FlowIndex flowIndex) {
        return flowIndexService.stayOperation(flowIndex);
    }

    /**
     * 收藏操作
     */
    @PostMapping(value = "/saveOperation")
    @ResponseBody
    public Result<String> saveOperation(@RequestBody FlowIndex flowIndex) {
        return flowIndexService.saveOperation(flowIndex);
    }

    /**
     * 删除流量指标信息
     */
    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Integer id) {
        flowIndexService.removeById(id);
        return ApiResult.success("流量指标删除成功");
    }

    /**
     * 查询用户名下的各类流量指标信息
     *
     * @param flowIndexQueryDto 查询参数
     * @return Result<List < FlowIndex>> 响应结果
     */
    @Pager
    @PostMapping(value = "/listUser")
    @ResponseBody
    public Result<List<FlowIndex>> listUser(@RequestBody FlowIndexQueryDto flowIndexQueryDto) {
        flowIndexQueryDto.setUserId(LocalThreadHolder.getUserId());
        return flowIndexService.list(flowIndexQueryDto);
    }


    /**
     * 查询流量指标信息
     *
     * @param flowIndexQueryDto 查询参数
     * @return Result<List < FlowIndex>> 响应结果
     */
    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<FlowIndex>> list(@RequestBody FlowIndexQueryDto flowIndexQueryDto) {
        return flowIndexService.list(flowIndexQueryDto);
    }

    /**
     * 查询用户收藏的房屋数据
     *
     * @param flowIndexQueryDto 查询参数
     * @return Result<List < HouseListItemVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/saveListHouse")
    @ResponseBody
    public Result<List<HouseListItemVO>> saveListHouse(@RequestBody FlowIndexQueryDto flowIndexQueryDto) {
        return flowIndexService.saveListHouse(flowIndexQueryDto);
    }

    /**
     * 查询用户收藏的房屋资讯数据
     *
     * @param flowIndexQueryDto 查询参数
     * @return Result<List < HouseNewsListVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/saveListHouseNews")
    @ResponseBody
    public Result<List<HouseNewsListVO>> saveListHouseNews(@RequestBody FlowIndexQueryDto flowIndexQueryDto) {
        return flowIndexService.saveListHouseNews(flowIndexQueryDto);
    }

}

