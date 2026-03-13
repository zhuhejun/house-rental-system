package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.CommunityQueryDto;
import com.kmbeast.pojo.entity.Community;
import com.kmbeast.pojo.vo.CommunityVO;
import com.kmbeast.service.CommunityService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 小区控制器
 */
@RestController
@RequestMapping("/community")
public class CommunityController {

    @Resource
    private CommunityService communityService;

    /**
     * 新增小区信息
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody Community community) {
        return communityService.saveEntity(community);
    }

    /**
     * 删除小区信息
     */
    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Integer id) {
        communityService.removeById(id);
        return ApiResult.success("小区删除成功");
    }

    /**
     * 修改小区信息
     */
    @PutMapping(value = "/update")
    @ResponseBody
    public Result<String> update(@RequestBody Community community) {
        return communityService.update(community);
    }

    /**
     * 通过ID查询小区详情信息
     *
     * @param id 小区ID
     * @return Result<CommunityVO> 响应结果
     */
    @GetMapping(value = "/getById/{id}")
    @ResponseBody
    public Result<CommunityVO> getById(@PathVariable Integer id) {
        return communityService.selectById(id);
    }

    /**
     * 查询小区信息
     *
     * @param communityQueryDto 查询参数
     * @return Result<List < CommunityVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<CommunityVO>> list(@RequestBody CommunityQueryDto communityQueryDto) {
        return communityService.list(communityQueryDto);
    }

}

