package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.entity.vo.request.ResetPasswordByPasswordVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account> , UserDetailsService {
     Account findAccountByNameOrEmail(String text);  //查询用户（username 和 email ）
     Account findAccountById(Integer id);
     void registerEmailVerifyCode(String type, String email, String ip); //发送注册邮箱验证码
     String registerEmailAccount(EmailRegisterVO emailRegisterVO); //通过邮箱注册用户
     String resetConfirm(ConfirmResetVO confirmVO);  //验证重置密码请求的验证码是否正确
     String resetEmailAccountPassword(EmailResetVO emailResetVO); //通过邮箱验证码重置密码
     String resetPasswordByPassword(ResetPasswordByPasswordVO vo, HttpServletRequest request);
}
