package com.example.controller;


import com.example.annotation.Auditable;
import com.example.entity.RestBean;

import com.example.entity.dto.ProductInfoAccountDto;
import com.example.entity.vo.request.ApplyHandlingRequestVO;
import com.example.entity.vo.response.PendingApplicationVO;

import com.example.service.AdminService;
import com.example.service.product.ProductInfoUpdateAccountService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    @Auditable(
            operationType = "UPDATE_ALL_ADMIN_OPERATE",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/updateAll/admin")
    public RestBean<Void> updateAllProductsAdmin(HttpServletRequest request
            , @RequestBody List<ProductInfoAccountDto> accountDtos)  {
        return service.updateAllProductInfoAdmin(request,accountDtos);
    }

    @Auditable(
            operationType = "UPDATE_SINGLE_ADMIN",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/updateSingle/admin")
    public RestBean<Void> updateProductsAdmin(HttpServletRequest  request
            ,  @RequestBody ProductInfoAccountDto accountDto)  {
        return service.updateSingleProductInfoAdmin(request, accountDto);
    }

    /**
     * 获取所有待处理的申请列表
     */

    @Auditable(
            operationType = "ADMIN_PENDING_APPLICATIONS",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/admin/pending-applications")
    public <T>RestBean<T>  adminApplySelect(HttpServletRequest request
            ,HttpServletResponse response) throws IOException {

        response.setContentType("application/json;Charset=utf-8");
        List<PendingApplicationVO> pendingApplicationVOS = adminService.getPendingApplications(request);
            if (!(pendingApplicationVOS ==null)) {
                response.getWriter().write(RestBean.success(pendingApplicationVOS).asJsonString());
                return null;
            }
        response.getWriter().write(RestBean.failure(401,"请检查权限或没有任何请求").asJsonString());
        return null;
    }

    @Auditable(
            operationType = "ADMIN_HANDLING_APPLICATIONS",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/admin/handling-applications")
    public RestBean<Void> adminApplyHandling(
            HttpServletRequest request,
            @RequestBody ApplyHandlingRequestVO aHR) throws IOException {

            List<PendingApplicationVO> voList = aHR.getData();
            if (voList == null) {return RestBean.failure(401,"请检查参数格式。");}
            byte answer = aHR.getAnswer();
         return this.adminService.handleApplication(request,voList,answer)
                 ? RestBean.success():RestBean.failure(401,"申请已过期或参数有误。");

    }


}
