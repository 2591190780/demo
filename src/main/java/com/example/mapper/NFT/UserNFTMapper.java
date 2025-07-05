package com.example.mapper.NFT;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.UserNFTDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserNFTMapper extends BaseMapper<UserNFTDto> {
}
