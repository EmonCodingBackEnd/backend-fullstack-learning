package com.coding.fullstack.order.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 订单确认页需要用的数据
 */
@Data
public class OrderConfirmVo {
    // 收货地址， ums_member_receive_address
    List<MemberAddressVo> address;
    // 所有选中的购物项
    List<OrderItemVo> items;
    // 发票记录...
    // 优惠券信息...
    // 积分信息...
    private Integer integration;

    // 商品的库存信息
    Map<Long, Boolean> stocks;

    // 防止重复的令牌
    String orderToken;

    public Integer getCount() {
        return Math.toIntExact(items.stream().map(OrderItemVo::getCount).count());
    }

    // 订单总额
    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                sum = sum.add(item.getPrice().multiply(new BigDecimal(item.getCount().toString())));
            }
        }
        return sum;
    }

    // 应付总额
    public BigDecimal getPayPrice() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                sum = sum.add(item.getPrice().multiply(new BigDecimal(item.getCount().toString())));
            }
        }
        return sum;
    }
}
