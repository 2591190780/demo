package com.example.utils;

import jakarta.annotation.Resource;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;

@Component
public class WalletBlockChainUtil {

    @Resource
    private CryptoSuite cryptoSuite;

    @Value("${fisco.webase.url}")
    private String webaseFrontUrl;

    @Resource
    private RestTemplate restTemplate;

    public Map<String, String> generateUserWallet() {
        CryptoKeyPair keyPair = cryptoSuite.getCryptoKeyPair();
        String address = keyPair.getAddress();
        String privateKey = keyPair.getHexPrivateKey();
        String publicKey = keyPair.getHexPublicKey();

        // 提取公钥的X和Y部分
        String publicKeyX = "";
        String publicKeyY = "";
        if (publicKey.length() >= 132) {
            // 公钥格式为04 + X(64字符) + Y(64字符)
            publicKeyX = publicKey.substring(4, 68);   // 公钥X部分
            publicKeyY = publicKey.substring(68, 132); // 公钥Y部分
        }

        return Map.of(
                "address", address,
                "privateKey", privateKey,
                "publicKeyX", publicKeyX,
                "publicKeyY", publicKeyY
        );
    }

    /**
     * 导入私钥到WeBASE-Front（使用GET请求）
     *
     * @param userName 用户名
     * @param privateKey 私钥(十六进制格式)
     * @return 导入结果
     */
    public Map<String, String> importPrivateKey(String userName, String privateKey) {
        // 构建请求URL（使用GET请求）
        String importUrl = webaseFrontUrl + "/privateKey/import";

        // 使用UriComponentsBuilder构建带参数的URL
        String url = UriComponentsBuilder.fromHttpUrl(importUrl)
                .queryParam("privateKey", privateKey)
                .queryParam("userName", userName)
                .toUriString();

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // 创建请求实体（GET请求不需要body）
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        try {
            // 使用类型安全的响应处理
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            // 检查响应状态
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // 增强的响应处理逻辑：兼容多种响应格式
                if (isSuccessResponse(responseBody)) {
                    // 处理成功响应
                    return extractImportResult(responseBody);
                } else {
                    // 处理错误响应
                    throw createErrorException(responseBody);
                }
            } else {
                throw new WebaseIntegrationException(
                        "WeBASE-Front 响应状态异常: " + response.getStatusCode(),
                        response.getBody()
                );
            }
        } catch (Exception e) {
            if (e instanceof WebaseIntegrationException) {
                throw (WebaseIntegrationException) e;
            }
            throw new WebaseIntegrationException("导入私钥时发生异常: " + e.getMessage(), e);
        }
    }

    /**
     * 判断响应是否成功
     */
    private boolean isSuccessResponse(Map<String, Object> responseBody) {
        // 情况1：包含 code 字段且为0
        if (responseBody.containsKey("code") &&
                (Integer) responseBody.get("code") == 0) {
            return true;
        }

        // 情况2：没有 code 字段但包含 address 字段（您的响应格式）
        if (responseBody.containsKey("address")) {
            return true;
        }

        // 情况3：包含 success 字段且为 true
        if (responseBody.containsKey("success") &&
                Boolean.TRUE.equals(responseBody.get("success"))) {
            return true;
        }

        return false;
    }

    /**
     * 从响应中提取导入结果
     */
    private Map<String, String> extractImportResult(Map<String, Object> responseBody) {
        // 确定数据来源：data 字段或整个响应体
        Object dataObj = responseBody.get("data");
        if (dataObj == null) {
            dataObj = responseBody;
        }

        // 处理不同的响应格式
        if (dataObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) dataObj;

            return Map.of(
                    "address", getStringValue(data, "address", ""),
                    "signUserId", getStringValue(data, "signUserId", ""),
                    "publicKey", getStringValue(data, "publicKey", ""),
                    "userName", getStringValue(data, "userName", "")
            );
        } else {
            throw new WebaseIntegrationException(
                    "WeBASE-Front 返回的数据格式无法解析: " + responseBody,
                    responseBody
            );
        }
    }

    /**
     * 安全获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return value.toString();
        }
        return defaultValue;
    }

    /**
     * 创建错误异常
     */
    private WebaseIntegrationException createErrorException(Map<String, Object> responseBody) {
        // 尝试获取错误代码
        String errorCode = "未知";
        if (responseBody.containsKey("code")) {
            errorCode = String.valueOf(responseBody.get("code"));
        } else if (responseBody.containsKey("errorCode")) {
            errorCode = String.valueOf(responseBody.get("errorCode"));
        }

        // 尝试获取错误消息
        String errorMsg = "未知错误";
        if (responseBody.containsKey("errorMessage")) {
            errorMsg = (String) responseBody.get("errorMessage");
        } else if (responseBody.containsKey("message")) {
            errorMsg = (String) responseBody.get("message");
        } else if (responseBody.containsKey("error")) {
            errorMsg = (String) responseBody.get("error");
        }

        return new WebaseIntegrationException(
                "WeBASE-Front 返回错误 [code=" + errorCode + "]: " + errorMsg,
                responseBody
        );
    }

    /**
     * 生成钱包并自动导入到WeBASE-Front
     *
     * @param userName 用户名
     * @return 完整的钱包信息
     */
    public Map<String, String> generateAndImportWallet(String userName) {
        // 1. 生成钱包
        Map<String, String> wallet = generateUserWallet();

        // 2. 导入私钥到WeBASE-Front
        Map<String, String> importResult = importPrivateKey(userName, wallet.get("privateKey"));

        // 3. 合并结果
        return Map.of(
                "userName", userName,
                "address", wallet.get("address"),
                "privateKey", wallet.get("privateKey"),
                "publicKeyX", wallet.get("publicKeyX"),
                "publicKeyY", wallet.get("publicKeyY"),
                "webaseAddress", importResult.get("address"),
                "signUserId", importResult.get("signUserId")
        );
    }

    // 增强的自定义异常类
    public static class WebaseIntegrationException extends RuntimeException {
        private final Map<String, Object> responseBody;

        public WebaseIntegrationException(String message) {
            super(message);
            this.responseBody = null;
        }

        public WebaseIntegrationException(String message, Map<String, Object> responseBody) {
            super(message);
            this.responseBody = responseBody;
        }

        public WebaseIntegrationException(String message, Throwable cause) {
            super(message, cause);
            this.responseBody = null;
        }

        public Map<String, Object> getResponseBody() {
            return responseBody;
        }

        @Override
        public String getMessage() {
            if (responseBody != null) {
                return super.getMessage() + " | Response: " + responseBody;
            }
            return super.getMessage();
        }
    }
}