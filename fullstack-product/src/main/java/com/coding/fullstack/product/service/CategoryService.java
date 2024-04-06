package com.coding.fullstack.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.product.entity.CategoryEntity;
import com.coding.fullstack.product.vo.Catelog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-01 12:50:47
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    /**
     * 根据catelogId找到其完整路径 [父/子/孙]
     * 
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    /**
     * 级联更新所有关联的数据
     * 
     * @param category
     */
    boolean updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevel1Categorys();

    /**
     * 缓存里面的数据如何和数据库保持一致 1)、双写模式 2)、失效模式
     * 
     * @return
     */
    Map<String, List<Catelog2Vo>> getCatalogJson();
}
