package com.coding.fullstack.order.vo;

import java.util.List;

import lombok.Data;

@Data
public class WareSkuLockVo {
    private String orderSn; // 订单号
    private List<OrderItemVo> locks; // 需要锁住的素有库存信息
}
