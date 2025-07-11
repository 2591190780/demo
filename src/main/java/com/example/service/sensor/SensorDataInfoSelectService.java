package com.example.service.sensor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.SensorDataInfoDto;
import com.example.entity.dto.SensorInfoDto;

import java.util.List;

public interface SensorDataInfoSelectService extends IService<SensorDataInfoDto> {
    SensorDataInfoDto getSensorDataInfoBySensorId(Integer id);
    List<SensorDataInfoDto> findSensorInfoByFarmerId(Integer id);
    List<SensorDataInfoDto> findSensorInfoByTypeId(String id);
    List<SensorDataInfoDto> findByCondition(SensorInfoDto dto);
}
