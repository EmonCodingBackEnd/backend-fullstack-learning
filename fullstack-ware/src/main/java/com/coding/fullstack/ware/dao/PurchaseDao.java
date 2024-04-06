package com.coding.fullstack.ware.dao;

import com.coding.fullstack.ware.entity.PurchaseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购信息
 * 
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 18:06:44
 */
@Mapper
public interface PurchaseDao extends BaseMapper<PurchaseEntity> {
	
}
