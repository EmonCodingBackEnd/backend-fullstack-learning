package com.coding.fullstack.order.vo;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.ToString;

/**
 * 封装支付成功支行的一部同志结果数据
 */
@ToString
@Data
public class PayNotifyAsyncVo {

    private String gmt_create;
    private String charset;
    private String gmt_payment;
    private LocalDateTime notify_time;
    private String subject;
    private String sign;
    private String buyer_id;// 支付者的id
    private String seller_email; // 商家账户
    private String buyer_logon_id; // 买家账户
    private String body;// 订单的信息
    private String invoice_amount;// 支付金额
    private String version;
    private String notify_id;// 通知id
    private String fund_bill_list;
    private String notify_type;// 通知类型； trade_status_sync
    private String out_trade_no;// 订单号
    private String total_amount;// 支付的总额
    private String trade_status;// 交易状态 TRADE_SUCCESS
    private String trade_no;// 流水号
    private String auth_app_id;//
    private String receipt_amount;// 商家收到的款
    private String point_amount;//
    private String app_id;// 应用id
    private String buyer_pay_amount;// 最终支付的金额
    private String sign_type;// 签名类型
    private String seller_id;// 商家的id

}

/*
参数名：gmt_create==>参数值：2024-05-25 22:04:15
参数名：charset==>参数值：UTF-8
参数名：seller_email==>参数值：cyrusk6916@sandbox.com
参数名：subject==>参数值：比肩华为的 Apple 15 岩石青 官方标配 128GB
参数名：sign==>参数值：ejof5cxkjgqmbJSd8LJlrRodDQ8+81LTvwpZoUVfNO7kUoEaFz2hKot8io3qBWzpBJ7JCz0z3V6hKyYbfjdD/QZwqt5lumc8URhBxCdWfHKFTIXCS4MeEorHlMhr1qo9USF5l6j5I5z4NFUHhmhDrcDzjsWEF2n63juVCDks3+/xAgoeKpXb2Wkm2/Bl645sCh17KvP+ff0mGdLhMfSS2cdFPYusAbVHt7HWQXTz97xZCjCzAWNghPnJcRVoL1ve3L2wvUO6gqi8HRMp9W8UuMgxoODHiO85LC+lw2ICbsN8g78Pdz19isehrEW9mRynbMqwDjCjk4SJFUsHPJokvQ==
参数名：body==>参数值：外观:岩石青;版本:官方标配;内存:128GB
参数名：buyer_id==>参数值：2088722036127985
参数名：invoice_amount==>参数值：5228.00
参数名：notify_id==>参数值：2024052501222220416127980503573452
参数名：fund_bill_list==>参数值：[{"amount":"5228.00","fundChannel":"ALIPAYACCOUNT"}]
参数名：notify_type==>参数值：trade_status_sync
参数名：trade_status==>参数值：TRADE_SUCCESS
参数名：receipt_amount==>参数值：5228.00
参数名：buyer_pay_amount==>参数值：5228.00
参数名：app_id==>参数值：9021000137632904
参数名：sign_type==>参数值：RSA2
参数名：seller_id==>参数值：2088721036149511
参数名：gmt_payment==>参数值：2024-05-25 22:04:16
参数名：notify_time==>参数值：2024-05-25 22:04:17
参数名：version==>参数值：1.0
参数名：out_trade_no==>参数值：202405252204008741794368827396464641
参数名：total_amount==>参数值：5228.00
参数名：trade_no==>参数值：2024052522001427980503355816
参数名：auth_app_id==>参数值：9021000137632904
参数名：buyer_logon_id==>参数值：hwqakr3094@sandbox.com
参数名：point_amount==>参数值：0.00
 */
