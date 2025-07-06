package com.example.controller.sensor;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.dto.SensorInfoDto;
import com.example.service.sensor.SensorInfoUpdateService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/AUSensor")
public class SensorInfoUpdateController {

    @Resource
    SensorInfoUpdateService service;

    @Auditable(
            operationType = "UPDATE_SINGLE_SENSOR",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/updateSingle")
    public RestBean<Void> updateSensorSingle(HttpServletRequest request, @RequestBody SensorInfoDto dto) {
        return service.updateSensorInfoDto(request,dto);
    }

    @Auditable(
            operationType = "UPDATE_MULTI_SENSOR",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/updateMutil")
    public RestBean<Void> updateSensorMutil(HttpServletRequest request, @RequestBody List<SensorInfoDto> dto) {
        return service.updateAllSensorInfo(request,dto);
    }

}
