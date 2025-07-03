package com.example.service.NFT;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.NFTRuleDto;
import jakarta.servlet.http.HttpServletRequest;

public interface NFTAddRuleService extends IService<NFTRuleDto> {
    boolean NFTRuleAddSingle(HttpServletRequest request, NFTRuleDto nftRuleDto);
}
