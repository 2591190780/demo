package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.service.AccountService;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
        return this.messageHandle(()->accountService.registerEmailAccount(vo));
    }

    private  RestBean<Void> messageHandle(Supplier<String> action){
        String message = action.get();
        return message == null? RestBean.success():RestBean.failure(400,message);
    }
}
