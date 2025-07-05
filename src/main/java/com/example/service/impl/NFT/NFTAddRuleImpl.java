package com.example.service.impl.NFT;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.NFTRuleDto;
import com.example.mapper.NFT.NFTRuleMapper;
import com.example.service.NFT.NFTAddRuleService;
import com.example.service.NFT.NFTInfoService;
import com.example.service.NFT.NFTRuleService;
import com.example.utils.BlockchainHashUtil;
import com.example.utils.InfoToRedisUtils;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class NFTAddRuleImpl extends ServiceImpl<NFTRuleMapper, NFTRuleDto> implements NFTAddRuleService {

    @Resource
    JwtUtils jwtUtils;

    @Resource
    InfoToRedisUtils infoToRedisUtils;

    @Resource
    NFTInfoService nftInfoService;

    @Resource
    BlockchainHashUtil blockchainHashUtil;

    @Resource
    NFTRuleService nftRuleService;

    @Override
    public boolean NFTRuleAddSingle(HttpServletRequest request, NFTRuleDto nftRuleDto){

        /**
         *   先提交NFT信息的申请后  nft_info中的publicID可能会对应多条templateID
         *   前端需要给的参数为： name , template id , validity_period , table
         */
        Integer userId =  jwtUtils.getRequesetId(request);
        Integer templateId = nftRuleDto.getTemplateId();
        //发布者与当前登录的用户ID相同才可以进行后续操作。
        if (!Objects.equals(nftInfoService.NFTInfoSelectByTemplateId(templateId).getPublicBy(), userId)){
            return false;
        }
        //如果新添加的规则对应的NFT 还存在 生效的 规则 则不允许添加
        if(nftRuleService.nftRuleSelectByActId(templateId)!=null) return false;
        LocalDateTime createTime = LocalDateTime.now();
        nftRuleDto.setCreatedAt(createTime);
        LocalDateTime endTime = null ;
        if(nftRuleDto.getValidityPeriod()!=-1 && nftRuleDto.getValidityPeriod()!=0){
            endTime =  createTime.plusDays(nftRuleDto.getValidityPeriod());
        }
        nftRuleDto.setPassActive(endTime);
        nftRuleDto.setIsActive(0);
        nftRuleDto.setTemplateId(templateId);
        nftRuleDto.setApplyHash(blockchainHashUtil.generateNFTRuleHash(nftRuleDto));
        if (this.save(nftRuleDto)){
            // 请求发送给 Redis等待管理员确认
            infoToRedisUtils.InfoToRedis(nftRuleDto.getRuleId(),userId,"add","nft_rule");
            return true;
        }
        return false;
    }


}
