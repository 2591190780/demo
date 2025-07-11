package com.example.entity.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Data
@TableName("blockchain_evidence")
@AllArgsConstructor
public class BlockChainEvidenceDto {

    @TableId(type = IdType.AUTO)
    private Integer evidenceId;

    private Integer dataType;
    private Integer relatedId;

    @Length(min = 66, max = 66)
    private String txHash;

    private String bolckNumber;
    private LocalDateTime timestamp;

    public BlockChainEvidenceDto() {

    }
}
