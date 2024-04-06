package com.coding.fullstack.product.dao;

import com.coding.fullstack.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-01 12:50:47
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
