package com.coding.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class WareConstant {

    @Getter
    @RequiredArgsConstructor
    public enum PurchaseStatusEnum {
        CREATED(0, "新建"), //
        ASSIGNED(1, "已分配"), //
        RECEIVED(2, "已领取"), //
        FINISHED(3, "已完成"), //
        HASERROR(4, "有异常"), //
        ;

        private final int code;
        private final String msg;
    }

    @Getter
    @RequiredArgsConstructor
    public enum PurchaseDetailStatusEnum {
        CREATED(0, "新建"), //
        ASSIGNED(1, "已分配"), //
        BUYING(2, "正在采购"), //
        FINISHED(3, "已完成"), //
        HASERROR(4, "失败"), //
        ;

        private final int code;
        private final String msg;
    }
}
