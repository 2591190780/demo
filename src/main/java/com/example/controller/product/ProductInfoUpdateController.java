package com.example.controller.product;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.dto.ProductInfoAccountDto;
import com.example.entity.vo.response.ProductVO;
import com.example.service.product.ProductInfoSelectAccountService;
import com.example.service.product.ProductInfoUpdateAccountService;
import com.example.utils.IPFSUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/AUProducts")
@Tag(name="产品信息修改请求",description = "相关操作")
public class ProductInfoUpdateController {

    @Resource
    ProductInfoUpdateAccountService service;
    @Resource
    IPFSUtils ipfsUtils;
    @Resource
    ProductInfoSelectAccountService selectAccountService;

    @Auditable(
            operationType = "UPDATE_SINGLE_PRODUCT",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/updateSingle")
    public RestBean<Object> updateProducts(HttpServletRequest  request,
                                         @RequestPart("accountDto")  ProductInfoAccountDto accountDto,
                                         @RequestPart(value = "file",required = false) MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String cid = ipfsUtils.returnUpLoadSuccess(file);
            accountDto.setProductImgurl(cid);
        }
        Integer pid = accountDto.getProductId();
        ProductVO dto = selectAccountService.getProductInfoAccountByProductId(pid);

        if(dto.getUpdateTime()==null){
            return RestBean.failure(401,"请等待管理员通过。");
        }

        RestBean<Object> result = service.updateSingleProductInfo(request, accountDto);
        if (result == null) {
            return RestBean.success();
        }
        return result;
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
    @PutMapping("/update/downOrUpProduct")
    public <T>RestBean<T> downOrUpProduct(HttpServletRequest  request
            ,@RequestBody ProductInfoAccountDto accountDto){
        Integer pid = accountDto.getProductId();
        ProductVO dto = selectAccountService.getProductInfoAccountByProductId(pid);
        if(dto.getUpdateTime()==null){
            return RestBean.failure(401,"请等待管理员通过。");
        }
        return this.service.updateProductInfoDown(request,accountDto);
    }
}
