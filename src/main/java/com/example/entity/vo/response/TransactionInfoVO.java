package com.example.entity.vo.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Setter
@Getter
public class TransactionInfoVO {
    private Integer listId ;
    private BigInteger id;
    private String orderId;
    private String alipayOrder;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime orderTime;
    private Integer sellerId;
    private Integer buyerId;
    private Integer addressInfo;
    @Length(min = 66, max = 66)
    private String certificationHash; // 映射数据库 certification_hash
    private Integer productId;
}
