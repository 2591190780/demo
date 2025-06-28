package com.example.mapper.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.ProductInfoAccountDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductInfoUpdateAccountMapper extends BaseMapper<ProductInfoAccountDto> {
}
