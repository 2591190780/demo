package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.entity.vo.request.ResetPasswordByPasswordVO;
import com.example.service.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;
import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name="验证码请求",description = "相关操作")
public class AuthorizeController {

    @Resource
    AccountService accountService;


    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam  @Email  String email ,
                                        @RequestParam @Pattern(regexp = "(register|reset)") String type,
                                        HttpServletRequest request
                                        ){
        //从消息队列中消费
        accountService.registerEmailVerifyCode(type, email, request.getRemoteAddr());
        return RestBean.success();

    }
    @PostMapping("/register")
    public RestBean<Void> registerUser(@RequestBody @Valid EmailRegisterVO vo) {
        return this.messageHandle(vo,accountService::registerEmailAccount);
    }

    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO vo){
        return this.messageHandle(vo, accountService::resetConfirm);
    }

    @PostMapping("/reset-password")
    public RestBean<Void> resetPasswordConfirm(@RequestBody @Valid EmailResetVO vo){
        return this.messageHandle(vo,accountService::resetEmailAccountPassword);
    }

    @PostMapping("/resetPassword") //密码验证方式修改密码
    public RestBean<Void> resetPasswordConfirm(@RequestBody @Valid ResetPasswordByPasswordVO dto,
                                             HttpServletRequest request ){
        String result = accountService.resetPasswordByPassword(dto, request);
        if (result == null) {
            return RestBean.success();
        } else {
            return RestBean.failure(400, result);
        }
    }


    private <T>RestBean<Void> messageHandle(T vo, Function<T, String> function) {
        return messageHandle(()->function.apply(vo));
    }

    private  RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        return message == null? RestBean.success():RestBean.failure(400,message);
    }
}
