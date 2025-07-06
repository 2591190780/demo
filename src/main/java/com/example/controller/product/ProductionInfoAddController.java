package com.example.controller.product;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.vo.request.ProductAddVO;
import com.example.service.product.ProductInfoAddAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addProducts")
@Tag(name="产品信息添加请求",description = "相关操作")
public class ProductionInfoAddController {
    @Resource
    ProductInfoAddAccountService service;

    @Auditable(
            operationType = "ADD_SINGLE_PRODUCT",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/addSingle")
    public RestBean<Void> addSingleProduct (HttpServletRequest request,
                                            @RequestBody ProductAddVO vo
                                            ) {
        return  service.addUserProductSingle(request,vo);
    }

    @Auditable(
            operationType = "ADD_MULTI_PRODUCT",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/addAll")
    public RestBean<Void> addAllProducts(HttpServletRequest  request,@RequestBody List<ProductAddVO> voList){
        return service.addUserProductAll(request,voList);

    }


}
