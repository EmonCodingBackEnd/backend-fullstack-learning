package com.coding.fullstack.thirdparty.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cloud.spring.boot.context.env.AliCloudProperties;
import com.alibaba.cloud.spring.boot.oss.env.OssProperties;
import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.coding.common.utils.R;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OssController {

    private final OSS ossClient;
    private final AliCloudProperties aliCloudProperties;
    private final OssProperties ossProperties;

    @Value("${alibaba.cloud.oss.bucket}")
    private String bucketName;

    // 参考自Web端直传实践：https://help.aliyun.com/zh/oss/use-cases/obtain-signature-information-from-the-server-and-upload-data-to-oss?spm=a2c4g.11186623.0.0.15945d03WOfZF7
    @RequestMapping("/thirdparty/oss/policy")
    public R policy() {
        // 填写Host名称，格式为https://bucketname.endpoint。
        String host = String.format("https://%s.%s", bucketName, ossProperties.getEndpoint());
        // 设置上传回调URL，即回调服务器地址，用于处理应用服务器与OSS之间的通信。OSS会在文件上传完成后，把文件上传信息通过此回调URL发送给应用服务器。
        String callbackUrl = "https://192.168.0.0:8888";
        // 设置上传到OSS文件的前缀，可置空此项。置空后，文件将上传至Bucket的根目录下。
        String dir = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);

        try {
            ZonedDateTime expiration = LocalDateTime.now().plusSeconds(30).atZone(ZoneId.systemDefault());
            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(Date.from(expiration.toInstant()), policyConds);
            byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
            String accessId = aliCloudProperties.getAccessKey();
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<>();
            respMap.put("accessId", accessId); // 用户请求的AccessKey ID。
            respMap.put("policy", encodedPolicy);// 用户表单上传的策略（Policy），Policy为经过Base64编码过的字符串。详情请参见Post Policy。
            respMap.put("signature", postSignature);
            respMap.put("dir", dir); // 限制上传的文件前缀。
            respMap.put("host", host);// 用户发送上传请求的域名。host不支持自定义域名。
            respMap.put("expire", String.valueOf(expiration.toEpochSecond())); // 由服务器端指定的Policy过期时间，格式为Unix时间戳（自UTC时间1970年01月01号开始的秒数）。
            return R.ok().put("data", respMap);
        } catch (Exception e) {
            log.error("OSS签名失败", e);
            return R.error(e.getMessage());
        }
    }
}
