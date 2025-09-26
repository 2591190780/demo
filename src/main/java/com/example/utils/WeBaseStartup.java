package com.example.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
public class WeBaseStartup {


    private final WeBaseUtils weBaseUtils;


    @Autowired
    public WeBaseStartup(WeBaseUtils weBaseUtils) {
        this.weBaseUtils = weBaseUtils;
    }


    // 应用完全就绪后异步加载合约缓存，避免阻塞主线程
    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            weBaseUtils.loadContractListToCache();
            weBaseUtils.getLogger().info("异步加载合约完成，缓存大小 = {}", weBaseUtils.getAllSimplifiedContracts().size());
        } catch (Exception e) {
            weBaseUtils.getLogger().error("异步加载合约失败", e);
        }
    }
}