package com.example.mapper.NFT;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.NFTTransactionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NFTTransactionMapper extends BaseMapper<NFTTransactionDto> {

    @Select("SELECT * FROM nft_transaction")
    Page<NFTTransactionDto> selectPage(
            Page< NFTTransactionDto > page);
}
