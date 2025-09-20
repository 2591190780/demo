package com.example.mapper.NFT;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dto.TransactionAccountDto;
import com.example.entity.dto.UserNFTDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

@Mapper
public interface UserNFTMapper extends BaseMapper<UserNFTDto> {
    @Select("SELECT * FROM user_nft WHERE user_id = #{userID}")
    Page<UserNFTDto> selectByOwnerId(
            Page<TransactionAccountDto> page, @Param("userID") Integer userID);

    @Select("SELECT * FROM user_nft ")
    Page<UserNFTDto> selectALL(
            Page<TransactionAccountDto> page);
}
