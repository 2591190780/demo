package com.example.service.NFT;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.NFTInfoDto;
import jakarta.servlet.http.HttpServletRequest;

public interface NFTAddInfoService extends IService<NFTInfoDto> {
    boolean NFTInfoAddSingle(HttpServletRequest request, NFTInfoDto nftInfoDto);
}
