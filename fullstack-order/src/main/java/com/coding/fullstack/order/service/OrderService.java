package com.coding.fullstack.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 17:57:43
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

