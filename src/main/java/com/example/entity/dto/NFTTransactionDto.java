package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.fisco.bcos.sdk.v3.codec.datatypes.Int;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@TableName("nft_transaction")
@AllArgsConstructor
public class NFTTransactionDto {

    @TableId(type = IdType.AUTO)
    private BigInteger txId;
    private Integer nftId;
    private Integer fromUser;
    private Integer toUser;

    @Length(min = 66, max = 66)
    private String txHash;

    private LocalDateTime txTime;
    private int type;

    private BigDecimal price;

    private int active;
}
