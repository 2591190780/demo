package com.example.entity.dto;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@TableName("user_nft")
@AllArgsConstructor
public class UserNFTDto {
    @TableId(type = IdType.AUTO)
    private BigInteger id;
    private Integer userId;
    private Integer nftId;
    @Length(min = 80, max = 80)
    private String tokenId;   //链上的token
    private String txHash;
    private LocalDateTime mintTime; //获得时间
    private int status;

    private String metaData;

}
