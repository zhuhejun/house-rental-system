package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.UserAreaQueryDto;
import com.kmbeast.pojo.entity.UserArea;
import com.kmbeast.pojo.vo.UserAreaVO;
import com.kmbeast.service.UserAreaService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户常居住地控制器
 */
@RestController
@RequestMapping("/user-area")
public class UserAreaController {

    @Resource
    private UserAreaService userAreaService;

    /**
     * 新增常居住地信息
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody UserArea userArea) {
        return userAreaService.saveEntity(userArea);
    }

    /**
     * 修改常居住地信息
     */
    @PutMapping(value = "/update")
    @ResponseBody
    public Result<String> update(@RequestBody UserArea userArea) {
        return userAreaService.update(userArea);
    }

    /**
     * 查询用户自己的常居住地信息
     *
     * @param userAreaQueryDto 查询参数
     * @return Result<List < UserArea>> 响应结果
     */
    @Pager
    @PostMapping(value = "/listUser")
    @ResponseBody
    public Result<List<UserAreaVO>> listUser(@RequestBody UserAreaQueryDto userAreaQueryDto) {
        userAreaQueryDto.setUserId(LocalThreadHolder.getUserId()); // 数据隔离
        return userAreaService.listUser(userAreaQueryDto);
    }

    /**
     * 查询常居住地信息
     *
     * @param userAreaQueryDto 查询参数
     * @return Result<List < UserArea>> 响应结果
     */
    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<UserArea>> list(@RequestBody UserAreaQueryDto userAreaQueryDto) {
        return userAreaService.list(userAreaQueryDto);
    }

}

