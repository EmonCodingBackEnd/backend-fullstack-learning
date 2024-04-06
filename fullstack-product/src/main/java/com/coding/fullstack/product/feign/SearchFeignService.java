package com.coding.fullstack.product.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.coding.common.to.es.SkuEsModel;
import com.coding.common.utils.R;

@FeignClient("fullstack-search")
public interface SearchFeignService {
    /**
     * 上架商品
     *
     * @param skuEsModels
     */
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
