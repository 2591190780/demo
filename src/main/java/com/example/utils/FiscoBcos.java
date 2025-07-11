package com.example.utils;


import com.example.config.BcosConfig;
import lombok.Data;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.v3.BcosSDK;

import org.fisco.bcos.sdk.v3.config.ConfigOption;
import org.fisco.bcos.sdk.v3.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.v3.config.model.ConfigProperty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;


import java.util.Map;

@Slf4j
@Data
@Component
public class FiscoBcos {

    @Autowired
    BcosConfig bcosConfig;

    private BcosSDK bcosSDK;

    public void init() throws ConfigException {
        ConfigProperty configProperty = loadProperty();
        ConfigOption configOption = new ConfigOption(configProperty); // 修正这里
        bcosSDK = new BcosSDK(configOption);
        log.info("FISCO BCOS SDK初始化成功");
    }

    public ConfigProperty loadProperty() {
        ConfigProperty configProperty = new ConfigProperty();
        configProperty.setCryptoMaterial(bcosConfig.getCryptoMaterial());
        configProperty.setAccount(bcosConfig.getAccount());

        // 修正网络配置
        Map<String, Object> networkConfig = new HashMap<>();
        networkConfig.put("peers", bcosConfig.getNetwork().get("peers"));
        configProperty.setNetwork(networkConfig);

        configProperty.setAmop(bcosConfig.getAmop());
        configProperty.setThreadPool(bcosConfig.getThreadPool());
        return configProperty;
    }
}
