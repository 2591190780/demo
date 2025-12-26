package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.BlockChainEvidenceDto;

import java.util.List;

public interface BlockChainEvidenceService extends IService<BlockChainEvidenceDto> {
    boolean addInfo(Integer dataType,Integer relatedId,String hash);
    List<BlockChainEvidenceDto> selectInfoCondition(BlockChainEvidenceDto params);
    BlockChainEvidenceDto selectInfoByTxHash(String txHash);
    List<BlockChainEvidenceDto> selectInfoByRelateId(Integer id);
    boolean updateInfoById(Integer id , Integer block,String txHash);
    BlockChainEvidenceDto selectInfoBySubmitHash(String submitHash);
    boolean updateInfoBySubmitHash(String block,String txHash,String submitHash);
}
