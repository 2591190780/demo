package com.example.controller;


import com.example.entity.RestBean;

import com.example.entity.dto.ProductInfoAccountDto;
import com.example.entity.vo.response.PendingApplicationVO;

import com.example.service.AdminService;
import com.example.service.ProductInfoUpdateAccountService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.ibatis.jdbc.Null;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/adminOperate")
@Tag(name="管理员",description = "管理员操作")
public class AdminController {

    @Resource
    ProductInfoUpdateAccountService service;

    @Resource
    AdminService adminService;

    @PutMapping("/updateAll/admin")
    public RestBean<Void> updateAllProductsAdmin(HttpServletRequest request
            , @RequestBody List<ProductInfoAccountDto> accountDtos)  {
        return service.updateAllProductInfoAdmin(request,accountDtos);
    }

    @PutMapping("/updateSingle/admin")
    public RestBean<Void> updateProductsAdmin(HttpServletRequest  request
            ,  @RequestBody ProductInfoAccountDto accountDto)  {
        return service.updateSingleProductInfoAdmin(request, accountDto);
    }
    /**
     * 获取所有待处理的申请列表
     */
    @GetMapping("/admin/pending-applications")
    public <T>RestBean<T>  adminApplySelect(HttpServletRequest request ,HttpServletResponse response) throws IOException {

        List<PendingApplicationVO> pendingApplicationVOS = adminService.getPendingApplications(request);
            if (pendingApplicationVOS.isEmpty()) {
                return RestBean.failure(401,"没有任何申请");
            }
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(pendingApplicationVOS).asJsonString());
        return null;
    }

    @PutMapping("/admin/handling-applications")
    public RestBean<Void> adminApplyHandling(
            HttpServletRequest request
            , @RequestBody  List<PendingApplicationVO> voList) throws IOException {
         return this.adminService.handleApplication(request,voList)
                 ? RestBean.success():RestBean.failure(500,"参数有误");

    }



}
