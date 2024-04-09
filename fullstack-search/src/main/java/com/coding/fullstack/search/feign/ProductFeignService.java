package com.coding.fullstack.search.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.coding.common.utils.R;

@FeignClient("fullstack-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    R info(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    // @RequiresPermissions("product:brand:info")
    R infos(@RequestParam("brandIds") List<Long> brandIds);
}
