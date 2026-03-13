package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.RentalContractStatusQueryDto;
import com.kmbeast.pojo.vo.RentalContractStatusVO;
import com.kmbeast.service.RentalContractStatusService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租赁合同状态流转控制器
 */
@RestController
@RequestMapping("/rental-contract-status")
public class RentalContractStatusController {

    @Resource
    private RentalContractStatusService rentalContractStatusService;

    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<RentalContractStatusVO>> list(@RequestBody RentalContractStatusQueryDto queryDto) {
        return rentalContractStatusService.list(queryDto);
    }
}
