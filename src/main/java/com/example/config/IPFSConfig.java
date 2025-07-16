package com.example.config;

import io.ipfs.api.IPFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IPFSConfig {

    private static final Logger log = LoggerFactory.getLogger(IPFSConfig.class);
    @Value("${ipfs.host}")
    private String host;

    @Bean
    public IPFS ipfs() {
        // Windows 环境下需要额外处理
        try {
            // 确保 IPFS Desktop 正在运行
            Process process = Runtime.getRuntime().exec("tasklist /FI \"IMAGENAME eq IPFS Desktop.exe\"");
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.warn("IPFS Desktop 未运行！请启动 IPFS Desktop");
            }

            return new IPFS(host);
        } catch (Exception e) {
            log.warn("无法连接 IPFS 节点: {}", e.getMessage(), e);
            return null;
        }
    }
}