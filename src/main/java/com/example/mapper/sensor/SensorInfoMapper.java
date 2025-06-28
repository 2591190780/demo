package com.example.mapper.sensor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.SensorInfoDto;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SensorInfoMapper extends BaseMapper<SensorInfoDto> {
}
