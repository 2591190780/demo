package com.example.controller;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.vo.request.ResetPasswordByPasswordVO;
import com.example.service.transaction.TransactionProcessService;
import com.example.utils.JwtUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/transaction")
@Tag(name="订单信息",description = "操作")
public class TransactionController {
    @Resource
    TransactionProcessService transactionProcessService;

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
                                       String status){
        if (!Objects.equals(jwtUtils.getRequesetId(request), sellerId)) {
            return RestBean.failure(401,"权限不足。");
        }
        return this.transactionProcessService.updateStatusByAlipayOrder(alipayOrder, sellerId, status)?
                RestBean.success():RestBean.failure(401,"请检查订单信息。");
    }

}
