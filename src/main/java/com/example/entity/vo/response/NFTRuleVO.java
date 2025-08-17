package com.example.entity.vo.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class NFTRuleVO {
    /** NFT 基本信息 */
    private String contractAddress;   // 合约地址
    private Integer templateId;       // 模板ID
    private String name;              // NFT名称
    private String description;       // 描述
    private String ruleDescription;       // 描述
    private String imageUrl;          // 图片URL
    private String nftLevel;          // NFT等级
    private Integer isActive;         // 是否有效 (1=有效,0=无效)
    private Integer issuanceLimit;    // 总发行量
    private Integer remainCount;      // 剩余数量
    private LocalDateTime createdAt;         // 创建时间

    private Integer publicBy;         // 发布者ID
    private String publicEmail;
    private String publicWalletAddress;
    private String publicImageUrl;
    private String publicName;

    private String metadataUrl;       // 元数据JSON字符串
    /** 发放规则列表 */
    private String rules;

}
