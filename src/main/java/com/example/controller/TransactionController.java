package com.example.controller;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.dto.TransactionAccountDto;
import com.example.entity.vo.request.ResetPasswordByPasswordVO;
import com.example.entity.vo.response.TransactionInfoVO;
import com.example.service.AddressService;
import com.example.service.transaction.TransactionProcessService;
import com.example.utils.JwtUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/transaction")
@Tag(name="订单信息",description = "操作")
public class TransactionController {
    @Resource
    TransactionProcessService transactionProcessService;
    @Resource
    AddressService addressService;

    @Resource
    JwtUtils jwtUtils;

    @Auditable(
            operationType = "RESET_PASSWORD_USER",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/update/status")
    public RestBean<Void> updateStatus(HttpServletRequest request ,
                                       String alipayOrder,
                                       Integer sellerId,
                                       String status,
                                       Integer transactionId){
        if (!Objects.equals(jwtUtils.getRequesetId(request), sellerId)) {
            return RestBean.failure(401,"权限不足。");
        }
        return this.transactionProcessService.updateStatusByAlipayOrder(alipayOrder,transactionId, sellerId, status)?
                RestBean.success():RestBean.failure(401,"请检查订单信息。");
    }


    @GetMapping("/select/id")
    public void updateStatus(HttpServletRequest request ,
                                       Integer transactionId,
                                       HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        Integer uid = jwtUtils.getRequesetId(request);
        TransactionAccountDto dto = this.transactionProcessService.getOrderByTransactionID(transactionId);
        Integer sellerId = dto.getSellerId();
        Integer buyerId = dto.getBuyerId();
        if(Objects.equals(uid, sellerId) || Objects.equals(uid, buyerId)){
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return;
        }
        response.getWriter().write(RestBean.failure(401,"权限不足。").asJsonString());
        return;
    }


}
