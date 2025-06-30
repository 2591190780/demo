package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@TableName("order_info")
@AllArgsConstructor
public class TransactionAccountDto {

    @TableId(type = IdType.AUTO)
    private BigInteger id;

    private String orderId;          // 映射数据库 order_id
    private Integer buyerId;         // 映射数据库 buyer_id
    private Integer productId;       // 映射数据库 product_id
    private Integer sellerId;        // 映射数据库 seller_id
    private BigDecimal quantity;
    private BigDecimal totalPrice;   // 映射数据库 total_price
    private String status;
    private LocalDateTime orderTime; // 映射数据库 order_time

    @Length(min = 66, max = 66)
    private String certificationHash; // 映射数据库 certification_hash

    private LocalDateTime completeTime;  // 映射数据库 complete_time
    private BigDecimal actualPayment;    // 映射数据库 actual_payment
    private LocalDateTime deliveryTime;  // 映射数据库 delivery_time
    private String alipayOrder;            // 映射数据库 alipay_order

    public TransactionAccountDto() {

    }
}
