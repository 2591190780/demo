package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.example.handler.JsonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.sql.Timestamp;
import java.time.LocalTime;

@Data
@TableName("log_info")
@AllArgsConstructor
public class LogInfoDto {
    // * 日志ID（主键）
    @TableId(type = IdType.AUTO)  // 自增主键
    private Integer logId;
    // * userID（外键）
    private Integer userId;
    // 操作名称
    private String operationType;
    //产品id
    private Integer productId;
    //操作前数据
    @TableField(typeHandler = JsonTypeHandler.class)
    private Object beforeSnapshot;
    //操作后数据
    @TableField(typeHandler = JsonTypeHandler.class)
    private Object afterSnapshot;
    //区块链交易hash(66字符)
    @Length(min=66,max = 66)
    String blockchainTxHash;
    // 时间
    private LocalTime createTime;
    /**
     * 错误信息（操作失败时记录）
     */
    private String errorMessage;
    /**
     * 操作状态（1/0）
     */
    private String status;
    /**
     * 客户端IP地址
     */
    private String clientIp;
}
