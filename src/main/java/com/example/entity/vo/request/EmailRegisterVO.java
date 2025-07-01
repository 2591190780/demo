package com.example.entity.vo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class EmailRegisterVO {
    @Email
    private  String email;
    @Length(min=6,max=6)
    String code;
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$", message = "用户名只能包含中文、字母和数字")
            @Length(min=1,max=10, message = "用户名长度必须在1-10个字符之间")
    String username;
    @Length(min=6,max=20, message = "用户名密码长度必须在6-20个字符之间")
    String password;

    String walletAddress;

}
