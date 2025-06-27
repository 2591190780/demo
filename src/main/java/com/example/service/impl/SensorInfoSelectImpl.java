package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.SensorInfoDto;
import com.example.mapper.SensorInfoMapper;
import com.example.service.SensorInfoSelectService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SensorInfoSelectImpl extends ServiceImpl<SensorInfoMapper, SensorInfoDto>
        implements SensorInfoSelectService {
    //根据传感器id查询
    @Override
    public SensorInfoDto getSensorInfoBySensorId(Integer id){
        return this.findSensorInfoBySensorId(id);
    }

    @Override
    public List<SensorInfoDto> getSensorInfoByFarmerId(Integer id){
        return this.findSensorInfoByFarmerId(id);
    }

    @Override
    public List<SensorInfoDto> getSensorInfoByTypeId(String id){
        return this.findSensorInfoByTypeId(id);
    }

    @Override
    public List<SensorInfoDto> getSensorInfoByText(Integer sensorid,Integer farmid,String typeid,String location){
        if (sensorid == null && farmid == null &&
                typeid == null && location == null) {
            return Collections.emptyList();
        }
        QueryWrapper<SensorInfoDto> queryWrapper = new QueryWrapper<>();
        if (sensorid != null) {
            queryWrapper.eq("sensor_id", sensorid);
        }

        if (farmid != null) {
            queryWrapper.eq("farm_id", farmid);
        }

        if (typeid != null && !typeid.trim().isEmpty()) {
            queryWrapper.eq("type", typeid.trim());
        }

        if (location != null && !location.trim().isEmpty()) {
            queryWrapper.like("location", location.trim());

        }
        return (list(queryWrapper));
    }




    private SensorInfoDto findSensorInfoBySensorId(Integer id){
        if(id==null)return null;
        return query()
                .eq("sensor_id", id)
                .one();
    }


    private List<SensorInfoDto> findSensorInfoByFarmerId(Integer id){
        if(id==null) return Collections.emptyList();
            return query()
                    .eq("farm_id", id)
                    .list();

    }

    private List<SensorInfoDto> findSensorInfoByTypeId(String id){
        if(id==null) return Collections.emptyList();
        return query()
                .eq("type", id)
                .list();

    }



    }








