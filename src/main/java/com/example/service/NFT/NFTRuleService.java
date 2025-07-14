package com.example.service.NFT;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.NFTRuleDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface NFTRuleService extends IService<NFTRuleDto> {
    NFTRuleDto nftRuleSelectByID (Integer id);
    List<NFTRuleDto> nftRuleSelectByName (String name);
    List<NFTRuleDto> nftRuleSelectByTemplateID (Integer id);
    NFTRuleDto nftRuleSelectByHash(String hash);
    List<NFTRuleDto> nftRuleSelectCondition(NFTRuleDto params);
    boolean nftRuleUpdateAdmin(Integer id,byte answer);
    NFTRuleDto nftRuleSelectByActId (Integer id);

}