package com.example.entity.dto;


import com.baomidou.mybatisplus.annotation.IdType;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("nft_reward_rule")
@AllArgsConstructor
public class NFTRuleDto {
    @TableId(type = IdType.AUTO)  // 自增主键
    private Integer ruleId;
    private String name;
    private String ruleDescription;
    private Integer templateId;
    private String conditionValue;
    private Integer validityPeriod;
    private int isActive;
    private LocalDateTime createdAt;
    private int tableName;
    private LocalDateTime conditionStartTime;
    private LocalDateTime conditionEndTime;
    private String applyHash;
    private LocalDateTime updateTime;


}
