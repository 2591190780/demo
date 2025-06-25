package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.RestBean;
import com.example.entity.dto.SensorInfoDto;
import jakarta.servlet.http.HttpServletRequest;

public interface SensorInfoAddService extends IService<SensorInfoDto> {
  <T> RestBean<T> addSensorInfoDto(HttpServletRequest request,SensorInfoDto dto);
}
