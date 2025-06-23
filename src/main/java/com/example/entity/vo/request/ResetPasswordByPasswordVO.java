package com.example.entity.vo.request;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class ResetPasswordByPasswordVO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "旧密码不能为空")
    @Length(min = 6, max = 20, message = "旧密码长度需在6-20字符之间")
    private String oldPassword;
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$",
            message = "密码必须包含大小写字母和数字")
    @Length(min = 6, max = 20, message = "新密码长度需在6-20字符之间")

    private String newPassword;
}
