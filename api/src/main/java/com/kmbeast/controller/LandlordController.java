package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.LandlordQueryDto;
import com.kmbeast.pojo.entity.Landlord;
import com.kmbeast.pojo.vo.LandlordVO;
import com.kmbeast.service.LandlordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 房东控制器
 */
@RestController
@RequestMapping("/landlord")
public class LandlordController {

    @Resource
    private LandlordService landlordService;

    /**
     * 新增房东信息
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody Landlord landlord) {
        return landlordService.saveEntity(landlord);
    }

    /**
     * 删除房东信息
     */
    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public Result<String> delete(@PathVariable Integer id) {
        landlordService.removeById(id);
        return ApiResult.success("房东删除成功");
    }

    /**
     * 修改房东信息
     */
    @PutMapping(value = "/update")
    @ResponseBody
    public Result<String> update(@RequestBody Landlord landlord) {
        return landlordService.update(landlord);
    }

    /**
     * 查询用户自己的房东申请信息
     *
     * @param landlordQueryDto 查询参数
     * @return Result<LandlordVO> 响应结果
     */
    @Pager
    @PostMapping(value = "/listUser")
    @ResponseBody
    public Result<LandlordVO> listUser(@RequestBody LandlordQueryDto landlordQueryDto) {
        return landlordService.listUser(landlordQueryDto);
    }

    /**
     * 查询房东信息
     *
     * @param landlordQueryDto 查询参数
     * @return Result<List < LandlordVO>> 响应结果
     */
    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<LandlordVO>> list(@RequestBody LandlordQueryDto landlordQueryDto) {
        return landlordService.list(landlordQueryDto);
    }

}

