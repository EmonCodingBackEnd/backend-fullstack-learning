package com.coding.fullstack.seckill.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coding.common.utils.R;
import com.coding.fullstack.seckill.service.SeckillService;
import com.coding.fullstack.seckill.to.SecKillSkuRedisTo;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SeckillController {
    private final SeckillService seckillService;

    /**
     * 返回当前时间可以参与的秒杀商品信息
     */
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SecKillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getCurrentSeckillInfo(@PathVariable("skuId") Long skuId) {
        SecKillSkuRedisTo to = seckillService.getCurrentSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId, @RequestParam("key") String key,
        @RequestParam("num") Integer num, Model model) {
        String orderSn = seckillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
