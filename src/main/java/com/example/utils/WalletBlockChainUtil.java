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

        //后续需要由前端生成私钥，返回给后端地址
        CryptoKeyPair keyPair = cryptoSuite.getCryptoKeyPair();
        String address = keyPair.getAddress();
        String privateKey = keyPair.getHexPrivateKey();
        return Map.of("address",address,"privateKey",privateKey);

        //这里直接执行用户信息区块链上注册操作

    }

    private  String getAddress(String publicKey) {

        return null;
    }


}
