package com.coding.fullstack.order.dto;

import java.math.BigDecimal;
import java.util.List;

import com.coding.fullstack.order.entity.OrderEntity;
import com.coding.fullstack.order.entity.OrderItemEntity;

import lombok.Data;

@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice; // 订单应付价格
}
