package com.example.utils;

import com.example.entity.dto.NFTInfoDto;
import com.example.entity.dto.NFTRuleDto;
import com.example.entity.dto.SensorDataInfoDto;
import com.example.entity.dto.TransactionAccountDto;
import com.example.service.NFT.NFTInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class BlockchainHashUtil {

    @Resource
    NFTInfoService nftInfoService;

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

    public  String  generateTransactionHash(TransactionAccountDto dto){
        // 1. 标准化数据格式
        String formattedData = String.format(
                "ORDER_ID:"+normalizeString(dto.getOrderId())
                +"BUY_ID:"+dto.getBuyerId()
                + "PRODUCT_ID:"+dto.getProductId()
                + "SELLER_ID:"+dto.getSellerId()
                +"QUANTITY:"+dto.getQuantity()
                +"TOTAL_PRICE:"+dto.getTotalPrice()
                //+"ACTUAL_PAYMENT:"+dto.getActual_payment()
                +"ORDER_TIME:"+formatTimestamp(dto.getOrderTime())
        );
        String hash = sha256(formattedData);
        return "0x" + hash;
    }

    public  String  generateNFTRuleHash(NFTRuleDto dto){
        // 1. 标准化数据格式
        NFTInfoDto nftInfoDto = nftInfoService.NFTInfoSelectByTemplateId(dto.getTemplateId());
        String formattedData = String.format(
                "NAME:"+normalizeString(dto.getName())
                        +"TEMPLATE_ID:"+dto.getTemplateId()
                        + "TABLE:"+dto.getTableName()
                        + "VALIDITY_PERIOD:"+dto.getValidityPeriod()
                        +"NFT_NAME:"+normalizeString(nftInfoDto.getName())
                        +"ISSUANCE_LIMIT:"+nftInfoDto.getIssuanceLimit()
                        +"NFT_LEVEL:"+normalizeString(nftInfoDto.getNftLevel())
                        +"PUBLIC_BY:"+nftInfoDto.getPublicBy()
        );
        String hash = sha256(formattedData);
        return "0x" + hash;
    }



    public String generateSensorHash(
            SensorDataInfoDto dto
    ){
        String formattedData = String.format(
                "SENSOR_ID:"+ dto.getSensorId()
                        +"VALUE"+dto.getValue()
                        +"CREATE_TIME"+formatTimestamp(dto.getCreate_time())
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