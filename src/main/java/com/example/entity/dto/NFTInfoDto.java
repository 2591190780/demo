package com.example.entity.dto;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EntityScan
@Data
@TableName("nft_template")
@AllArgsConstructor
public class NFTInfoDto {

    @Setter
    @Getter
    @TableId(type = IdType.AUTO)  // 自增主键
    private Integer templateId;
    private String name;
    private String description;
    private String imageUrl;

    @Length(min = 42, max = 42)
    private String contractAddress;

    private Integer issuanceLimit;
    private Integer remainCount;


    private Integer isActive;
    private LocalDateTime createdAt;


    private String metadataUrl;

    private String nftLevel;
    private LocalDateTime updateTime;
    private Integer publicBy;

    public NFTInfoDto() {

    }

}
