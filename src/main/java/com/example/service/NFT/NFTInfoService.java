package com.example.service.NFT;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.NFTInfoDto;

import java.util.List;

public interface NFTInfoService extends IService<NFTInfoDto> {

    boolean NFTInfoAddSingle(NFTInfoDto nftInfoDto);
    boolean NFTInfoUpdateSingle(NFTInfoDto nftInfoDto);

    boolean NFTInfoAddMulti(NFTInfoDto nftInfoDto);
    boolean NFTInfoUpdateMulti(NFTInfoDto nftInfoDto);

    NFTInfoDto NFTInfoSelectSingle(NFTInfoDto nftInfoDto);
    List<NFTInfoDto> NFTInfoSelectMulti(List<NFTInfoDto> nftInfoDtoList);


}
