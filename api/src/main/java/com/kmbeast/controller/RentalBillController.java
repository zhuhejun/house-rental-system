package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.RentalBillQueryDto;
import com.kmbeast.pojo.entity.RentalBill;
import com.kmbeast.pojo.vo.RentalBillVO;
import com.kmbeast.service.RentalBillService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租赁账单控制器
 */
@RestController
@RequestMapping("/rental-bill")
public class RentalBillController {

    @Resource
    private RentalBillService rentalBillService;

    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody RentalBill rentalBill) {
        return rentalBillService.saveEntity(rentalBill);
    }

    @GetMapping(value = "/getById/{id}")
    @ResponseBody
    public Result<RentalBillVO> getById(@PathVariable Integer id) {
        return rentalBillService.getById(id);
    }

    @PutMapping(value = "/submitUtilityProof")
    @ResponseBody
    public Result<String> submitUtilityProof(@RequestBody RentalBill rentalBill) {
        return rentalBillService.submitUtilityProof(rentalBill);
    }

    @PutMapping(value = "/auditUtilityProof")
    @ResponseBody
    public Result<String> auditUtilityProof(@RequestBody RentalBill rentalBill) {
        return rentalBillService.auditUtilityProof(rentalBill);
    }

    @Pager
    @PostMapping(value = "/listUser")
    @ResponseBody
    public Result<List<RentalBillVO>> listUser(@RequestBody RentalBillQueryDto queryDto) {
        return rentalBillService.listUser(queryDto);
    }

    @Pager
    @PostMapping(value = "/listLandlord")
    @ResponseBody
    public Result<List<RentalBillVO>> listLandlord(@RequestBody RentalBillQueryDto queryDto) {
        return rentalBillService.listLandlord(queryDto);
    }

    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<RentalBillVO>> list(@RequestBody RentalBillQueryDto queryDto) {
        return rentalBillService.list(queryDto);
    }
}
