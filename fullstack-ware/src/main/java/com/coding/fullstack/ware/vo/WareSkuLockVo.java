package com.coding.fullstack.ware.vo;

import java.util.List;

import lombok.Data;

@Data
public class WareSkuLockVo {
    private String orderSn; // 订单号
    private List<OrderItemVo> locks; // 需要锁住的所有库存信息
}
