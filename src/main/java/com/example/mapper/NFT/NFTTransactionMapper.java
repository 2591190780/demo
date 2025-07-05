package com.example.mapper.NFT;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.NFTTransactionDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NFTTransactionMapper extends BaseMapper<NFTTransactionDto> {
}
