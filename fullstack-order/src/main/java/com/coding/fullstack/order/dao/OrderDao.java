package com.coding.fullstack.order.dao;

import com.coding.fullstack.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 17:57:43
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
