package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.ProductUpdateVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductInfoUpdateAccountMapper extends BaseMapper<ProductUpdateVO> {
}
