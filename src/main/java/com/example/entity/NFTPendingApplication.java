package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.scheduling.quartz.LocalDataSourceJobStore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NFTPendingApplication {
    private String buyerID;
    private String sellerID;
    private String nftID;
    private BigDecimal price;
    private Object nft;
    private LocalDateTime applyTime;
    private LocalDateTime passiveTime;
    private String status;
}
