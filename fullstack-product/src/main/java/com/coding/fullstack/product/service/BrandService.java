package com.coding.fullstack.product.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.product.entity.BrandEntity;

/**
 * 品牌
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-01 12:50:47
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    boolean updateDetail(BrandEntity brand);

    List<BrandEntity> getBrandsById(List<Long> brandIds);
}

