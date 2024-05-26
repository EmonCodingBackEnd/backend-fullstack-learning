package com.coding.fullstack.order.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coding.fullstack.order.entity.OrderEntity;

/**
 * 订单
 * 
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 17:57:43
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    int updateOrderStatus(@Param("outTradeNo") String outTradeNo, @Param("code") Integer code);
}
