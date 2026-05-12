package com.qms.campuscard.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlipayClientConfig {

    @Bean
    public AlipayClient alipayClient(AlipaySandboxProperties properties) throws AlipayApiException {
        AlipayConfig config = new AlipayConfig();
        config.setServerUrl(properties.getGatewayUrl());
        config.setAppId(properties.getAppId());
        config.setPrivateKey(properties.getPrivateKey());
        config.setFormat("json");
        config.setCharset(properties.getCharset());
        config.setAlipayPublicKey(properties.getAlipayPublicKey());
        config.setSignType(properties.getSignType());
        config.setConnectTimeout(10000);
        config.setReadTimeout(30000);
        return new DefaultAlipayClient(config);
    }
}
