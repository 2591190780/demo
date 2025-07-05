package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@TableName("user_info")
@AllArgsConstructor
public class Account {
    @TableId(type = IdType.AUTO)
    Integer id;
    String username;
    String password;
    String email;
    String role;
    LocalDateTime register_date;

    // 钱包地址字段
    @Length(min = 42, max = 42)
    String walletAddress;

    String userImgurl;

    @Length(min = 11, max = 11)
    String phoneNumber;

}
