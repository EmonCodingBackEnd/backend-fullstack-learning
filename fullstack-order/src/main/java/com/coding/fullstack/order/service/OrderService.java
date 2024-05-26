package com.coding.fullstack.order.service;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coding.common.utils.PageUtils;
import com.coding.fullstack.order.entity.OrderEntity;
import com.coding.fullstack.order.vo.*;

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

    /**
     * 获取当前订单的支付信息
     * 
     * @param orderSn
     * @return
     */
    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayNotifyResult(PayNotifyAsyncVo notifyVo);
}
