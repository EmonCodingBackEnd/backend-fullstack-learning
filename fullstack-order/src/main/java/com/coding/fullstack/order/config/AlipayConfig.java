package com.coding.fullstack.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.coding.fullstack.order.vo.PayVo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayConfig {
    // 商户appid
    public String appId = "";
    public String rsaPublicKey = "";
    // 私钥 pkcs8格式的
    public String rsaPrivateKey = "";
    // 服务器异步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public String notifyUrl = "";
    // 页面跳转同步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问 商户可以自定义同步跳转地址
    public String returnUrl = "";
    // 请求网关地址
    public String url = "";
    // 编码
    public String charset = "UTF-8";
    // 返回格式
    public String format = "json";
    // 支付宝公钥
    public String alipayPublicKey = "";
    // 日志记录目录
    public String logPath = "/log";
    // RSA2
    public String signtype = "RSA2";
    public String timeout = "";

    public String pay(PayVo vo) throws AlipayApiException {
        // SDK 公共请求类，包含公共请求参数，以及封装了签名与验签，开发者无需关注签名与验签
        // 调用RSA签名方式
        // 1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient =
            new DefaultAlipayClient(url, appId, rsaPrivateKey, format, charset, alipayPublicKey, signtype);

        // 2、创建一个支付请求
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        // 设置异步通知地址
        alipayRequest.setNotifyUrl(notifyUrl);
        // 设置同步地址
        alipayRequest.setReturnUrl(returnUrl);

        // 封装请求支付信息
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(vo.getOut_trade_no());
        model.setSubject(vo.getSubject());
        model.setTotalAmount(vo.getTotal_amount());
        model.setBody(vo.getBody());
        // 超时时间 可空
        model.setTimeoutExpress("1m");
        // 销售产品码 必填
        model.setProductCode("QUICK_WAP_WAY");
        alipayRequest.setBizModel(model);

        String result = alipayClient.pageExecute(alipayRequest).getBody();
        log.info("支付宝的响应：{}", result);
        return result;
    }
}
