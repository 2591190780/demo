package com.example.utils;

import com.example.config.BcosConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WeBaseUtils {

    private final BcosConfig bcosConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private String baseUrl;
    private Integer groupId;

    // 合约地址到ABI的缓存
    private final Map<String, String> contractAbiCache = new ConcurrentHashMap<>();
    // 合约地址到简化合约信息的缓存
    private final Map<String, SimplifiedContractInfo> simplifiedContractCache = new ConcurrentHashMap<>();

    @Autowired
    public WeBaseUtils(BcosConfig bcosConfig, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.bcosConfig = bcosConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    // 在类中添加以下方法
    public Map<String, Object> callContractMethod(
            String userAddress,
            String contractAddress,
            String methodName,
            List<Object> params
    ) throws Exception {
        if (!validateBaseUrl()) {
            throw new RuntimeException("WeBase服务未配置");
        }
        String apiUrl = baseUrl + "/trans/handle";
        log.info("调用合约方法: {}.{} at {}", contractAddress, methodName, apiUrl);

        // 1. 获取合约ABI
        String abi = getContractAbi(contractAddress);
        if (abi == null || abi.isEmpty()) {
            throw new RuntimeException("获取合约ABI失败: " + contractAddress);
        }
        // 2. 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("groupId", groupId);
        requestBody.put("user", userAddress);
        requestBody.put("contractAddress", contractAddress);
        requestBody.put("funcName", methodName);

        // 直接使用参数列表，不要序列化为字符串
        requestBody.put("funcParam", params);

        // 将ABI字符串转换为JSON数组
        JsonNode abiNode = objectMapper.readTree(abi);
        requestBody.put("contractAbi", abiNode);
        requestBody.put("useCns", false);


        // 3. 发送请求到WeBase-Front
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // 4. 处理响应 - 适配实际返回的交易回执格式
        if (response.getStatusCode() != HttpStatus.OK) {
            String responseBody = response.getBody() != null ? response.getBody() : "无响应体";
            log.error("WeBase请求失败: 状态码={}, 响应体={}", response.getStatusCode(), responseBody);
            throw new RuntimeException("WeBase请求失败: " + response.getStatusCode());
        }
        String responseBody = response.getBody();
        // 后续省略
        if (responseBody == null) {
            throw new RuntimeException("WeBase响应体为空");
        }
        log.debug("WeBase完整响应: {}", responseBody);
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody.trim());
            Map<String, Object> result;
            boolean topArray = rootNode.isArray();

            if (topArray) {
                List<Object> dataList = objectMapper.convertValue(
                        rootNode,
                        new TypeReference<List<Object>>() {}
                );
                result = new HashMap<>();
                result.put("success", true);
                result.put("data", dataList);
                return result;
            }

            result = objectMapper.convertValue(
                    rootNode,
                    new TypeReference<Map<String, Object>>() {}
            );

            Object dataObj = result.get("data");
            if (dataObj instanceof String) {
                String dataText = (String) dataObj;
                JsonNode dataNode = objectMapper.readTree(dataText);
                if (dataNode.isArray()) {
                    List<Object> unwrapped = objectMapper.convertValue(
                            dataNode,
                            new TypeReference<List<Object>>() {}
                    );
                    result.put("data", unwrapped);
                }
            }

            // 状态判断：成功或失败标记
            if (result.containsKey("statusOK") && Boolean.TRUE.equals(result.get("statusOK"))) {
                result.put("success", true);
                return result;
            } else if (result.containsKey("status") && "0x0".equals(result.get("status"))) {
                result.put("success", true);
                return result;
            } else {
                String msg = "未知错误";
                if (result.containsKey("errorMessage")) {
                    msg = (String) result.get("errorMessage");
                } else if (result.containsKey("message")) {
                    msg = (String) result.get("message");
                } else if (result.containsKey("statusMsg")) {
                    msg = (String) result.get("statusMsg");
                }

                result.put("success", false);
                result.put("message", msg);
                return result;
            }

        } catch (Exception e) {
            log.error("解析WeBase响应失败: {}", responseBody, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "解析WeBase响应失败: " + e.getMessage());
            return errorResult;
        }

    }

    @PostConstruct
    public void init() {
        if (bcosConfig.getWebase() != null) {
            // 直接使用配置中的URL和groupId
            baseUrl = bcosConfig.getWebase().getUrl();
            groupId = bcosConfig.getWebase().getGroupId();

            // 移除前端路由部分
            if (baseUrl != null && baseUrl.contains("/#/")) {
                baseUrl = baseUrl.substring(0, baseUrl.indexOf("/#/"));
            }

            log.info("WeBase服务初始化完成: baseUrl={}, groupId={}", baseUrl, groupId);

            // 初始化时加载合约列表到缓存
            loadContractListToCache();
        } else {
            log.error("WeBase配置未加载!");
        }
    }

    // 加载合约列表到缓存
    private void loadContractListToCache() {
        List<ContractInfo> contracts = getContractList();
        for (ContractInfo contract : contracts) {
            if (contract.getContractAddress() != null && contract.getContractAbi() != null) {
                String address = contract.getContractAddress().toLowerCase();

                // 缓存ABI
                contractAbiCache.put(address, contract.getContractAbi());

                // 缓存简化合约信息
                simplifiedContractCache.put(address, createSimplifiedContract(contract));

                log.debug("缓存合约: {} -> {}", address, contract.getContractName());
            }
        }
        log.info("已缓存 {} 个合约信息", simplifiedContractCache.size());
    }

    // 创建简化合约信息
    private SimplifiedContractInfo createSimplifiedContract(ContractInfo contract) {
        SimplifiedContractInfo simplified = new SimplifiedContractInfo();
        simplified.setContractAddress(contract.getContractAddress());
        simplified.setContractName(contract.getContractName());

        try {
            // 解析ABI获取方法和参数
            JsonNode abiArray = objectMapper.readTree(contract.getContractAbi());
            List<ContractMethod> methods = new ArrayList<>();

            for (JsonNode abiNode : abiArray) {
                if (abiNode.has("type") && "function".equals(abiNode.get("type").asText())) {
                    ContractMethod method = new ContractMethod();
                    method.setName(abiNode.get("name").asText());

                    List<Param> inputs = new ArrayList<>();
                    JsonNode inputsNode = abiNode.get("inputs");
                    if (inputsNode.isArray()) {
                        for (JsonNode inputNode : inputsNode) {
                            Param param = new Param();
                            param.setName(inputNode.get("name").asText());
                            param.setType(inputNode.get("type").asText());
                            inputs.add(param);
                        }
                    }
                    method.setInputs(inputs);
                    methods.add(method);
                }
            }
            simplified.setMethods(methods);
        } catch (Exception e) {
            log.error("解析ABI失败: {}", contract.getContractAddress(), e);
        }

        return simplified;
    }

    // 获取合约列表 - 修复反序列化问题
    public List<ContractInfo> getContractList() {
        if (!validateBaseUrl()) return Collections.emptyList();

        String apiUrl = baseUrl + "/contract/contractList";
        log.info("请求合约列表: {}", apiUrl);

        try {
            // 1. 准备请求头和体
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 构建符合后端要求的请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("groupId", groupId);
            requestBody.put("pageNumber", 1);
            requestBody.put("pageSize", 100); // 获取全部合约
            requestBody.put("contractName", ""); // 可选参数

            HttpEntity<Map<String, Object>> requestEntity =
                    new HttpEntity<>(requestBody, headers);

            // 2. 发送POST请求
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.debug("合约列表响应: 状态码={}, 响应体={}",
                    response.getStatusCode(),
                    response.getBody());

            // 3. 解析响应 - 使用更灵活的方式处理数据结构
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());

                // 检查响应代码
                int code = rootNode.get("code").asInt();
                String message = rootNode.get("message").asText();

                if (code == 0) {
                    JsonNode dataNode = rootNode.get("data");

                    // 处理不同的数据结构
                    List<ContractInfo> contracts;
                    if (dataNode.isArray()) {
                        // 如果data是数组，直接解析
                        contracts = new ArrayList<>();
                        for (JsonNode contractNode : dataNode) {
                            ContractInfo contract = parseContractInfo(contractNode);
                            contracts.add(contract);
                        }
                        log.info("成功获取 {} 个合约 (数组格式)", contracts.size());
                    } else if (dataNode.has("content")) {
                        // 如果data是分页对象，提取content数组
                        contracts = new ArrayList<>();
                        JsonNode contentNode = dataNode.get("content");
                        for (JsonNode contractNode : contentNode) {
                            ContractInfo contract = parseContractInfo(contractNode);
                            contracts.add(contract);
                        }
                        log.info("成功获取 {} 个合约 (分页格式)", contracts.size());
                    } else {
                        log.error("未知的响应格式: {}", dataNode);
                        return Collections.emptyList();
                    }

                    return contracts;
                } else {
                    log.error("WeBase返回错误: code={}, message={}", code, message);
                }
            }
        } catch (Exception e) {
            log.error("获取合约列表失败: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    // 解析单个合约信息
    private ContractInfo parseContractInfo(JsonNode contractNode) {
        ContractInfo contract = new ContractInfo();
        contract.setContractName(contractNode.get("contractName").asText());
        contract.setContractAddress(contractNode.get("contractAddress").asText());
        contract.setContractPath(contractNode.get("contractPath").asText());
        contract.setContractAbi(contractNode.get("contractAbi").asText());

        // 可选字段
        if (contractNode.has("deployTime")) {
            contract.setDeployTime(contractNode.get("deployTime").asText());
        }
        if (contractNode.has("contractBin")) {
            contract.setContractBin(contractNode.get("contractBin").asText());
        }
        if (contractNode.has("bytecodeBin")) {
            contract.setBytecodeBin(contractNode.get("bytecodeBin").asText());
        }
        if (contractNode.has("contractSource")) {
            contract.setContractSource(contractNode.get("contractSource").asText());
        }

        return contract;
    }

    // 获取合约ABI - 从缓存中获取
    public String getContractAbi(String contractAddress) {
        if (!validateBaseUrl() || contractAddress == null || contractAddress.isEmpty()) {
            return "";
        }

        // 转换为小写以统一处理
        String addressKey = contractAddress.toLowerCase();

        // 1. 首先尝试从缓存获取
        if (contractAbiCache.containsKey(addressKey)) {
            return contractAbiCache.get(addressKey);
        }

        // 2. 缓存中没有，重新加载合约列表
        log.info("ABI未缓存，重新加载合约列表: {}", contractAddress);
        loadContractListToCache();

        // 3. 再次尝试从缓存获取
        return contractAbiCache.getOrDefault(addressKey, "");
    }

    // 获取简化合约信息（给前端使用）
    public SimplifiedContractInfo getSimplifiedContractInfo(String contractAddress) {
        if (!validateBaseUrl() || contractAddress == null || contractAddress.isEmpty()) {
            return null;
        }

        String addressKey = contractAddress.toLowerCase();

        // 1. 首先尝试从缓存获取
        if (simplifiedContractCache.containsKey(addressKey)) {
            return simplifiedContractCache.get(addressKey);
        }

        // 2. 缓存中没有，重新加载合约列表
        log.info("合约信息未缓存，重新加载: {}", contractAddress);
        loadContractListToCache();

        // 3. 再次尝试从缓存获取
        return simplifiedContractCache.get(addressKey);
    }

    // 获取所有简化合约信息（给前端使用）
    public List<SimplifiedContractInfo> getAllSimplifiedContracts() {
        if (!validateBaseUrl()) return Collections.emptyList();

        // 确保缓存是最新的
        if (simplifiedContractCache.isEmpty()) {
            loadContractListToCache();
        }

        return new ArrayList<>(simplifiedContractCache.values());
    }

    private boolean validateBaseUrl() {
        if (baseUrl == null || baseUrl.isEmpty()) {
            log.error("WeBase基础URL未配置!");
            return false;
        }
        return true;
    }

    // ================== DTO 定义 ==================

    // 完整合约信息DTO
    @Data
    public static class ContractInfo {
        private String contractName;
        private String contractAddress;
        private String contractPath;
        private String contractAbi;  // 原始的ABI字符串
        private String deployTime;
        private String contractBin;
        private String bytecodeBin;
        private String contractSource;
    }

    // 给前端的简化合约信息
    @Data
    public static class SimplifiedContractInfo {
        private String contractName;
        private String contractAddress;
        private List<ContractMethod> methods;
    }

    // 合约方法信息
    @Data
    public static class ContractMethod {
        private String name;
        private List<Param> inputs;
    }

    // 方法参数信息
    @Data
    public static class Param {
        private String name;
        private String type;
    }
}