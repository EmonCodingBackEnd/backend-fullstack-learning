package com.coding.fullstack.thirdparty.component;

import com.coding.common.utils.R;
import com.coding.fullstack.thirdparty.util.HttpUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ConfigurationProperties(prefix = "alibaba.cloud")
@Data
@Component
public class SmsComponent {

    private String host;
    private String path;
    private String smsSignId;
    private String templateId;
    private String appcode;

    public R sendSmsCode(String phone, String code) {
        System.out.println("发送短信验证码：" + phone + "，验证码：" + code);

        String method = "POST";
        Map<String, String> headers = new HashMap<String, String>();
        // 最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("param", String.format("**code**:%s,**minute**:5", code));

        // smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。参考文档：http://help.guoyangyun.com/Problem/Qm.html

        querys.put("smsSignId", smsSignId);
        querys.put("templateId", templateId);
        Map<String, String> bodys = new HashMap<>();

        try {
            /**
             * 重要提示如下: HttpUtils请从\r\n\t \t*
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java\r\n\t
             * \t* 下载
             *
             * 相应的依赖请参照 https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            // 获取response的body
            // System.out.println(EntityUtils.toString(response.getEntity()));
            return R.ok();
        } catch (Exception e) {
            log.error("发送短信验证码失败", e);
            return R.error(e.getMessage());
        }
    }
}
