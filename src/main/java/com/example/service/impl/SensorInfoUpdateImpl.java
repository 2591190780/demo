package com.example.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;

import com.example.entity.dto.SensorInfoDto;
import com.example.mapper.SensorInfoMapper;
import com.example.service.SensorInfoUpdateService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SensorInfoUpdateImpl extends ServiceImpl<SensorInfoMapper, SensorInfoDto>
        implements SensorInfoUpdateService {

    @Resource
    JwtUtils jwtUtils;

    @Override
    public <T>RestBean<T> updateSensorInfoDto(HttpServletRequest request, SensorInfoDto dto){
        if (!roleVerify(request)) return RestBean.failure(401,"权限不足");
        String authorization = request.getHeader("Authorization");
        DecodedJWT jwt = jwtUtils.resolveJWT(authorization);
        Integer userId = jwtUtils.toId(jwt);

        if(!Objects.equals(dto.getFarmId(), userId) ){
            if (Objects.equals(jwtUtils.toRole(jwt), "3")){
                return this.update(dto,userId) ? RestBean.success():RestBean.failure(500,"内部错误请联系管理员");
            }
            return RestBean.failure(500,"权限不足");
        }
        return this.update(dto,userId) ? RestBean.success():RestBean.failure(500,"内部错误请联系管理员");

    }


    private  boolean roleVerify (HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        DecodedJWT jwt = jwtUtils.resolveJWT(authorization);
        String role = jwtUtils.toRole(jwt);
        return !role.equals("2");
    }



private boolean update(SensorInfoDto dto,Integer userId){
    Integer SensorID=dto.getSensorId();
    String Type=dto.getType();
    String Location=dto.getLocation();
    Boolean Is_active=dto.getIs_active();

    return  this.update()
            .eq("sensor_id",SensorID)
            .eq("farm_id",userId)
            .set("type",Type)
            .set("location",Location)
            .set("is_active",Is_active)
            .update();
}

}
