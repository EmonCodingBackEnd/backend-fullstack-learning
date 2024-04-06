package com.coding.fullstack.ware.vo;

import java.util.List;

import lombok.Data;

@Data
public class MergeVo {
    
    // 采购单id
    private Long purchaseId;

    // 采购项id
    private List<Long> items;
}
