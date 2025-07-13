package com.example.service.sensor;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.RestBean;
import com.example.entity.dto.SensorDataInfoDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface SensorDataInfoAddService extends IService<SensorDataInfoDto> {
    <T> RestBean<T> addSensorDataInfoSingle(HttpServletRequest request, SensorDataInfoDto dto) throws Exception;
    <T> RestBean<T> addSensorDataInfoMutil(HttpServletRequest request, List<SensorDataInfoDto> dto);
}
