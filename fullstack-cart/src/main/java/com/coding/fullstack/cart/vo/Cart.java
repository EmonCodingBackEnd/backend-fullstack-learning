package com.coding.fullstack.cart.vo;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * 购物车
 */
@Data
public class Cart {
    private List<CartItem> items;
    private Integer countNum; // 商品数量
    private Integer countType; // 商品类型
    private BigDecimal totalAmount; // 商品总价
    private BigDecimal reduce = new BigDecimal("0.00"); // 减免价格

    public Integer getCountNum() {
        Integer count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        Integer count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                count += 1;
            }
        }
        return count;
    }

    public BigDecimal getTotalAmount() {
        // 1、计算购物项总价
        BigDecimal amount = new BigDecimal("0");
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                if (item.getCheck()) {
                    amount = amount.add(item.getTotalPrice());
                }
            }
        }

        // 2、减去优惠总价
        amount = amount.subtract(getReduce());

        return amount;
    }
}
