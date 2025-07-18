package com.example.controller;

import com.example.annotation.Auditable;
import com.example.utils.WeBaseUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public String test(){
        return "Hello World";
    }


    @Resource
    WeBaseUtils weBaseUtils;
    @GetMapping("/contract")
    public void contractTest(HttpServletResponse httpServletResponse) throws Exception {
        // 1. 设置参数
        String userContractAddress = "0xe441981c5cdb9c2d5f18b45605d37eb9161986f5";
        String contractAddress = "0xc248c1c09d12cd39f751734d8102af50ca8259b6";
        String methodName = "registerUser";
        List<Object> param = new ArrayList<>();
        param.add(0,"ContractTest");
        param.add(1,100);
        // 2. 调用方法，获取返回消息。
        Map<String, Object> result
                = weBaseUtils.callContractMethod(userContractAddress,contractAddress,methodName,param);
        // 3. 显示结果
        System.out.println(result);
        // 4. 以string格式返回给前端
        httpServletResponse.getWriter().write(result.toString());
    }
}