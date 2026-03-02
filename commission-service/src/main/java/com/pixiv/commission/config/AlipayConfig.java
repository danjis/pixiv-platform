package com.pixiv.commission.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝沙箱支付配置
 *
 * 沙箱环境文档: https://opendocs.alipay.com/common/02kkv7
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    /** 应用 AppID（沙箱） */
    private String appId;

    /** 应用私钥（RSA2） */
    private String appPrivateKey;

    /** 支付宝公钥 */
    private String alipayPublicKey;

    /** 支付宝网关（沙箱） */
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    /** 签名方式 */
    private String signType = "RSA2";

    /** 编码 */
    private String charset = "UTF-8";

    /** 数据格式 */
    private String format = "json";

    /** 异步通知地址（支付宝回调我们的服务器） */
    private String notifyUrl;

    /** 同步跳转地址（支付完成后跳回前端） */
    private String returnUrl;

    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                gatewayUrl,
                appId,
                appPrivateKey,
                format,
                charset,
                alipayPublicKey,
                signType);
    }
}
