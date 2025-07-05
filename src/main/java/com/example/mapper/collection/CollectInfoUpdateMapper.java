package com.example.mapper.collection;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.vo.request.CollectUpdateVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CollectInfoUpdateMapper extends BaseMapper<CollectUpdateVO> {
}
