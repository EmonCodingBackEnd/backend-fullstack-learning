package com.coding.fullstack.order.vo;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class OrderItemVo {
    private Long skuId;
    private Boolean check = true; // 是否被选中
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;

    public BigDecimal getTotalPrice() {
        return price.multiply(new BigDecimal(count));
    }
}
