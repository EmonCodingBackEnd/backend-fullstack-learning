package com.coding.fullstack.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 封装订单提交的数据
 */
@Data
public class OrderSubmitVo {
    private Long addrId; // 收货地址的id
    private Integer payType; // 支付方式
    // 无需提交需要购买的商品，会去购物车再获取一遍
    // 优惠、发票，防重令牌
    private String orderToken;
    private BigDecimal payPrice; // 应付价格，用于价格验证
    // 用户信息信息，直接去session去除登录用户
    private String note; // 订单备注

}
