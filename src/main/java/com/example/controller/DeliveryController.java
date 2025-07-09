package com.example.controller;


import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.domain.DeliveryInfo;
import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.dto.DeliveryInfoDto;
import com.example.service.transaction.DeliveryInfoService;
import com.example.service.transaction.TransactionProcessService;
import com.example.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/api/delivery")
@Tag(name="运送信息",description = "操作")
public class DeliveryController {

    @Resource
    DeliveryInfoService deliveryInfoService;

    @Resource
    JwtUtils jwtUtils;

    @Resource
    TransactionProcessService transactionProcessService;

    @Auditable(
            operationType = "ADD_INFO_APP_DELIVERY",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/add/info")
    public <T>RestBean<T> addInfo(HttpServletRequest request , @RequestBody DeliveryInfoDto dto){
       return this.deliveryInfoService.addDeliveryInfo(request,dto)?
               RestBean.success():RestBean.failure(401,"添加失败，请勿重复添加同时检查参数。");
    }

    @Auditable(
            operationType = "SELECT_INFO_DELIVERY",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/select/info")
    public <T> RestBean<T> selectInfo(HttpServletRequest request,
                                      @Parameter String order,
                                      HttpServletResponse response ) throws IOException {
        Integer uid = this.jwtUtils.getRequesetId(request);
        Integer buyerId = this.transactionProcessService.getOrderByAlipayOrder(order).getBuyerId();
        if(Objects.equals(uid, buyerId))return RestBean.failure(401,"暂无权限。");
        DeliveryInfoDto dto = this.deliveryInfoService.selectDeliveryInfoByAlipayOrder(order);
        if(dto==null){return RestBean.failure(401,"查询失败。");}
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(dto).asJsonString());
        return null;
    }


    @Auditable(
            operationType = "UPDATE_INFO_DELIVERY",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/update/info")
    public <T>RestBean<T> updateInfo(HttpServletRequest request ,
                                     @Parameter String alipayOrder,
                                     @Parameter String time,
                                     @Parameter String data){
      return this.deliveryInfoService.updateDeliveryInfo(request,alipayOrder,data,time) ?
              RestBean.success():RestBean.failure(401,"修改失败，请检查参数。");
    }


}
