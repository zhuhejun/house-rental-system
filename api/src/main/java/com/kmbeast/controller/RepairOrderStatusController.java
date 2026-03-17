package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.RepairOrderStatusQueryDto;
import com.kmbeast.pojo.vo.RepairOrderStatusVO;
import com.kmbeast.service.RepairOrderStatusService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 报修工单状态流转控制器
 */
@RestController
@RequestMapping("/repair-order-status")
public class RepairOrderStatusController {

    @Resource
    private RepairOrderStatusService repairOrderStatusService;

    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<RepairOrderStatusVO>> list(@RequestBody RepairOrderStatusQueryDto queryDto) {
        return repairOrderStatusService.list(queryDto);
    }
}
