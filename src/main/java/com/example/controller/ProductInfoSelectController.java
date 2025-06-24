package com.example.controller;


import com.example.entity.RestBean;
import com.example.entity.vo.response.ProductVO;
import com.example.service.ProductInfoAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name="产品信息请求",description = "相关操作")
public class ProductInfoSelectController {

    @Resource
    ProductInfoAccountService paService;

    @GetMapping("/search/id") //单个查询
    public RestBean<Void> getProductInfoByID(@RequestParam int id,
                                             HttpServletResponse response) throws IOException {
        ProductVO productVO = paService.getProductInfoAccountByProductId(id);
        response.setContentType("application/json;Charset=utf-8");
        if (productVO != null) {
            response.getWriter().write(RestBean.success(productVO).asJsonString());
            return null;
        }
        return RestBean.failure(404, "该农户暂无产品");
    }

        @GetMapping("/search/farmerId")
        public RestBean<Void> getProductInfoByFamerID(@RequestParam int id,
                                             HttpServletResponse response) throws IOException {

            List<ProductVO> productVO = paService.getProductInfoAccountByFarmerID(id);
            if (productVO.isEmpty()) {
                return RestBean.failure(404, "该农户暂无产品");
            }
            response.setContentType("application/json;Charset=utf-8");
            response.getWriter().write(RestBean.success(productVO).asJsonString());
            return null;
    }

    @GetMapping("/search/text")
    public RestBean<Void> getProductInfoByName(@RequestParam String text,
                                                  HttpServletResponse response) throws IOException {

        List<ProductVO> productVO = paService.getProducteInfoAccountByName(text);
        if (productVO.isEmpty()) {
            return RestBean.failure(404, "该农户暂无产品");
        }
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(productVO).asJsonString());
        return null;
    }

    @GetMapping("/search/all")
    public RestBean<Void> getProductInfoAll(@RequestParam Integer id,@RequestParam Integer fid,
                                            @RequestParam String name,@RequestParam String category,
                                            @RequestParam String location,HttpServletResponse response) throws IOException {

        List<ProductVO> productVO = paService.selectProductAccByText(id,fid,name,category,location);
        if (productVO.isEmpty()) {
            return RestBean.failure(404, "该农户暂无产品");
        }
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(productVO).asJsonString());
        return null;
    }


}
