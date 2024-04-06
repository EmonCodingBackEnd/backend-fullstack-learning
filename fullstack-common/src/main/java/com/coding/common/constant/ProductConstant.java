package com.coding.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class ProductConstant {

    @Getter
    @RequiredArgsConstructor
    public enum AttrEnum {
        ATTR_TYPE_SALE(0, "销售属性"), //
        ATTR_TYPE_BASE(1, "基本属性"), //
        ATTR_TYPE_BASE_AND_SALE(2, "既是销售属性又是基本属性"), //
        ;

        private final int code;
        private final String msg;
    }

    @Getter
    @RequiredArgsConstructor
    public enum StatusEnum {
        NEW_SPU(0, "下架"), //
        SPU_UP(1, "上架"), //
        ;

        private final int code;
        private final String msg;
    }

}
