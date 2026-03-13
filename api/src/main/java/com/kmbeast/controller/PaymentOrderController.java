package com.kmbeast.controller;

import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.vo.PaymentStartVO;
import com.kmbeast.pojo.vo.RentalBillVO;
import com.kmbeast.service.PaymentOrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付订单控制器
 */
@RestController
@RequestMapping("/payment-order")
public class PaymentOrderController {

    @Resource
    private PaymentOrderService paymentOrderService;

    @PostMapping(value = "/payBill/{billId}")
    @ResponseBody
    public Result<PaymentStartVO> payBill(@PathVariable Integer billId) {
        return paymentOrderService.createPagePay(billId);
    }

    @GetMapping(value = "/queryBillStatus/{billId}")
    @ResponseBody
    public Result<RentalBillVO> queryBillStatus(@PathVariable Integer billId) {
        return paymentOrderService.queryBillStatus(billId);
    }

    @PostMapping(value = "/notify")
    @ResponseBody
    public String notify(HttpServletRequest request) {
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : requestMap.entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                params.put(entry.getKey(), values[0]);
            }
        }
        return paymentOrderService.handleNotify(params);
    }
}
