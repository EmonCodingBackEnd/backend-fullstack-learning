package com.coding.fullstack.order.dao;

import com.coding.fullstack.order.entity.RefundInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款信息
 * 
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 17:57:43
 */
@Mapper
public interface RefundInfoDao extends BaseMapper<RefundInfoEntity> {
	
}
