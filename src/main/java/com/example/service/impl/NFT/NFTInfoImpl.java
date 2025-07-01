package com.example.service.impl.NFT;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.NFTInfoDto;
import com.example.mapper.NFT.NFTInfoMapper;
import com.example.service.NFT.NFTInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NFTInfoImpl extends ServiceImpl<NFTInfoMapper, NFTInfoDto> implements NFTInfoService {


    @Override
    public boolean NFTInfoAddSingle(NFTInfoDto nftInfoDto){
        /**
         * 前端传入--> 名称、描述、图片、发行个数、nft稀有度，metadata元数据
         *
         * 后端需要 使用ipfs 以及 上链获取nft地址
         */



        return true;
    }

    @Override
    public boolean NFTInfoUpdateSingle(NFTInfoDto nftInfoDto){
        return true;
    }

    @Override
    public boolean NFTInfoAddMulti(NFTInfoDto nftInfoDto){
        return true;
    }

    @Override
    public boolean NFTInfoUpdateMulti(NFTInfoDto nftInfoDto){
        return true;
    }

    @Override
    public NFTInfoDto NFTInfoSelectSingle(NFTInfoDto nftInfoDto){
        return null;
    }

    @Override
    public List<NFTInfoDto> NFTInfoSelectMulti(List<NFTInfoDto> nftInfoDtoList){
        return null;
    }
}
