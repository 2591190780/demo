package com.example.service.NFT;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.NFTInfoDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface NFTInfoService extends IService<NFTInfoDto> {

    NFTInfoDto NFTInfoSelectByTemplateId(Integer id);
    List<NFTInfoDto> NFTInfoSelectByPublic(Integer publicBy);
    NFTInfoDto NFTInfoSelectByURL(String url);
    List<NFTInfoDto> infoSelectByCondition(NFTInfoDto params);
     boolean NFTInfoUpdateAdmin(Integer id,byte answer);




}
