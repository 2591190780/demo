package com.example.entity.vo.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Setter
@Getter
public class TransactionInfoVO {


    private Integer listId ;
    private String orderId;
    private String alipayOrder;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal price;
    private String status;
    private LocalDateTime orderTime;
    private Integer sellerId;
    private Integer buyerId;
    private Integer addressInfo;

}
