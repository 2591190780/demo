package com.example.controller.sensor;

import com.example.entity.RestBean;
import com.example.entity.dto.SensorInfoDto;
import com.example.service.sensor.SensorInfoAddService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/AUSensor")
@Tag(name="传感器信息修改请求",description = "相关操作")
public class SensorInfoAddController {

    @Resource
    SensorInfoAddService service;

    @PutMapping("/addSingle")
    public RestBean<Void> addSensorSingle(HttpServletRequest request,@RequestBody SensorInfoDto dto) {
        return service.addSensorInfoDto(request,dto);
    }

}
