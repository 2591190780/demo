package com.example.service.impl.sensor;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.SensorDataInfoDto;
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

}
