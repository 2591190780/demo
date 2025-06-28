package com.example.service.impl.sensor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;

import com.example.entity.dto.SensorInfoDto;
import com.example.mapper.sensor.SensorInfoMapper;
import com.example.service.sensor.SensorInfoUpdateService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.example.service.impl.product.ProductInfoAddAccountImpl.userIdVerify;

@Service
public class SensorInfoUpdateImpl extends ServiceImpl<SensorInfoMapper, SensorInfoDto>
        implements SensorInfoUpdateService {

    @Resource
    JwtUtils jwtUtils;

    //修改单个传感器信息
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

    //激活
    @Override
    public <T>RestBean<T> updateSensorInfoDtoadmin(HttpServletRequest request, SensorInfoDto dto){
        if (!roleVerifyAdmin(request)) return RestBean.failure(401,"权限不足");
        if(updateAdmin(dto)) return RestBean.success();
        return  RestBean.failure(500,"参数有误");
    }

    //批量修改传感器信息
    @Override
    public <T> RestBean<T> updateAllSensorInfo (HttpServletRequest request,List<SensorInfoDto> dto){
        if (!jwtUtils.userRoleVerify(request)) {
            return RestBean.forbidden("只有农户才能修改处传感器的位置");
        }
        try {
            boolean allSuccess = true;
            for (SensorInfoDto sensordto : dto) {
                Integer fid = sensordto.getFarmId();
                if(!getUserIdVerify(request,fid))
                    return  RestBean.failure(500,"无权限的操作，请检查传感器编号");
                // 对每个产品执行更新
                String authorization = request.getHeader("Authorization");
                Integer userid = jwtUtils.toId(jwtUtils.resolveJWT(authorization));
                if (!update(sensordto,userid)) {
                    allSuccess = false;
                    // 记录失败日志（实际生产环境应更详细）
                    log.error(String.format("更新传感器，传感器ID:%s", sensordto.getSensorId()));
                }
            }
            return allSuccess ?
                    RestBean.success() :
                    RestBean.failure(400, "部分传感器更新失败，请检查数据");
        } catch (Exception e) {
            log.error("批量更新传感器异常", e);
            return RestBean.failure(500, "批量更新失败: " + e.getMessage());
        }

    }
    @Override
    public <T> RestBean<T> updateAllSensorInfoAdmin (HttpServletRequest request,List<SensorInfoDto> dto){
        if (!jwtUtils.userRoleVerifyAdmin(request)) {
            return RestBean.forbidden("权限不足");
        }
        try {
            boolean allSuccess = true;
            for (SensorInfoDto sensordto : dto) {
                Integer fid = sensordto.getFarmId();
                if(!getUserIdVerify(request,fid))
                    return  RestBean.failure(500,"无权限的操作，请检查传感器编号");
                // 对每个产品执行更新
                if (!updateAdmin(sensordto)) {
                    allSuccess = false;
                    // 记录失败日志（实际生产环境应更详细）
                    log.error(String.format("更新传感器，传感器ID:%s", sensordto.getSensorId()));
                }
            }
            return allSuccess ?
                    RestBean.success() :
                    RestBean.failure(400, "部分传感器更新失败，请检查数据");
        } catch (Exception e) {
            log.error("批量更新传感器异常", e);
            return RestBean.failure(500, "批量更新失败: " + e.getMessage());
        }

    }

    //传感器激活审核
    private  boolean roleVerify (HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        DecodedJWT jwt = jwtUtils.resolveJWT(authorization);
        String role = jwtUtils.toRole(jwt);
        return role.equals("1");
    }
    private  boolean roleVerifyAdmin (HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        DecodedJWT jwt = jwtUtils.resolveJWT(authorization);
        String role = jwtUtils.toRole(jwt);
        return role.equals("3");
    }
    private Boolean getUserIdVerify(HttpServletRequest request,Integer fid){
        return userIdVerify(request, fid, jwtUtils);
    }

//更新函数方法
private boolean update(SensorInfoDto dto,Integer userId){
    Integer SensorID=dto.getSensorId();
    String Location=dto.getLocation();
    return  this.update()
            .eq("sensor_id",SensorID)
            .eq("farm_id",userId)
            .set("location",Location)
            .set("is_active",0)
            .update();
}
//审核函数方法
    private boolean updateAdmin(SensorInfoDto dto){
        Integer SensorID=dto.getSensorId();
        Integer userId = dto.getFarmId();
        Byte is_active=dto.getIs_active();
        return  this.update()
                .eq("sensor_id",SensorID)
                .eq("farm_id",userId)
                .set("is_active",is_active)
                .update();
    }

}
