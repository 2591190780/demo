package com.example.controller.product;

import com.example.annotation.Auditable;
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

    @Auditable(
            operationType = "UPDATE_SINGLE_PRODUCT",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/updateSingle")
    public RestBean<Void> updateProducts(HttpServletRequest  request
            ,  @RequestBody ProductInfoAccountDto accountDto)  {

        return service.updateSingleProductInfo(request, accountDto);
    }

    @Auditable(
            operationType = "UPDATE_ALL_PRODUCTS",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/updateAll")
    public RestBean<Void> updateAllProducts(HttpServletRequest  request
            ,  @RequestBody List<ProductInfoAccountDto> accountDtos)  {
        return service.updateAllProductInfo(request,accountDtos);
    }


    @Auditable(
            operationType = "UPDATE_PRODUCT_DOWN",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/update/downProduct")
    public <T>RestBean<T> downProduct(HttpServletRequest  request
            ,@RequestBody ProductInfoAccountDto accountDto){
        return this.service.updateProductInfoDown(request,accountDto);
    }


}
