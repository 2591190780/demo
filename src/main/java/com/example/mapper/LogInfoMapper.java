package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.LogInfoDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogInfoMapper extends BaseMapper<LogInfoDto> {
}
