package com.example.entity.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blockchain_evidence")
@AllArgsConstructor
public class BlockChainEvidenceDto {

    @TableId(type = IdType.AUTO)
    private Integer evidenceId;

    private Integer dataType;
    private Integer relatedId;
    private String txHash;

    private String bolckNumber;
    private LocalDateTime timestamp;

}
