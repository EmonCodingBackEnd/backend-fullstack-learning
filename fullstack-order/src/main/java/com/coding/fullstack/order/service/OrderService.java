package com.coding.fullstack.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.order.entity.OrderEntity;
import com.coding.fullstack.order.vo.OrderConfirmVo;
import com.coding.fullstack.order.vo.OrderSubmitVo;
import com.coding.fullstack.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-02 17:57:43
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 返回订单确认页需要用的数据
     * 
     * @return
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    /**
     * 下单
     * 
     * @param vo
     * @return
     */
    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity entity);
}
