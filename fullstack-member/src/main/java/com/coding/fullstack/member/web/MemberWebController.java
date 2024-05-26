package com.coding.fullstack.member.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.coding.common.utils.R;
import com.coding.fullstack.member.feign.OrderFeignService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MemberWebController {
    private final OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")

    public String memberOrderPage(Model model,
        @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum) {
        // 查出当前登录的用户的所有订单列表数据
        Map<String, Object> params = new HashMap<>();
        params.put("page", String.valueOf(pageNum));
        R r = orderFeignService.listWithItem(params);
        model.addAttribute("orders", r);
        return "orderList.html";
    }
}
