package com.coding.fullstack.product.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 二级分类Vo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catelog2Vo {
    private String catalog1Id; // 1级父分类id
    private List<Catelog3Vo> catalog3List; // 三级子分类列表
    private String id;
    private String name;

    /**
     * 三级分类Vo
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Catelog3Vo {
        private String catalog2Id; // 父分类，2级分类id
        private String id;
        private String name;
    }
}
