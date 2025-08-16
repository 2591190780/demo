package com.example.service.NFT;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.NFTTransactionDto;
import com.example.entity.dto.UserNFTDto;

import java.util.List;

public interface UserNFTService extends IService<UserNFTDto> {

    UserNFTDto selectNFTById(Integer id);
    List<UserNFTDto> selectNFTByUid(Integer uid);
    List<UserNFTDto> selectNFTByNFTid(Integer nftId);
    UserNFTDto selectNFTByUNid(Integer userId,Integer nftId);
    boolean UserNFT(NFTTransactionDto dto);
    boolean deleteUserNFT(Integer id,Integer nftid);
    boolean updateNFTStatus(Integer uid,Integer nftId ,Integer status);
}
