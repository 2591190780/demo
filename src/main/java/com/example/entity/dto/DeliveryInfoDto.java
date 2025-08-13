package com.example.entity.dto;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@TableName("delivery_info")
@AllArgsConstructor
public class DeliveryInfoDto {

    @TableId(type = IdType.AUTO)  // 自增主键
    private Integer id;

    String orderId;
    String startAddress;
    String endAddress;
    String startTime;
    String endTime;
    String deliveryProcess;

    @Length(min = 11, max = 11)
    String sellerPhonenumber;
    @Length(min = 11, max = 11)
    String buyerPhonenumber;

    Integer relateId;
}
