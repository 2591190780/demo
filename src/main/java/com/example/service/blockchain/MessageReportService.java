package com.example.service.blockchain;


import com.example.service.BlockChainEvidenceService;
import com.example.utils.WeBaseUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MessageReportService {
    @Resource
    WeBaseUtils weBaseUtils;

    @Resource
    BlockChainEvidenceService blockChainEvidenceService;

    /**
     * params严格按照合约输入的顺序 dataType(int) relatedId(int) submitHash(hash)
     */
    public String blockChainEvidenceReport(String operationName, List<Object> params ,
                                           String userWalletAddress,String contractAddress) throws Exception {
        WeBaseUtils.SimplifiedContractInfo info =this.weBaseUtils.getSimplifiedContractInfo(contractAddress);
        if(info==null) return "未查询到该合约信息";
        List<WeBaseUtils.ContractMethod> methodList  = info.getMethods();
        for (WeBaseUtils.ContractMethod method : methodList ){
            if(!Objects.equals(method.getName(), operationName)) continue;
            if (!this.blockChainEvidenceService.addInfo((Integer) params.get(0)
                    , (Integer) params.get(1), (String) params.get(2))) continue;
            Map<String, Object> result
                    = weBaseUtils.callContractMethod(userWalletAddress,contractAddress,operationName,params);
            String txHash = (String) result.get("transactionHash");
            String blockNumber = (String) result.get("blockNumber");

            String submitHash = (String) params.get(2);
            this.blockChainEvidenceService.updateInfoBySubmitHash(blockNumber,txHash,submitHash);
            return "上链成功";
        }
        return null;
    }
}
