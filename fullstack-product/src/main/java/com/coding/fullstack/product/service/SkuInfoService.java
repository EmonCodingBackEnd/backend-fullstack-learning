package com.coding.fullstack.product.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.product.entity.SkuInfoEntity;
import com.coding.fullstack.product.vo.SkuItemVo;

/**
 * sku信息
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-01 12:50:47
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfos(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;
}

