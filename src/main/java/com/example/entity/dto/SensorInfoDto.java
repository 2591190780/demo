package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sensor_info")
@AllArgsConstructor
public class SensorInfoDto {
    // * 传感器ID（主键）
    @TableId(type = IdType.AUTO)
    private Integer sensorId;
    // * 传感器安装在哪个农场下
    private Integer farmId;
    // * 传感器类型 温度 湿度 土壤
    private String type;
    // * 传感器在农场安装位置
    private String location;
    // * 传感器是否激活
    private  Boolean is_active;
    // * 传感创建时间
    private LocalDateTime create_time;
    // * 传感器更新时间
    private LocalDateTime update_time;

}
