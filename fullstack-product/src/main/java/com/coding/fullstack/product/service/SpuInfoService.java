package com.coding.fullstack.product.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.product.entity.SpuInfoEntity;
import com.coding.fullstack.product.vo.spu.SpuSaveVo;

/**
 * spu信息
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-01 12:50:47
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo spuSaveVo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    boolean spuUp(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}
