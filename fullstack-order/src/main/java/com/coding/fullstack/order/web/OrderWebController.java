package com.coding.fullstack.order.web;

import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.coding.fullstack.order.service.OrderService;
import com.coding.fullstack.order.vo.OrderConfirmVo;
import com.coding.fullstack.order.vo.OrderSubmitVo;
import com.coding.fullstack.order.vo.SubmitOrderResponseVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    @PostMapping("/subm" + "itOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {
        log.info("订单参数：{}", vo);
        SubmitOrderResponseVo res = null;
        try {
            res = orderService.submitOrder(vo);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", e.getMessage());
            return "redirect:http://order.fsmall.com/toTrade";
        }

        if (res.getCode() == 0) {
            // 下单成功来到支付选择页
            model.addAttribute("submitOrderResp", res);
            return "pay";
        } else {
            String msg = "下单失败：";
            switch (res.getCode()) {
                case 1:
                    msg += "订单信息过期，请刷新再次提交";
                    break;
                case 2:
                    msg += "订单商品价格发生变化，请确认后再次提交";
                    break;
                case 3:
                    msg += "库存锁定失败，商品库存不足";
                    break;
            }
            // 下单失败回到订单确认页重新确认订单信息
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.fsmall.com/toTrade";
        }
    }
}
