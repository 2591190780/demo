package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.SensorInfoDto;
import com.example.mapper.SensorInfoMapper;
import com.example.service.SensorInfoSelectService;
import org.springframework.stereotype.Service;

@Service
public class SensorInfoSelectImpl extends ServiceImpl<SensorInfoMapper
        , SensorInfoDto> implements SensorInfoSelectService {
}
