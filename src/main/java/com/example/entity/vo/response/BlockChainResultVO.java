package com.example.entity.vo.response;

import com.example.entity.dto.NFTInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
public class BlockChainResultVO {

    @Length(min = 42, max = 42)
    String fromWalletAddress;
    @Length(min = 42, max = 42)
    String toWalletAddress;
    NFTInfoDto nftInfoDto;
    @Length(min = 66, max = 66)
    String txHashSeller; //
    String txHashBuyer;
    Integer type;

//    @Length(min = 66, max = 66)
//    String submitHash;


}
