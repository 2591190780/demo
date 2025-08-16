package com.example.mapper.NFT;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.NFTInfoDto;
import com.example.entity.dto.TransactionAccountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

@Mapper
public interface NFTInfoMapper extends BaseMapper<NFTInfoDto> {

    @Select("SELECT * FROM nft_template")
    Page<NFTInfoDto> selectPage(
            Page<NFTInfoDto> page);
}
