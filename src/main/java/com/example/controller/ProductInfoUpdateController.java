package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.ProductInfoAccountDto;
import com.example.entity.vo.request.ProductAddVO;
import com.example.service.ProductInfoUpdateAccountService;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/AUProducts")
@Tag(name="产品信息修改请求",description = "相关操作")
public class ProductInfoUpdateController {

    @Resource
    ProductInfoUpdateAccountService service;

    @PutMapping("/updateSingle")
    public RestBean<Void> updateProducts(HttpServletRequest  request
            ,  @RequestBody ProductInfoAccountDto accountDto)  {

        return service.updateSingleProductInfo(request, accountDto);
    }

    @PutMapping("/updateAll")
    public RestBean<Void> updateAllProducts(HttpServletRequest  request
            ,  @RequestBody List<ProductInfoAccountDto> accountDtos)  {
        return service.updateAllProductInfo(request,accountDtos);
    }

    @PutMapping("/updateAll/admin")
    public RestBean<Void> updateAllProductsAdmin(HttpServletRequest  request
            ,  @RequestBody List<ProductInfoAccountDto> accountDtos)  {
        return service.updateAllProductInfoAdmin(request,accountDtos);
    }

    @PutMapping("/updateSingle/admin")
    public RestBean<Void> updateProductsAdmin(HttpServletRequest  request
            ,  @RequestBody ProductInfoAccountDto accountDto)  {
        return service.updateSingleProductInfoAdmin(request, accountDto);
    }

}
