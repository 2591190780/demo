package com.example.config;

import lombok.Data;

import org.fisco.bcos.sdk.v3.config.model.AmopTopic;

import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties("fisco-config.properties")
public class BcosConfig {
    private Map<String, Object> cryptoMaterial;
    private Map<String, List<String>> network;
    private List<AmopTopic> amop;
    private Map<String, Object> account;
    private Map<String, Integer> threadPool;

    @Bean
    public CryptoSuite createSMCryptoSuite()
    {
        return new CryptoSuite(CryptoType.SM_TYPE);
    }
}