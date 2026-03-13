package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.RentalContractQueryDto;
import com.kmbeast.pojo.entity.RentalContract;
import com.kmbeast.pojo.vo.RentalContractVO;
import com.kmbeast.service.RentalContractService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租赁合同控制器
 */
@RestController
@RequestMapping("/rental-contract")
public class RentalContractController {

    @Resource
    private RentalContractService rentalContractService;

    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody RentalContract rentalContract) {
        return rentalContractService.saveEntity(rentalContract);
    }

    @GetMapping(value = "/getById/{id}")
    @ResponseBody
    public Result<RentalContractVO> getById(@PathVariable Integer id) {
        return rentalContractService.getById(id);
    }

    @PutMapping(value = "/adminApprove/{id}")
    @ResponseBody
    public Result<String> adminApprove(@PathVariable Integer id) {
        return rentalContractService.adminApprove(id);
    }

    @PutMapping(value = "/adminReject")
    @ResponseBody
    public Result<String> adminReject(@RequestBody RentalContract rentalContract) {
        return rentalContractService.adminReject(rentalContract);
    }

    @PutMapping(value = "/tenantConfirm/{id}")
    @ResponseBody
    public Result<String> tenantConfirm(@PathVariable Integer id) {
        return rentalContractService.tenantConfirm(id);
    }

    @PutMapping(value = "/tenantReject")
    @ResponseBody
    public Result<String> tenantReject(@RequestBody RentalContract rentalContract) {
        return rentalContractService.tenantReject(rentalContract);
    }

    @PutMapping(value = "/cancel")
    @ResponseBody
    public Result<String> cancel(@RequestBody RentalContract rentalContract) {
        return rentalContractService.cancel(rentalContract);
    }

    @Pager
    @PostMapping(value = "/listUser")
    @ResponseBody
    public Result<List<RentalContractVO>> listUser(@RequestBody RentalContractQueryDto queryDto) {
        return rentalContractService.listUser(queryDto);
    }

    @Pager
    @PostMapping(value = "/listLandlord")
    @ResponseBody
    public Result<List<RentalContractVO>> listLandlord(@RequestBody RentalContractQueryDto queryDto) {
        return rentalContractService.listLandlord(queryDto);
    }

    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<RentalContractVO>> list(@RequestBody RentalContractQueryDto queryDto) {
        return rentalContractService.list(queryDto);
    }
}
