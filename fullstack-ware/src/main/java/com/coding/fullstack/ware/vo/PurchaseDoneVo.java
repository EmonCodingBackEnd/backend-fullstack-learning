package com.coding.fullstack.ware.vo;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PurchaseDoneVo {

    // 采购单id
    @NotNull
    private Long id;

    // 采购项id
    private List<PurchaseItemDoneVo> items;
}
