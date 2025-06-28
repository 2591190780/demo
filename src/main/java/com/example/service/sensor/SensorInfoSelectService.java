package com.example.service.sensor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.SensorInfoDto;

import java.util.List;

public interface SensorInfoSelectService extends IService <SensorInfoDto> {

   SensorInfoDto getSensorInfoBySensorId(Integer id);
   List<SensorInfoDto> getSensorInfoByFarmerId(Integer id);
   List<SensorInfoDto> getSensorInfoByTypeId(String id);
   List<SensorInfoDto> getSensorInfoByText(Integer sensorid,Integer farmid,String typeid,String location);
}
