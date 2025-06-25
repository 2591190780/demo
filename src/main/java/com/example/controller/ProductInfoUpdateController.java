package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.ProductUpdateVO;
import com.example.service.ProductInfoUpdateAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/AUProducts")
@Tag(name="产品信息修改请求",description = "相关操作")
public class ProductInfoUpdateController {

    @Resource
    ProductInfoUpdateAccountService service;

    @PutMapping("/updateSingle")
    public RestBean<Void> updateProducts(HttpServletRequest  request
            , @Valid @RequestBody ProductUpdateVO productUpdateVO)  {
        return service.updateSingleProductInfo(request,productUpdateVO);
    }

}
