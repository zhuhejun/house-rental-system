package com.kmbeast.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    private String appId;

    private String privateKey;

    private String publicKey;

    private String gatewayUrl;

    private String notifyUrl;

    private String returnUrl;

    private String charset;

    private String signType;

    private String format;
}
