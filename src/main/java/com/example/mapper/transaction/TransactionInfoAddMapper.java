package com.example.mapper.transaction;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.TransactionAccountDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionInfoAddMapper extends BaseMapper<TransactionAccountDto>{
}
