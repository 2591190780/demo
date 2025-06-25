package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.RestBean;
import com.example.entity.dto.SensorInfoDto;
import jakarta.servlet.http.HttpServletRequest;

public interface SensorInfoUpdateService extends IService<SensorInfoDto> {

     <T> RestBean<T> updateSensorInfoDto(HttpServletRequest request, SensorInfoDto dto);
}
