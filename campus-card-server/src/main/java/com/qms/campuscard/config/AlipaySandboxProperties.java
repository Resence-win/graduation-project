package com.qms.campuscard.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alipay.sandbox")
public class AlipaySandboxProperties {

    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String gatewayUrl;
    private String returnUrl;
    private String frontendReturnUrl;
    private String charset = "UTF-8";
    private String signType = "RSA2";

    public boolean isConfigured() {
        return hasText(appId) && hasText(privateKey) && hasText(alipayPublicKey);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
