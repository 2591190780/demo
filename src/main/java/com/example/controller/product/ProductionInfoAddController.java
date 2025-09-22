package com.example.controller.product;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.vo.request.ProductAddVO;
import com.example.service.product.ProductInfoAddAccountService;
import com.example.utils.IPFSUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/addProducts")
@Tag(name="产品信息添加请求",description = "相关操作")
public class ProductionInfoAddController {
    @Resource
    ProductInfoAddAccountService service;
    @Resource
    IPFSUtils ipfsUtils;


    @Auditable(
            operationType = "ADD_SINGLE_PRODUCT",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/addSingle")
    public <T>RestBean<T> addSingleProduct (HttpServletRequest request,
                                            @RequestPart("productAddVO") ProductAddVO productAddVO,
                                            @RequestPart("file") MultipartFile file
                                            ) throws IOException {

        if (file != null && !file.isEmpty()) {
            String cid = ipfsUtils.returnUpLoadSuccess(file);
            productAddVO.setProductImgurl(cid);
         return  service.addUserProductSingle(request, productAddVO); // 按你的 service 接口
        }

        return RestBean.failure(401,"参数错误。");
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
