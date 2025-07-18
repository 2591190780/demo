package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("user_address")
@AllArgsConstructor
public class AddressDto  {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String userAddress;

    private Integer defaultAddress;

    @Length(min = 11, max = 11,message = "手机号长度必须为11位")
    private String phoneNumber;

    private String receiveName;

    public AddressDto() {

    }
}
