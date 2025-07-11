package com.example.service.impl.sensor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.SensorDataInfoDto;
import com.example.entity.dto.SensorInfoDto;
import com.example.mapper.sensor.SensorDataInfoMapper;
import com.example.service.sensor.SensorDataInfoSelectService;
import com.example.service.sensor.SensorInfoSelectService;
import jakarta.annotation.Resource;
import org.bouncycastle.pqc.crypto.newhope.NHOtherInfoGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Service
public class SensorDataInfoSelectImpl extends ServiceImpl<SensorDataInfoMapper, SensorDataInfoDto>
        implements SensorDataInfoSelectService {

    @Resource
    SensorInfoSelectService sensorInfoSelectService;


    @Override
   public SensorDataInfoDto getSensorDataInfoBySensorId(Integer id){
        if(id==null)return null;
        return query()
                .eq("sensor_id", id)
                .orderByDesc("create_time")
                .last("LIMIT 1").one();
    }

    @Override
    public List<SensorDataInfoDto> findSensorInfoByFarmerId(Integer id){
        if(id==null) return Collections.emptyList();
        List<SensorInfoDto> dtoList = this.sensorInfoSelectService.getSensorInfoByFarmerId(id);
        List<SensorDataInfoDto> sensorDataInfoDtoList =new ArrayList<>();
        if(dtoList == null || dtoList.isEmpty()){ return null;}
        for (SensorInfoDto dto : dtoList) {
            sensorDataInfoDtoList.add(this.getSensorDataInfoBySensorId(dto.getSensorId()));
        }
        return sensorDataInfoDtoList;
    }


    @Override
    public List<SensorDataInfoDto> findSensorInfoByTypeId(String id){
        if(id==null) return Collections.emptyList();
        List<SensorInfoDto> dtoList = this.sensorInfoSelectService.getSensorInfoByTypeId(id);
        List<SensorDataInfoDto> sensorDataInfoDtoList =new ArrayList<>();
        if(dtoList == null || dtoList.isEmpty()){ return null;}
        for (SensorInfoDto dto : dtoList) {
            sensorDataInfoDtoList.add(this.getSensorDataInfoBySensorId(dto.getSensorId()));
        }
        return sensorDataInfoDtoList;
    }

    @Override
    public List<SensorDataInfoDto> findByCondition(SensorInfoDto dto){
        if(dto==null) return Collections.emptyList();
        QueryWrapper<SensorDataInfoDto> queryWrapper = new QueryWrapper<>();
        if (dto.getSensorId() != null) {
            queryWrapper.eq("sensor_id", dto.getSensorId());
        }
        if (queryWrapper.orderByDesc("create_time")==null) { return null;}
        List<SensorDataInfoDto> dtoList = new ArrayList<>();
        for (SensorDataInfoDto sensorDataInfoDto : this.list(queryWrapper)) {
            if(Objects.equals(
                    this.sensorInfoSelectService.getSensorInfoBySensorId
                            (sensorDataInfoDto.getSensorId()).getFarmId()
                    , dto.getFarmId()) &&
                    Objects.equals(
                            this.sensorInfoSelectService.getSensorInfoBySensorId
                                    (sensorDataInfoDto.getSensorId()).getType()
                            , dto.getType())
            )
            {
                dtoList.add(sensorDataInfoDto);
            }
        }
        return dtoList;

    }



}
