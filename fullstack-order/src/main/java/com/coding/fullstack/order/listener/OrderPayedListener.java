package com.coding.fullstack.order.listener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.coding.fullstack.order.config.AlipayConfig;
import com.coding.fullstack.order.service.OrderService;
import com.coding.fullstack.order.vo.PayNotifyAsyncVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderPayedListener {

    private final AlipayConfig alipayConfig;

    private final OrderService orderService;

    /**
     * 支付宝成功异步通知
     * 
     * @param request
     * @return
     */
    @PostMapping("/payed/notify")
    public String handleAlipayed(HttpServletRequest request, PayNotifyAsyncVo notifyVo) throws AlipayApiException {
        log.info("收到支付宝回调通知");
        // 只要我们收到了支付宝给我们异步的通知，告诉我们订单支付成功，返回success，支付宝就再也不通知
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String key : requestParams.keySet()) {
            String parameter = request.getParameter(key);
            log.info("参数名：{}==>参数值：{}", key, parameter);
        }

        // 获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<>();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        // 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
        // 商户订单号

        String out_trade_no = new String(request.getParameter("out_trade_no").getBytes(StandardCharsets.ISO_8859_1),
            StandardCharsets.UTF_8);
        // 支付宝交易号

        String trade_no =
            new String(request.getParameter("trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        // 交易状态
        String trade_status = new String(request.getParameter("trade_status").getBytes(StandardCharsets.ISO_8859_1),
            StandardCharsets.UTF_8);

        // 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
        // 计算得出通知验证结果
        // boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String
        // sign_type)
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(),
            alipayConfig.getCharset(), alipayConfig.getSigntype());

        String result = "";
        if (signVerified) {// 验证成功
            //////////////////////////////////////////////////////////////////////////////////////////
            // 请在这里加上商户的业务逻辑程序代码

            // ——请根据您的业务逻辑来编写程序（以下代码仅作参考）——

            /*if (trade_status.equals("TRADE_FINISHED")) {
                // 判断该笔订单是否在商户网站中已经做过处理
                // 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                // 请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                // 如果有做过处理，不执行商户的业务程序
            
                // 注意：
                // 如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                // 如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
            } else if (trade_status.equals("TRADE_SUCCESS")) {
                // 判断该笔订单是否在商户网站中已经做过处理
                // 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                // 请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                // 如果有做过处理，不执行商户的业务程序
            
                // 注意：
                // 如果签约的是可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
            }*/

            log.info("签名验证成功......");
            result = orderService.handlePayNotifyResult(notifyVo);
            //////////////////////////////////////////////////////////////////////////////////////////
        } else {// 验证失败
            log.error("签名验证失败......");
            result = "fail";
        }
        return result;
    }
}
