package com.example.service.NFT;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.NFTPendingApplication;
import com.example.entity.dto.NFTTransactionDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface NFTTransactionService extends IService<NFTTransactionDto> {

    boolean addAgreeNFTTransaction(NFTTransactionDto nftTransactionDto) throws Exception;
    List<NFTTransactionDto> selectNFTTransactionByNFTId(Integer nftId);
    NFTTransactionDto selectNFTTransactionById(Integer Id);
    List<NFTTransactionDto> selectNFTTransactionByFromId(Integer fromID);
    List<NFTTransactionDto> selectNFTTransactionByToId(Integer toID);
    List<NFTTransactionDto> selectNFTCondition(NFTTransactionDto dto);
    NFTTransactionDto selectOrderByTime(Integer nftId);
    boolean buyApplyForNFT(NFTPendingApplication application);
    List<NFTPendingApplication> getApplyForNFT(HttpServletRequest request);


}
