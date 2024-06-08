package com.coding.fullstack.product.web;

import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.coding.fullstack.product.service.SkuInfoService;
import com.coding.fullstack.product.vo.SkuItemVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final SkuInfoService skuInfoService;

    /**
     * 展示当前sku的详情
     * 
     * @return
     */
    @GetMapping({"/{skuId}.html"})
    public String skuItem(@PathVariable("skuId") Long skuId, Model model)
        throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item", skuItemVo);
        return "item";
    }
}
