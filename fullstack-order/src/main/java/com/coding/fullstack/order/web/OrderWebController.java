package com.coding.fullstack.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.coding.fullstack.order.service.OrderService;
import com.coding.fullstack.order.vo.OrderConfirmVo;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;

@Controller
@RequiredArgsConstructor
public class OrderWebController {

    private final OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }
}
