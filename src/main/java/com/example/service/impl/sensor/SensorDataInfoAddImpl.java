package com.example.service.impl.sensor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.SensorDataInfoDto;
import com.example.entity.dto.SensorInfoDto;
import com.example.mapper.sensor.SensorDataInfoMapper;
import com.example.service.sensor.SensorDataInfoAddService;
import com.example.service.sensor.SensorInfoSelectService;
import com.example.utils.BlockchainHashUtil;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorDataInfoAddImpl extends ServiceImpl<SensorDataInfoMapper, SensorDataInfoDto>
        implements SensorDataInfoAddService {
    @Resource
    JwtUtils jwtUtils;

    @Resource
    BlockchainHashUtil blockchainHashUtil;

    @Resource
    SensorInfoSelectService selectService;

    @Override
    public <T> RestBean<T> addSensorDataInfoSingle(HttpServletRequest request, SensorDataInfoDto dto) {

        if(!this.SensorFarmerIdVerify(request))return RestBean.failure(401,"权限不足");
        SensorInfoDto infoDto = selectService.getSensorInfoBySensorId(dto.getSensorId());
        Integer fid = infoDto.getFarmId();
        if(fid==null) return RestBean.failure(500, "没有该编号的传感器");
        if (!jwtUtils.getUserIdVerify(request,fid)) return RestBean.failure(500, "不准增加其他农户传感器信息");
        dto.setBlockchain_hash(blockchainHashUtil.generateSensorHash(dto));
            return this.save(dto) ? RestBean.success() : RestBean.failure(500, "内部错误请联系管理员");
    }

    @Override
    public <T> RestBean<T> addSensorDataInfoMutil(HttpServletRequest request, List<SensorDataInfoDto> dto) {
        if(!this.SensorFarmerIdVerify(request))return RestBean.failure(401,"权限不足");
        for (SensorDataInfoDto sensorData : dto) {
            SensorInfoDto infoDto = selectService.getSensorInfoBySensorId(sensorData.getSensorId());
            Integer fid = infoDto.getFarmId();
            if(fid==null) return RestBean.failure(500, "没有该编号的传感器");
            if (!jwtUtils.getUserIdVerify(request,fid)) return RestBean.failure(500, "不准增加其他农户传感器信息");
            sensorData.setBlockchain_hash(blockchainHashUtil.generateSensorHash(sensorData));
            if (!save(sensorData)) return RestBean.failure(500, "添加有误，操作终止");
        }
        return RestBean.success();
    }





    private  boolean SensorFarmerIdVerify (HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        DecodedJWT jwt = jwtUtils.resolveJWT(authorization);
        String role = jwtUtils.toRole(jwt);
        return !role.equals("2");
    }

}
