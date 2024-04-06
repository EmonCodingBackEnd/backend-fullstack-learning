package com.coding.fullstack.ware.vo;

import lombok.Data;

@Data
public class PurchaseItemDoneVo {

    // 采购项id
    private Long itemId;
    // 采购项状态
    private Integer status;
    // 采购项失败原因
    private String reason;
}
