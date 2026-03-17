package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.RepairOrderQueryDto;
import com.kmbeast.pojo.entity.RepairOrder;
import com.kmbeast.pojo.vo.RepairOrderVO;
import com.kmbeast.service.RepairOrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 报修工单控制器
 */
@RestController
@RequestMapping("/repair-order")
public class RepairOrderController {

    @Resource
    private RepairOrderService repairOrderService;

    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody RepairOrder repairOrder) {
        return repairOrderService.saveEntity(repairOrder);
    }

    @GetMapping(value = "/getById/{id}")
    @ResponseBody
    public Result<RepairOrderVO> getById(@PathVariable Integer id) {
        return repairOrderService.getById(id);
    }

    @PutMapping(value = "/landlordAccept/{id}")
    @ResponseBody
    public Result<String> landlordAccept(@PathVariable Integer id) {
        return repairOrderService.landlordAccept(id);
    }

    @PutMapping(value = "/landlordProcessing/{id}")
    @ResponseBody
    public Result<String> landlordProcessing(@PathVariable Integer id) {
        return repairOrderService.landlordProcessing(id);
    }

    @PutMapping(value = "/landlordFinish")
    @ResponseBody
    public Result<String> landlordFinish(@RequestBody RepairOrder repairOrder) {
        return repairOrderService.landlordFinish(repairOrder);
    }

    @PutMapping(value = "/tenantConfirmFinish/{id}")
    @ResponseBody
    public Result<String> tenantConfirmFinish(@PathVariable Integer id) {
        return repairOrderService.tenantConfirmFinish(id);
    }

    @PutMapping(value = "/tenantCancel")
    @ResponseBody
    public Result<String> tenantCancel(@RequestBody RepairOrder repairOrder) {
        return repairOrderService.tenantCancel(repairOrder);
    }

    @PutMapping(value = "/adminReject")
    @ResponseBody
    public Result<String> adminReject(@RequestBody RepairOrder repairOrder) {
        return repairOrderService.adminReject(repairOrder);
    }

    @Pager
    @PostMapping(value = "/listUser")
    @ResponseBody
    public Result<List<RepairOrderVO>> listUser(@RequestBody RepairOrderQueryDto queryDto) {
        return repairOrderService.listUser(queryDto);
    }

    @Pager
    @PostMapping(value = "/listLandlord")
    @ResponseBody
    public Result<List<RepairOrderVO>> listLandlord(@RequestBody RepairOrderQueryDto queryDto) {
        return repairOrderService.listLandlord(queryDto);
    }

    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<RepairOrderVO>> list(@RequestBody RepairOrderQueryDto queryDto) {
        return repairOrderService.list(queryDto);
    }
}
