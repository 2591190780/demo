package com.example.entity.dto;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.*;
import com.example.handler.JsonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("log_info")
@AllArgsConstructor
public class LogInfoDto {
    @TableId(type = IdType.AUTO)
    private Integer logId;
    private Integer userId;
    private String operationType;

    private LocalDateTime createTime;
    private String errorMessage;
    private String status; // 使用String类型存储状态
    private String clientIp;
    private String responseContent;

    public LogInfoDto() {

    }

}
