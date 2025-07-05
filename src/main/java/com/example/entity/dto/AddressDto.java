package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@TableName("user_address")
@AllArgsConstructor
public class AddressDto {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String address;

    public AddressDto() {

    }
}
