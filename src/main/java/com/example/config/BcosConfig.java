package com.example.config;

import lombok.Data;

import org.fisco.bcos.sdk.v3.config.model.AmopTopic;

import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "fisco")
@PropertySource(value = "classpath:fisco-config.properties", ignoreResourceNotFound = true)
public class BcosConfig {
    private Map<String, Object> cryptoMaterial;
    private Map<String, List<String>> network;
    private List<AmopTopic> amop;
    private Map<String, Object> account;
    private Map<String, Object> threadPool;

    private WebaseConfig webase;

    @Data
    public static class WebaseConfig {
        private String url;
        private int groupId;
    }
    @Bean
    public CryptoSuite createSMCryptoSuite()
    {
        return new CryptoSuite(CryptoType.SM_TYPE);
    }

}