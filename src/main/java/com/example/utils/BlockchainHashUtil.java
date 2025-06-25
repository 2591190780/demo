package com.example.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class BlockchainHashUtil {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    /**
     * 生成产品存证哈希
     *
     * @param farmerId 农户ID
     * @param name 产品名称
     * @param category 产品类别
     * @param originLocation 原产地
     * @param createTime 创建时间
     * @return 66字符的十六进制哈希值 (0x开头)
     */
    public  String generateProductHash(
            Integer farmerId,
            String name,
            String category,
            String originLocation,
            LocalDateTime createTime
    ) {
        // 1. 标准化数据格式
        String formattedData = String.format(
                "FARMER_ID:%d|NAME:%s|CATEGORY:%s|ORIGIN:%s|TIMESTAMP:%s",
                farmerId,
                normalizeString(name),
                normalizeString(category),
                normalizeString(originLocation),
                formatTimestamp(createTime)
        );

        // 2. 计算SHA-256哈希
        String hash = sha256(formattedData);

        // 3. 添加0x前缀（总长度66字符）
        return "0x" + hash;
    }

    private static String normalizeString(String input) {
        return input == null ? "" : input.trim().toUpperCase();
    }

    private static String formatTimestamp(LocalDateTime time) {
        return time.format(TIMESTAMP_FORMATTER);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}