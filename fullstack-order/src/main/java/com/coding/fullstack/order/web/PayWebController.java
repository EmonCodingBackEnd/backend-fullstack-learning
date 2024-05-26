package com.coding.fullstack.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alipay.api.AlipayApiException;
import com.coding.fullstack.order.config.AlipayConfig;
import com.coding.fullstack.order.service.OrderService;
import com.coding.fullstack.order.vo.PayVo;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PayWebController {

    private final AlipayConfig alipayConfig;
    private final OrderService orderService;

    /**
     * 1、将支付页让浏览器展示 2、支付成功以后，我们要跳到用户的订单列表页
     * 
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */
    @ResponseBody
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);
        String result = alipayConfig.pay(payVo);
        return result;
    }
}
