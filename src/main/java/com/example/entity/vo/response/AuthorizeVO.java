package com.example.entity.vo.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Data
@Setter
@Getter
public class AuthorizeVO {
    Integer id;
    String username;
    String role;
    String token;
    Date expire;
    String email;
    String date;
    // 钱包地址字段
    String walletAddress;
    String phoneNumber;
}
