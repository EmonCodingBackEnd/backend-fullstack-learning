package com.coding.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// @formatter:off
/**
 * 错误码和错误信息定义类
 * 1.错误码定义规则为5位数字
 * 2.前两位表示业务场景，最后三位表示错误码。例如：10001。10：通用 001：系统位置异常
 * 3.维护错误码后需要维护错误描述，将它们定义为枚举形式
 * 错误码列表：
 * 10：通用
 *      001：参数格式校验错误
 * 11：商品
 * 12：订单
 * 13：购物车
 * 14：物流
 */
// @formatter:on
@Getter
@RequiredArgsConstructor
public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000, "系统未知异常"), //
    VALID_EXCEPTION(10001, "参数格式校验失败"), //
    VALID_SMS_CODE_EXCEPTION(10002, "短信验证码获取频率太高，请稍后再试！"), //
    PRODUCT_UP_EXCEPTION(11001, "商品上架失败"), //
    NO_STOCK_EXCEPTION(21000, "商品库存不足")//
    ;

    private final int code;
    private final String msg;

}
