package com.example.service.blockchain;

import com.example.entity.dto.Account;
import com.example.entity.dto.NFTInfoDto;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.service.AccountService;
import com.example.service.NFT.NFTInfoService;
import com.example.service.NFT.NFTRuleService;
import com.example.service.product.ProductInfoSelectAccountService;
import com.example.service.sensor.SensorDataInfoSelectService;
import com.example.service.sensor.SensorInfoSelectService;
import com.example.service.transaction.TransactionProcessService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class SelectSatisfyConditionNFT {

        @Resource AccountService accountService;
        @Resource ProductInfoSelectAccountService productInfoSelectAccountService;
        @Resource SensorInfoSelectService sensorInfoSelectService;
        @Resource SensorDataInfoSelectService sensorDataInfoSelectService;
        @Resource TransactionProcessService transactionProcessService;
        @Resource NFTInfoService nftInfoService;
        @Resource NFTRuleService nftRuleService;
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
    public AuthorizeVO selectSatisfyCondition(Integer nftId) {
        NFTInfoDto nftInfoDto =this.nftInfoService.NFTInfoSelectByTemplateId(nftId);
        if(this.nftRuleService.nftRuleSelectByActId(nftId)==null){ return null;}
        String contractAddress = nftInfoDto.getContractAddress();
        AuthorizeVO authorizeVO = new AuthorizeVO();


        return authorizeVO;


        }




}
