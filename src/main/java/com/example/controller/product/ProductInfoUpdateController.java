package com.example.controller.product;

import com.example.entity.RestBean;
import com.example.entity.dto.ProductInfoAccountDto;
import com.example.service.product.ProductInfoUpdateAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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



}
