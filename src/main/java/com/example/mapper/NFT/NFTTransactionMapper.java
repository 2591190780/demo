package com.example.mapper.NFT;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.NFTTransactionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

@Mapper
public interface NFTTransactionMapper extends BaseMapper<NFTTransactionDto> {

    @Select("SELECT * FROM nft_transaction ORDER BY tx_time DESC")
    Page<NFTTransactionDto> selectPage(
            Page< NFTTransactionDto > page);


    @Select("SELECT COUNT(*) FROM nft_transaction WHERE from_user = #{fid} AND active=1 ")
    Integer countNumByFromID(Integer fid);
}
