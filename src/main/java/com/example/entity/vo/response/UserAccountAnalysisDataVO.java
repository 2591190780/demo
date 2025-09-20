package com.example.entity.vo.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class UserAccountAnalysisDataVO {
    //基本信息
    Integer id;
    String username;
    String role;
    String email;
    String walletAddress;
    String phoneNumber;
    String imgUrl;

    //进阶信息
    // 购物交易数量
    Integer numOfTransactions;
    //总购物成交金额
    Integer totalMoneyOfTransactions;
    // NFT交易次数
    Integer totalNFTTransactions;

}
