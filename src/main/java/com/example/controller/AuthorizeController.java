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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Function;
import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name="请求",description = "相关操作")
public class AuthorizeController {

    @Resource
    AccountService accountService;


    @GetMapping("/ask-code") //请求发送验证码
    public RestBean<Void> askVerifyCode(@RequestParam  @Email  String email ,
                                        @RequestParam @Pattern(regexp = "(register|reset)") String type,
                                        HttpServletRequest request
                                        ){
        //从消息队列中消费
        accountService.registerEmailVerifyCode(type, email, request.getRemoteAddr());
        return RestBean.success();

    }
    @PostMapping("/register")  //注册
    public ResponseEntity<RestBean<String[]>> registerUser(@RequestBody @Valid EmailRegisterVO vo) {
          String[] message =  accountService.registerEmailAccount(vo).split(":");
          if(message.length != 2){
              return ResponseEntity.ok()
                      .body(
                              RestBean.failure(401,message[0])
                      );
          }
          return ResponseEntity.ok()
                  .body(
                          RestBean.success(message)
                  );

    }

    @PostMapping("/reset-confirm") //验证码确认
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO vo){
        return this.messageHandle(vo, accountService::resetConfirm);
    }

    @PostMapping("/reset-password") //验证码修改密码
    public RestBean<Void> resetPasswordConfirm(@RequestBody @Valid EmailResetVO vo){
        return this.messageHandle(vo,accountService::resetEmailAccountPassword);
    }

    @PostMapping("/resetPassword") //密码验证方式修改密码
    public RestBean<Void> resetPasswordConfirm(@RequestBody  ResetPasswordByPasswordVO dto,
                                             HttpServletRequest request ){
        String result = accountService.resetPasswordByPassword(dto, request);
        return result == null ? RestBean.success():RestBean.failure(401,result);
    }

    private <T>RestBean<Void> messageHandle(T vo, Function<T, String> function) {
        return messageHandle(()->function.apply(vo));
    }

    private  RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        return message == null ? RestBean.success():RestBean.failure(400,message);
    }
}
