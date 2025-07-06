package com.example.controller.sensor;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.dto.SensorDataInfoDto;
import com.example.service.sensor.SensorDataInfoAddService;
import com.example.service.sensor.SensorInfoSelectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/addSensorData")
@Tag(name="传感器信息添加请求",description = "相关操作")
public class SensorDataInfoAddController {
    @Resource
    SensorDataInfoAddService service;

    @Auditable(
            operationType = "ADD_DATA_SINGLE_SENSOR_DATA",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/addDataSingle")
    public RestBean<Void> addSensorDataInfoSingle(HttpServletRequest request, @RequestBody @Valid SensorDataInfoDto dto){
        return service.addSensorDataInfoSingle(request,dto);

    }

    @Auditable(
            operationType = "ADD_DATA_MULTI_SENSOR",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/addDataMutil")
    public RestBean<Void> addSensorDataInfoMutil(HttpServletRequest request, @RequestBody @Valid List<SensorDataInfoDto> dto){
        return service.addSensorDataInfoMutil(request,dto);

    }

}
