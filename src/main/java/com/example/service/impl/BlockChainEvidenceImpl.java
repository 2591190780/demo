package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.BlockChainEvidenceDto;
import com.example.mapper.BlockChainEvidenceMapper;
import com.example.service.BlockChainEvidenceService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BlockChainEvidenceImpl extends ServiceImpl<BlockChainEvidenceMapper, BlockChainEvidenceDto>
        implements BlockChainEvidenceService {

    @Override
    public boolean addInfo(Integer dataType,Integer relatedId,String hash){
        return this.save(
                new BlockChainEvidenceDto(
                        null,dataType,relatedId,null,hash,null,null
                )
        );
    }

    @Override
    public List<BlockChainEvidenceDto> selectInfoCondition(BlockChainEvidenceDto params){
        QueryWrapper<BlockChainEvidenceDto> queryWrapper = new QueryWrapper<>();
        // 精确匹配条件
        if (params.getEvidenceId() != null) {
            queryWrapper.eq("evidence_id", params.getEvidenceId());
        }
        if (params.getDataType() != null) {
            queryWrapper.eq("data_type", params.getDataType());
        }
        if (params.getRelatedId() != null) {
            queryWrapper.eq("related_id", params.getRelatedId());
        }
        if (params.getTxHash() != null) {
            queryWrapper.eq("tx_hash", params.getTxHash());
        }
        if (params.getBolckNumber() != null) {
            queryWrapper.eq("block_number", params.getBolckNumber());
        }
        return this.list(queryWrapper);
    }

    @Override
    public BlockChainEvidenceDto selectInfoByTxHash(String hash){

        return this.query().eq("tx_hash", hash).one();
    }

    @Override
    public boolean updateInfoById(Integer id , Integer block,String txHash){
        return this.update().eq("evidence_id",id).set("block_number",block)
                .set("timestamp", LocalDateTime.now()).set("tx_hash",txHash).update();
    }

    @Override
    public boolean updateInfoBySubmitHash(String block,String txHash,String submitHash){
        return this.update().eq("submit_hash",submitHash).set("block_number",block)
                .set("timestamp", LocalDateTime.now()).set("tx_hash",txHash).update();
    }


    @Override
    public BlockChainEvidenceDto selectInfoBySubmitHash(String submitHash){
        return this.query().eq("submit_hash", submitHash).one();
    }



}
