package com.coding.common.to.mq;

import lombok.Data;

@Data
public class StockLockedTo {

    private Long id; // 库存工作单的id
    private StockLockedDetailTo detail; // 工作详情的所有id
}
