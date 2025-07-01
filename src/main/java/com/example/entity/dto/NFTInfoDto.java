package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.time.LocalDateTime;

@EntityScan
@Data
@TableName("nft_template")
@AllArgsConstructor
public class NFTInfoDto {

    @TableId(type = IdType.AUTO)  // 自增主键
    private Integer templateId;
    private String name;
    private String description;
    private String imgUrl;
    private String contractAddress;
    private Integer issuanceLimit;
    private int isActive;
    private LocalDateTime createdAt;
    private String metadataUrl;
    private String nftLevel;
    private LocalDateTime updateTime;

    public NFTInfoDto() {

    }
}
