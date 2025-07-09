package com.example.service.impl.sensor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.SensorDataInfoDto;
import com.example.entity.dto.SensorInfoDto;
import com.example.mapper.sensor.SensorDataInfoMapper;
import com.example.service.sensor.SensorDataInfoSelectService;
import org.bouncycastle.pqc.crypto.newhope.NHOtherInfoGenerator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Service
public class SensorDataInfoSelectImpl extends ServiceImpl<SensorDataInfoMapper, SensorDataInfoDto>
        implements SensorDataInfoSelectService {


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
        return query()
                .eq("farm_id", id)
                .list();
    }

    @Override
    public List<SensorDataInfoDto> findSensorInfoByTypeId(String id){
        if(id==null) return Collections.emptyList();
        return query()
                .eq("type", id)
                .list();
    }

    @Override
    public List<SensorDataInfoDto> findByCondition(SensorDataInfoDto dto){
        if(dto==null) return Collections.emptyList();
        QueryWrapper<SensorDataInfoDto> queryWrapper = new QueryWrapper<>();
        if (dto.getSensorId() != null) {
            queryWrapper.eq("sensor_id", dto.getSensorId());
        }

        if (dto.getDataId()!= null) {
            queryWrapper.eq("farm_id", dto.getDataId());
        }

        if (dto.getBlockchain_hash() != null) {
            queryWrapper.eq("type", dto.getBlockchain_hash());
        }
        return (list(queryWrapper));
    }



}
