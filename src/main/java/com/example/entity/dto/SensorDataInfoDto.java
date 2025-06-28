package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@TableName("sensor_datainfo")
@AllArgsConstructor
public class SensorDataInfoDto {
    // * 传感器数据量（主键）
    @TableId(type = IdType.AUTO)
    private BigInteger dataId;
    // * 传感器ID（外键）
    private Integer sensorId;
    // * 传感器数值
    private BigDecimal value;
    // * 采集数据时间
    private LocalDateTime timestamp;
    // * 区块链上报数据
    @Length(min = 66, max = 66)
    private String blockchain_hash;




}
