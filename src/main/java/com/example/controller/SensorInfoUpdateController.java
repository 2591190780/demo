package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.SensorInfoDto;
import com.example.service.SensorInfoUpdateService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/AUSensor")
public class SensorInfoUpdateController {

    @Resource
    SensorInfoUpdateService service;

    @PutMapping("/updateSingle")
    public RestBean<Void> updateSensorSingle(HttpServletRequest request, @RequestBody SensorInfoDto dto) {
        return service.updateSensorInfoDto(request,dto);
    }
}
