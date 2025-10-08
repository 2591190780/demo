package com.example.service.blockchain;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alipay.api.domain.AccountDTO;
import com.alipay.api.domain.AccountVO;
import com.alipay.api.domain.ProductInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.entity.dto.*;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.mapper.BlockChainEvidenceMapper;
import com.example.mapper.product.ProductInfoSelectAccountMapper;
import com.example.mapper.sensor.SensorDataInfoMapper;
import com.example.mapper.sensor.SensorInfoMapper;
import com.example.mapper.transaction.TransactionProcessMapper;
import com.example.service.AccountService;
import com.example.service.BlockChainEvidenceService;
import com.example.service.NFT.NFTInfoService;
import com.example.service.NFT.NFTRuleService;
import com.example.service.NFT.UserNFTService;
import com.example.service.product.ProductInfoSelectAccountService;
import com.example.service.sensor.SensorDataInfoSelectService;
import com.example.service.sensor.SensorInfoSelectService;
import com.example.service.transaction.TransactionProcessService;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import com.example.utils.WeBaseUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConditionNFTRule {
        @Resource BlockChainEvidenceMapper blockChainEvidenceMapper;
        @Resource MessageReportService messageReportService;
        @Resource BlockChainEvidenceService blockChainEvidenceService;
        @Resource AccountService accountService;
        @Resource JwtUtils jwtUtils;
        @Resource
        ProductInfoSelectAccountMapper productInfoSelectAccountMapper;
        @Resource
        SensorInfoMapper sensorInfoMapper;
        @Resource
        SensorDataInfoMapper sensorDataInfoMapper;
        @Resource
        TransactionProcessMapper transactionProcessMapper;
        @Resource NFTInfoService nftInfoService;
        @Resource NFTRuleService nftRuleService;
        @Resource WeBaseUtils weBaseUtils;
        @Resource UserNFTService userNFTService;

    /**
     *  NFT发放过程：
     *  首先商家选择要发放的 NFT （对应的信息应该是：智能合约地址）-->
     *  这里需要规定好NFT规则的链上格式，后端通过call调用NFT规则来自动解析规则。
     *  所有合约必须有一个GETRule方法来获取NFT的规则，返回给后端。
     *  规定：NFT规则的链上格式为：
     *  {
     *      所需要使用的表名 : ["条件1","&&","条件二",......,"#"],
     *      所需要使用的表名 : ["条件1","||","条件二","#"]
     *      //条件以#号为标志表示结束。
     *  }
     *  "条件X" 的格式为 "名称:关系:值" 以冒号分割。
     *  由管理员上传该NFT智能合约地址。
     *  然后筛选满足规则的 所有用户（按时间排序）
     *
     */
    //这里是最后一块了。/
    public List<Account> selectSatisfyCondition(HttpServletRequest request
            ,Integer nftId,List<String> targetDims) throws Exception {
        //nft发布者可以进行nft发放。
        Integer rid = jwtUtils.getRequesetId(request);
        String requestAddress = accountService.findAccountById(rid).getWalletAddress();
        //查询了NFT的规则是否存在。
        NFTInfoDto nftInfoDto =this.nftInfoService.NFTInfoSelectByTemplateId(nftId);
        //同一个NFT的规则有且只有一个生效。
        if(this.nftRuleService.nftRuleSelectByActId(nftId)==null){ return null;}
        //NFT不属于当前用户，不允许筛选。
        if(!Objects.equals(this.nftInfoService.NFTInfoSelectByTemplateId(nftId).getPublicBy(), rid)){ return null;}
        //从区块链NFTRULE合约中获取所有的合约规则。
        List<Object> pa = new ArrayList<>();
        pa.add(0,nftId);
        Map<String, Object> ans
                = weBaseUtils.callContractMethod(requestAddress,
                Const.CONTRACT_FOR_NFT_RULE,
                Const.CONTRACT_FOR_NFT_RULE_METHOD_GETRULEBYTOKENID
                ,pa);

        Map<String, List<Long>> ansList = this.ansRuleAnalyseMultiDim(ans,nftId,targetDims);
        List<Long> longList = ansList.get("mergedResult");
        List<Integer> intList = new ArrayList<>(longList.size());
        for (Long l : longList) {
            intList.add(l.intValue());  // 或 l == null ? null : l.intValue()
        }
        List<Account> voList = new ArrayList<>();
        for(Integer uid :  intList){
            Account account = accountService.findAccountById(uid);
            if(this.userNFTService.selectNFTByUNid(uid,nftId)!=null) continue;
            account.setPassword(null);
            voList.add(account);
        }
        return voList;
    }

    /**
     * 真实表名与其支持的维度字段映射
     */
    private static final Map<String, Map<String, String>> DIMENSION_FIELD_MAP = Map.of(
            "product_info", Map.of(
                    "farmer", "farmer_id",
                    "name","name"
            ),
            "sensor_info", Map.of(
                    "farmer", "farmer_id"
            ),
            "order_info", Map.of(
                    "buyer",   "buyer_id",
                    "seller",  "seller_id"

                    //这里添加聚合字段
            )
    );

    public Map<String, List<Long>> ansRuleAnalyseMultiDim(
            Map<String, Object> ans,
            Integer nftId,
            List<String> targetDimensions
    ) {
        String dataStr = ans.get("data").toString();
        if ("[[]]".equals(dataStr)) return Collections.emptyMap();

        Object raw = ans.get("data");
        if (!(raw instanceof List<?> dataList) || dataList.isEmpty()) return Collections.emptyMap();

        Object first = dataList.get(0);
        if (!(first instanceof String jsonString)) return Collections.emptyMap();

        JSONArray rulesArray = JSON.parseArray(jsonString);

        Map<String, Map<Integer, Object>> dimRuleResults = new LinkedHashMap<>();
        for (String dim : targetDimensions) {
            dimRuleResults.put(dim, new LinkedHashMap<>());
        }

        List<String> logicOps = new ArrayList<>(); // 用来保存null规则的逻辑符
        List<String> dimOrder = new ArrayList<>(); // 非null规则对应的维度顺序
        List<List<Long>> idLists = new ArrayList<>(); // 非null规则对应的ID列表

        int dimIdx = 0;
        for (int i = 0; i < rulesArray.size(); i++) {
            JSONArray rule = rulesArray.getJSONArray(i);
            String tableName = rule.getString(0);
            List<String> condList = rule.getJSONArray(1).stream()
                    .map(Object::toString)
                    .filter(s -> !s.equals("#"))
                    .collect(Collectors.toList());

            if (!"null".equals(tableName)) {
                if (dimIdx >= targetDimensions.size()) {
                    throw new IllegalArgumentException("规则数量超过目标维度数量，请检查维度匹配顺序。");
                }
                String currentDim = targetDimensions.get(dimIdx);
                Map<String, String> dimMap = DIMENSION_FIELD_MAP.get(tableName);
                if (dimMap == null || !dimMap.containsValue(currentDim)) {
                    throw new IllegalArgumentException("表 " + tableName + " 不支持维度字段 " + currentDim);
                }
                List<Long> ids = applyRule(tableName, condList, currentDim);
                dimRuleResults.get(currentDim).put(i, ids);

                // 记录维度和对应id列表，方便后续合并
                dimOrder.add(currentDim);
                idLists.add(ids);

                dimIdx++;
            } else {
                // null 只放入前一个维度（默认连接两个规则）
                if (dimIdx > 0) {
                    String prevDim = targetDimensions.get(dimIdx - 1);
                    dimRuleResults.get(prevDim).put(i, condList);

                    // 这里直接取逻辑符号放到logicOps
                    if (!condList.isEmpty()) {
                        logicOps.add(condList.get(0));
                    }
                }
            }
        }

        Map<String, List<Long>> allDimResults = new LinkedHashMap<>();

        // 保留原有单维度内部合并逻辑不变
        for (String dim : targetDimensions) {
            Map<Integer, Object> ruleResults = dimRuleResults.get(dim);
            if (ruleResults.isEmpty()) continue;

            List<Integer> toRemove = new ArrayList<>();
            List<Integer> keys = new ArrayList<>(ruleResults.keySet());

            for (int idx : keys) {
                Object v = ruleResults.get(idx);
                if (v instanceof List<?> logicList && !logicList.isEmpty()) {
                    Object firstElem = logicList.get(0);
                    if (firstElem instanceof String op && ("&&".equals(op) || "||".equals(op))) {
                        @SuppressWarnings("unchecked")
                        List<Long> left = (List<Long>) ruleResults.get(idx - 1);
                        @SuppressWarnings("unchecked")
                        List<Long> right = (List<Long>) ruleResults.get(idx + 1);

                        if (left == null || right == null) continue;

                        List<Long> merged;
                        if ("&&".equals(op)) {
                            merged = left.stream().filter(right::contains).collect(Collectors.toList());
                        } else {
                            Set<Long> set = new LinkedHashSet<>();
                            set.addAll(left);
                            set.addAll(right);
                            merged = new ArrayList<>(set);
                        }
                        ruleResults.put(idx + 1, merged);
                        toRemove.add(idx - 1);
                        toRemove.add(idx);
                    }
                }
            }
            toRemove.forEach(ruleResults::remove);

            @SuppressWarnings("unchecked")
            List<Long> finalIds = (List<Long>) ruleResults.values().iterator().next();

            allDimResults.put(dim, finalIds);
        }

        // 新增：根据logicOps顺序合并所有非null规则的idLists
        if (!idLists.isEmpty()) {
            List<Long> mergedResult = idLists.get(0);
            for (int i = 0; i < logicOps.size(); i++) {
                String op = logicOps.get(i);
                List<Long> nextList = idLists.get(i + 1);
                if ("&&".equals(op)) {
                    mergedResult = mergedResult.stream()
                            .filter(nextList::contains)
                            .collect(Collectors.toList());
                } else if ("||".equals(op)) {
                    Set<Long> unionSet = new LinkedHashSet<>(mergedResult);
                    unionSet.addAll(nextList);
                    mergedResult = new ArrayList<>(unionSet);
                }
            }
            allDimResults.put("mergedResult", mergedResult);
        }

        return allDimResults;
    }



    /**
     * 执行单条聚合查询
     */
    private List<Long> applyRule(
            String tableName,
            List<String> condList,
            String dimensionField
    ) {
        // 提取逻辑符号
        List<String> ops = condList.stream()
                .filter(s -> "&&".equals(s) || "||".equals(s))
                .toList();

        Long amountTh = null, countTh = null, moneyTh = null;
        LocalDateTime start = null, end = null;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        for (String c : condList) {
            if (c.startsWith("amount:")) {
                amountTh = Long.parseLong(c.split(":")[2]);
            } else if (c.startsWith("count:")) {
                countTh = Long.parseLong(c.split(":")[2]);
            } else if (c.startsWith("money:")) {
                moneyTh = Long.parseLong(c.split(":")[2]);
            } else if (c.startsWith("time:<>:")) {
                String raw = c.substring("time:<>:".length());
                String[] se = raw.split("!!");  // 用 ! 分隔
                start = LocalDateTime.parse(se[0], fmt);
                end   = LocalDateTime.parse(se[1], fmt);
            }
        }

        List<Map<String, Object>> rows;
        switch (tableName) {
            case "product_info" -> {
                QueryWrapper<ProductInfoAccountDto> w = new QueryWrapper<>();
                if (start != null && end != null) {
                    w.between("create_time", start, end);
                }
                w.select(dimensionField, "SUM(stock) AS agg")
                        .groupBy(dimensionField);
                if (amountTh != null) {
                    w.having("SUM(stock) > {0}", amountTh);
                }
                rows = productInfoSelectAccountMapper.selectMaps(w);
            }
            case "sensor_info" -> {
                QueryWrapper<SensorInfoDto> w = new QueryWrapper<>();
                // 假设时间字段是 create_time
                if (start != null && end != null) {
                    w.between("create_time", start, end);
                }
                w.select(dimensionField, "COUNT(1) AS cnt")
                        .groupBy(dimensionField);
                if (countTh != null) {
                    w.having("COUNT(1) > {0}", countTh);
                }
                rows = sensorInfoMapper.selectMaps(w);
            }
            case "sensor_datainfo" -> {
                QueryWrapper<SensorDataInfoDto> w = new QueryWrapper<>();
                // 假设时间字段是 upload_time
                if (start != null && end != null) {
                    w.between("create_time", start, end);
                }
                w.select(dimensionField, "COUNT(1) AS cnt")
                        .groupBy(dimensionField);
                if (countTh != null) {
                    w.having("COUNT(1) > {0}", countTh);
                }
                rows = sensorDataInfoMapper.selectMaps(w);
            }
            case "order_info" -> {
                QueryWrapper<TransactionAccountDto> w = new QueryWrapper<>();
                if (start != null && end != null) w.between("order_time", start, end);
                w.select(dimensionField, "COUNT(1) AS cnt", "SUM(quantity) AS allQuantity")
                        .groupBy(dimensionField);

                // 提取逻辑符 ops
                ops = condList.stream()
                        .filter(s -> "&&".equals(s) || "||".equals(s))
                        .toList();

                // 定义条件列表 conds
                List<String> conds = new ArrayList<>();

                for (String c : condList) {
                    if (!"&&".equals(c) && !"||".equals(c)) {
                        if (c.startsWith("count:")) {
                            conds.add("cnt > " + countTh);
                        } else if (c.startsWith("money:") || c.startsWith("amount:")) {
                            conds.add("allQuantity > " + moneyTh);
                        }
                    }
                }

                StringBuilder having = new StringBuilder();
                if (!conds.isEmpty()) {
                    having.append(conds.get(0));
                    for (int i = 0; i < ops.size(); i++) {
                        String op = ops.get(i).equals("&&") ? " AND " : " OR ";
                        if (i + 1 < conds.size()) {
                            having.append(op).append(conds.get(i + 1));
                        }
                    }
                }
                if (!having.isEmpty()) {
                    w.having(having.toString());
                }
                rows = transactionProcessMapper.selectMaps(w);
            }
            default -> throw new IllegalArgumentException("不支持的表名: " + tableName);
        }

        return rows.stream()
                .map(m -> ((Number) m.get(dimensionField)).longValue())
                .collect(Collectors.toList());
    }



    public boolean NFTRuleReport(String userAddress, Integer nftID,
                                 List<Map<String,Object>> ruleList) throws Exception {
        //先检查该NFT是不是已经有规则了，有的话要删除。
        List<Object> pa = new ArrayList<>();
        pa.add(0,nftID);
        Map<String, Object> ans
                = weBaseUtils.callContractMethod(userAddress,
                Const.CONTRACT_FOR_NFT_RULE,
                Const.CONTRACT_FOR_NFT_RULE_METHOD_GETRULEBYTOKENID
                ,pa);
        if(!ans.get("data").toString().equals("[[]]")) {
            weBaseUtils.callContractMethod(userAddress,
                    Const.CONTRACT_FOR_NFT_RULE,
                    Const.CONTRACT_FOR_NFT_RULE_METHOD_DELETERULEGROUP
                    ,pa);
        };
        ObjectMapper mapper = new ObjectMapper();
        for (Map<String,Object> rule : ruleList){
            String tableName = (String) rule.get("tableName");
            Object condObj = rule.get("condition");
            String[] condition;
            if (condObj instanceof List<?>) {
                List<?> list = (List<?>) condObj;
                // 检查最后一项是否是 "#"
                if (list.isEmpty() || !"#".equals(list.get(list.size() - 1).toString())) {
                    return false;
                }
                // 转成 String[]
                condition = list.stream().map(Object::toString).toArray(String[]::new);

            } else {
                return false; // 非法类型
            }
            //  将 condition 转为 JSON 字符串，用于合约调用
            String conditionJson;
            try {
                conditionJson = mapper.writeValueAsString(condition);  // 结果是：["age:>:18","region:=:CN","#"]
            } catch (JsonProcessingException e) {
                return false;
            }
            NFTInfoDto dto = this.nftInfoService.NFTInfoSelectByTemplateId(nftID);
            NFTRuleDto ruleDto =this.nftRuleService.nftRuleSelectByActChainNodeId(nftID);
            if( dto==null || ruleDto ==null) return false;
            List<Object> param = new ArrayList<>();
            param.add(0,tableName);
            param.add(1,conditionJson);
            param.add(2,Const.CONTRACT_FOR_NFT_INFO);
            param.add(3,nftID);
            param.add(4,"ipfs/"+dto.getImageUrl());
            param.add(5,dto.getMetadataUrl());
            param.add(6,ruleDto.getApplyHash());
            //执行上报逻辑
            Map<String, Object> result = weBaseUtils.callContractMethod(userAddress,Const.CONTRACT_FOR_NFT_RULE,
                    Const.CONTRACT_FOR_NFT_RULE_METHOD_ADDORUODATERULE,param);
            String txHash = (String) result.get("transactionHash");
            String blockNumber = (String) result.get("blockNumber");
            BlockChainEvidenceDto evidenceDto = new BlockChainEvidenceDto(
                    null,6,ruleDto.getRuleId()
                    ,txHash,ruleDto.getApplyHash(),blockNumber, LocalDateTime.now()
            );
            this.blockChainEvidenceMapper.insert(evidenceDto);

        }
        return true;
    }


}
