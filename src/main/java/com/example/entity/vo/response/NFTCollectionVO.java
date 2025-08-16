package com.example.entity.vo.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class NFTCollectionVO {
    private Integer nftId;
    private Integer ownerId;
    private Integer publicBy;
    private LocalDateTime getNFTTime;
    private String nftName;
    private String nftCid;
    private String transactionHash;
    private Integer status;
    private String nftContractAddress;
    private LocalDateTime nftCreateTime;
    private Integer nftIssuance;
    private LocalDateTime nftPassTime;
    private String validityPeriod;
    private String metadataUrl;
    private String description;

}
