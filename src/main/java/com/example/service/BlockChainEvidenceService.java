package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.BlockChainEvidenceDto;

import java.util.List;

public interface BlockChainEvidenceService extends IService<BlockChainEvidenceDto> {
    boolean addInfo(Integer dataType,Integer relatedId,String hash);
    List<BlockChainEvidenceDto> selectInfoCondition(BlockChainEvidenceDto params);
    BlockChainEvidenceDto selectInfoByHash(String hash);
    boolean updateInfo(String block,String hash);
}
