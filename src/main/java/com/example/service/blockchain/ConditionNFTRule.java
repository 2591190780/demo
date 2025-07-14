package com.example.service.blockchain;

import com.example.entity.dto.BlockChainEvidenceDto;
import com.example.entity.dto.NFTInfoDto;
import com.example.entity.dto.NFTRuleDto;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.mapper.BlockChainEvidenceMapper;
import com.example.service.AccountService;
import com.example.service.BlockChainEvidenceService;
import com.example.service.NFT.NFTInfoService;
import com.example.service.NFT.NFTRuleService;
import com.example.service.product.ProductInfoSelectAccountService;
import com.example.service.sensor.SensorDataInfoSelectService;
import com.example.service.sensor.SensorInfoSelectService;
import com.example.service.transaction.TransactionProcessService;
import com.example.utils.Const;
import com.example.utils.WeBaseUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ConditionNFTRule {
        @Resource BlockChainEvidenceMapper blockChainEvidenceMapper;
        @Resource MessageReportService messageReportService;
        @Resource BlockChainEvidenceService blockChainEvidenceService;
        @Resource AccountService accountService;
        @Resource ProductInfoSelectAccountService productInfoSelectAccountService;
        @Resource SensorInfoSelectService sensorInfoSelectService;
        @Resource SensorDataInfoSelectService sensorDataInfoSelectService;
        @Resource TransactionProcessService transactionProcessService;
        @Resource NFTInfoService nftInfoService;
        @Resource NFTRuleService nftRuleService;
        @Resource WeBaseUtils weBaseUtils;
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
     */
    //这里是最后一块了。
    public AuthorizeVO selectSatisfyCondition(Integer nftId) {
        NFTInfoDto nftInfoDto =this.nftInfoService.NFTInfoSelectByTemplateId(nftId);
        if(this.nftRuleService.nftRuleSelectByActId(nftId)==null){ return null;}
        String contractAddress = nftInfoDto.getContractAddress();
        AuthorizeVO authorizeVO = new AuthorizeVO();
        return authorizeVO;





        }

    public boolean NFTRuleReport(String userAddress, Integer nftID,
                                 List<Map<String,Object>> ruleList) throws Exception {
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
            NFTRuleDto ruleDto =this.nftRuleService.nftRuleSelectByActId(nftID);
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
