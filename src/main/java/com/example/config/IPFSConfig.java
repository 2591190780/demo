package com.example.config;

import io.ipfs.api.IPFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
public class IPFSConfig {

    private static final Logger log = LoggerFactory.getLogger(IPFSConfig.class);

    @Value("${ipfs.host:/ip4/127.0.0.1/tcp/5001}") // 设置默认值
    private String host;

    @Bean
    public IPFS ipfs() {
        try {
            log.info("正在尝试连接IPFS节点，地址: {}", host);

            // 创建IPFS实例
            IPFS ipfsClient = new IPFS(host);

            // 设置合理的超时时间，避免长时间阻塞
            ipfsClient.timeout(10000); // 10秒超时

            // 测试连接是否正常 - 这是关键改进
            testIPFSConnection(ipfsClient);

            log.info("✅ IPFS节点连接成功");
            return ipfsClient;

        } catch (Exception e) {
            log.error("❌ 无法连接IPFS节点: {}", e.getMessage());
            log.debug("连接异常详情:", e); // 调试级别日志记录详细异常
            return null;
        }
    }

    /**
     * 测试IPFS连接状态 - 替代原来的进程检查
     */
    private void testIPFSConnection(IPFS ipfsClient) throws IOException {
        try {
            // 方法1: 尝试获取版本信息（最轻量的API调用）
            String version = ipfsClient.version();
            log.debug("IPFS节点版本: {}", version);

        } catch (IOException e) {
            // 如果版本获取失败，说明连接有问题
            throw new IOException("IPFS节点未就绪或连接失败。请确保已运行 'ipfs daemon' 命令启动守护进程", e);
        }
    }
}