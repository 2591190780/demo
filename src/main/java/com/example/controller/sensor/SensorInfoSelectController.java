package com.example.controller.sensor;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.dto.SensorInfoDto;
import com.example.service.sensor.SensorInfoSelectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/AUSensor")
@Tag(name="传感器信息查找请求",description = "相关操作")
public class SensorInfoSelectController {
    @Resource
    SensorInfoSelectService service;

    @Auditable(
            operationType = "SELECT_SINGLE_SENSOR_ID",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/selectSingle")
    public RestBean<Void> selectSensorInfoBySensorId( @RequestParam @Valid String id
    , HttpServletResponse response) throws IOException {

        SensorInfoDto dto=service.getSensorInfoBySensorId(this.convertToInteger(id));
        response.setContentType("application/json;Charset=utf-8");
        if(dto!=null) {
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return null;
        }
        return RestBean.failure(404, "没有该id的传感器");
    }

    @Auditable(
            operationType = "SELECT_MULTI_SENSOR_FARMER_ID",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping ("/selectMultiFarm")
    public RestBean<Void> selectSensorInfoByFarmerId( @RequestParam @Valid String id
            , HttpServletResponse response) throws IOException{
        List<SensorInfoDto> dto=service.getSensorInfoByFarmerId(this.convertToInteger(id));
        response.setContentType("application/json;Charset=utf-8");
        if(dto!=null) {
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return null;
        }
        return RestBean.failure(404, "该农户没有传感器");
    }

    @Auditable(
            operationType = "SELECT_MULTI_TYPE_SENSOR",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping ("/selectMultiType")
    public RestBean<Void> selectSensorInfoByTypeId( @RequestParam @Valid String id
            , HttpServletResponse response) throws IOException{
        List<SensorInfoDto> dto=service.getSensorInfoByTypeId(id);
        response.setContentType("application/json;Charset=utf-8");
        if(dto!=null) {
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return null;
        }
        return RestBean.failure(404, "没有该类型的传感器");
    }

    @Auditable(
            operationType = "SELECT_MULTI_TEXT_SENSOR",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping ("/selectMultiText")
    public RestBean<Void> selectSensorInfoByText(   @RequestParam String sensorId
            ,    @RequestParam String farmId
            ,  @RequestParam String type
            , @RequestParam String location
            , HttpServletResponse response) throws IOException{

        List<SensorInfoDto> dto=service.getSensorInfoByText(
                this.convertToInteger(sensorId),
                this.convertToInteger(farmId)
                ,type,location);
        response.setContentType("application/json;Charset=utf-8");
        if(dto!=null) {
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return null;
        }
        return RestBean.failure(404, "搜索有误");
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
