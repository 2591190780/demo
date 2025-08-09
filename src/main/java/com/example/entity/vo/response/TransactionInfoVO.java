package com.example.entity.vo.response;

import com.example.entity.dto.AddressDto;
import com.example.entity.dto.ProductInfoAccountDto;
import com.example.entity.dto.TransactionAccountDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Setter
@Getter
public class TransactionInfoVO {


    private Integer id ;
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
