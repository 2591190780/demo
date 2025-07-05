package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class NFTPendingApplication {
    private String buyerID;
    private String sellerID;
    private String nftID;
    private BigDecimal price;
    private Object nft;
}
