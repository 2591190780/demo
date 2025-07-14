package com.example.service.impl.NFT;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.NFTInfoDto;
import com.example.mapper.NFT.NFTInfoMapper;
import com.example.service.NFT.NFTAddInfoService;

import com.example.utils.Const;
import com.example.utils.InfoToRedisUtils;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class NFTAddInfoImpl extends ServiceImpl<NFTInfoMapper, NFTInfoDto> implements NFTAddInfoService {
    @Resource
    JwtUtils jwtUtils;

    @Resource
    InfoToRedisUtils infoToRedisUtils;

    @Override
    public boolean NFTInfoAddSingle(HttpServletRequest request, NFTInfoDto nftInfoDto){
        /**
         * 前端传入--> 名称、描述、图片、发行个数、nft稀有度，metadata元数据
         * 后端需要 使用ipfs 以及 上链获取nft地址
         */
        Integer userId =  jwtUtils.getRequesetId(request);
        if(!Objects.equals(nftInfoDto.getPublicBy(), userId)) return false;
        LocalDateTime createTime = LocalDateTime.now();
        nftInfoDto.setCreatedAt(createTime);
        nftInfoDto.setRemainCount(nftInfoDto.getIssuanceLimit());
        nftInfoDto.setIsActive(0);
        nftInfoDto.setContractAddress(Const.CONTRACT_FOR_NFT_INFO);
        if (this.save(nftInfoDto)){
            // 请求发送给 Redis等待管理员确认
            infoToRedisUtils.InfoToRedis(nftInfoDto.getTemplateId(),userId,"add","nft_info");
            return true;
        }
        return false;
    }


}
