package com.example.utils;


import jakarta.annotation.Resource;

import org.fisco.bcos.sdk.v3.config.Config;
import org.fisco.bcos.sdk.v3.config.ConfigOption;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class WalletBlockChainUtil {

    @Resource
    CryptoSuite cryptoSuite;

    public Map<String,String> generateUserWallet() {

        CryptoKeyPair keyPair = cryptoSuite.getCryptoKeyPair();
        String address = keyPair.getAddress();
        String privateKey = keyPair.getHexPrivateKey();
        return Map.of("address",address,"privateKey",privateKey);

    }

    private  String getAddress(String publicKey) {

        return null;
    }


}
