package com.example.entity.dto;


import com.baomidou.mybatisplus.annotation.IdType;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

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

    @Length(min = 66, max = 66)
    private String applyHash;
    private LocalDateTime updateTime;
    private LocalDateTime passActive;

}
