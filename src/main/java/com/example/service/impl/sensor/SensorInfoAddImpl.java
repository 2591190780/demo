package com.example.service.impl.sensor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.SensorInfoDto;
import com.example.mapper.sensor.SensorInfoMapper;
import com.example.service.sensor.SensorInfoAddService;
import com.example.utils.InfoToRedisUtils;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SensorInfoAddImpl extends ServiceImpl<SensorInfoMapper, SensorInfoDto>
        implements SensorInfoAddService {

     @Resource
     JwtUtils jwtUtils;
    @Autowired
    private InfoToRedisUtils infoToRedisUtils;

     @Override
     public <T> RestBean<T> addSensorInfoDto(HttpServletRequest request,SensorInfoDto dto) {
          /**
           *
           * 1.获取前端传入的参数--> request （farmerid(token),role）--> vo
           * 2.验证数据的有效性 // 数据
           * return RestBean.failure(401,"  ")
           * return RestBean.failure(401,"")
           * 3.add逻辑  query.add().one() --> private
           * 4.return RestBean.success()
           */
          if(!this.SensorFarmerIdVerify(request))return RestBean.failure(401,"权限不足");
          String authorization = request.getHeader("Authorization");
          DecodedJWT jwt = jwtUtils.resolveJWT(authorization);
          Integer id = jwtUtils.toId(jwt);
          dto.setFarmId(id);
          dto.setCreate_time(LocalDateTime.now());
          dto.setIs_active((byte) 0);
          if(this.save(dto)){
               this.infoToRedisUtils.InfoToRedis(dto.getSensorId(),id,"add","sensor");
               return RestBean.success();
          }
          return RestBean.failure(500,"内部错误请联系管理员");
     }


     private  boolean SensorFarmerIdVerify (HttpServletRequest request) {
          String authorization = request.getHeader("Authorization");
          DecodedJWT jwt = jwtUtils.resolveJWT(authorization);
          String role = jwtUtils.toRole(jwt);
         return !role.equals("2");
     }


}
