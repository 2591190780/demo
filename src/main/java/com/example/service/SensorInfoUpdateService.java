package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.RestBean;
import com.example.entity.dto.SensorInfoDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface SensorInfoUpdateService extends IService<SensorInfoDto> {

     <T> RestBean<T> updateSensorInfoDto(HttpServletRequest request, SensorInfoDto dto);
     <T>RestBean<T> updateSensorInfoDtoadmin(HttpServletRequest request, SensorInfoDto dto);
     <T> RestBean<T> updateAllSensorInfo (HttpServletRequest request,List<SensorInfoDto> dto);
      <T> RestBean<T> updateAllSensorInfoAdmin (HttpServletRequest request,List<SensorInfoDto> dto);
}
