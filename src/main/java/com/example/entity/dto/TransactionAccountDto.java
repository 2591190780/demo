package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
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
    private Integer buyer_id; //买家
    private Integer product_id; //产品id
    private Integer seller_id; //卖家
    private BigDecimal quantity; //数量
    private BigDecimal total_price; //总价
    private String status;  //订单状态
    private LocalDateTime order_time;  //下单时间
    @Length(min = 66,max = 66)
    private String certificationHash;  //交易哈希值
    private LocalDateTime complete_time;  //交易完成时间
    private BigDecimal actual_payment;  //实付款
    private LocalDateTime delivery_time;  //发货时间

}
