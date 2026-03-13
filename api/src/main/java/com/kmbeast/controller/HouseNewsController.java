package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseNewsQueryDto;
import com.kmbeast.pojo.entity.HouseNews;
import com.kmbeast.pojo.vo.HouseNewsListVO;
import com.kmbeast.service.HouseNewsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 房屋资讯控制器
 */
@RestController
@RequestMapping("/house-news")
public class HouseNewsController {

    @Resource
    private HouseNewsService houseNewsService;

    /**
     * 新增房屋资讯信息
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody HouseNews houseNews) {
        return houseNewsService.saveEntity(houseNews);
    }


    /**
     * 修改房屋资讯信息
     */
    @PutMapping(value = "/update")
    @ResponseBody
    public Result<String> update(@RequestBody HouseNews houseNews) {
        return houseNewsService.updateEntity(houseNews);
    }


    /**
     * 删除房屋资讯信息
     */
    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Integer id) {
        houseNewsService.removeById(id);
        return ApiResult.success("房屋资讯删除成功");
    }

    /**
     * 通过ID查询资讯详情信息
     */
    @GetMapping(value = "/getById/{id}")
    @ResponseBody
    public Result<HouseNews> getById(@PathVariable Integer id) {
        return houseNewsService.selectById(id);
    }

    /**
     * 查询房屋资讯信息
     *
     * @param houseNewsQueryDto 查询参数
     * @return Result<List < FlowIndex>> 响应结果
     */
    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<HouseNewsListVO>> list(@RequestBody HouseNewsQueryDto houseNewsQueryDto) {
        return houseNewsService.list(houseNewsQueryDto);
    }

    /**
     * 房屋资讯推荐
     *
     * @param count 推荐的条数
     * @return Result<List < HouseNewsListVO>> 响应结果
     */
    @GetMapping(value = "/recommend/{count}")
    @ResponseBody
    public Result<List<HouseNewsListVO>> recommend(@PathVariable Integer count) {
        return houseNewsService.recommend(count);
    }

}

