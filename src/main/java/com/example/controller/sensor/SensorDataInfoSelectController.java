package com.example.controller.sensor;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.dto.SensorDataInfoDto;
import com.example.service.sensor.SensorDataInfoSelectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/selectSensorData")
@Tag(name="传感器信息搜索",description = "相关操作")


public class SensorDataInfoSelectController {
    @Resource
    SensorDataInfoSelectService service;

    @Auditable(
            operationType = "SEARCH_ID_SINGLE_SENSOR_DATA",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/searchIdSingle")
    public RestBean<Void> searchSensorDataInfoBySensorId(@RequestParam @Valid  String sensorId
            , HttpServletResponse response)throws IOException {
        SensorDataInfoDto dto = service.getSensorDataInfoBySensorId(this.convertToInteger(sensorId));
        response.setContentType("application/json;Charset=utf-8");
        if(dto!=null) {
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return null;
        }
        return RestBean.failure(404, "没有该id传感器的信息。");
    }

    @Auditable(
            operationType = "SEARCH_FARMER_ID_SENSOR_DATA",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/searchFarmerID")
    public RestBean<Void> searchSensorDataInfoByFarmerID(@RequestParam @Valid  String fid
            , HttpServletResponse response)throws IOException {
        List<SensorDataInfoDto> dto = service.findSensorInfoByFarmerId(this.convertToInteger(fid));
        response.setContentType("application/json;Charset=utf-8");
        if(dto!=null) {
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return null;
        }
        return RestBean.failure(404, "没有该农户的传感器信息。");
    }


    @Auditable(
            operationType = "SEARCH_TYPE_ID_SENSOR_DATA",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/searchTypeID")
    public RestBean<Void> searchSensorDataInfoByTypeID(@RequestParam @Valid  String tid
            , HttpServletResponse response)throws IOException {
        List<SensorDataInfoDto> dto = service.findSensorInfoByTypeId((tid));
        response.setContentType("application/json;Charset=utf-8");
        if(dto!=null) {
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return null;
        }
        return RestBean.failure(404, "没有该类型的传感器信息。");
    }


    @Auditable(
            operationType = "SEARCH_CONDITION_SENSOR_DATA",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/searchByCondition")
    public RestBean<Void> searchSensorDataInfoByCondition(@RequestBody SensorDataInfoDto dto,
                                                          HttpServletResponse response)throws IOException {
        List<SensorDataInfoDto> dtoList = service.findByCondition(dto);
        if(dtoList!=null) {
             response.getWriter().write(RestBean.success(dtoList).asJsonString());
             return null;
        }
        return RestBean.failure(401,"暂无该传感器信息。");
    }

    private Integer convertToInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null; // 或者记录日志
        }
    }
}






















