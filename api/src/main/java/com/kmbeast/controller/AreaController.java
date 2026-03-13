package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.AreaQueryDto;
import com.kmbeast.pojo.entity.Area;
import com.kmbeast.service.AreaService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 地区控制器
 */
@RestController
@RequestMapping("/area")
public class AreaController {

    @Resource
    private AreaService areaService;

    /**
     * 新增地区信息
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody Area area) {
        return areaService.saveEntity(area);
    }

    /**
     * 删除地区信息
     */
    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Integer id) {
        areaService.removeById(id);
        return ApiResult.success("地区删除成功");
    }

    /**
     * 修改地区信息
     */
    @PutMapping(value = "/update")
    @ResponseBody
    public Result<String> update(@RequestBody Area area) {
        return areaService.update(area);
    }

    /**
     * 查询地区信息
     *
     * @param areaQueryDto 查询参数
     * @return Result<List < Area>> 响应结果
     */
    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<Area>> list(@RequestBody AreaQueryDto areaQueryDto) {
        return areaService.list(areaQueryDto);
    }

}

