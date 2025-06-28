package com.example.controller.product;


import com.example.entity.RestBean;
import com.example.entity.vo.response.ProductVO;
import com.example.service.product.ProductInfoSelectAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/selectProducts")
@Tag(name="产品信息搜索请求",description = "相关操作")
public class ProductInfoSelectController {

    @Resource
    ProductInfoSelectAccountService paService;

    /**
     *
     * @param id productid select
     * @param response
     * @return
     * @throws IOException
     */
    @GetMapping("/search/id")
    public RestBean<Void> getProductInfoByID(@RequestParam @Valid  String id,
                                             HttpServletResponse response) throws IOException {
        ProductVO productVO = paService.getProductInfoAccountByProductId(this.convertToInteger(id));
        response.setContentType("application/json;Charset=utf-8");
        if (productVO != null) {
            response.getWriter().write(RestBean.success(productVO).asJsonString());
            return null;
        }
        return RestBean.failure(404, "该农户暂无产品");
    }

    /**
     *
     * @param id farmerid select
     * @param response
     * @return
     * @throws IOException
     */
        @GetMapping("/search/farmerId")
        public RestBean<Void> getProductInfoByFarmerID(@RequestParam @Valid  String id,
                                                       HttpServletResponse response) throws IOException {

            List<ProductVO> productVO = paService.getProductInfoAccountByFarmerID(this.convertToInteger(id));
            if (productVO.isEmpty()) {
                return RestBean.failure(404, "该农户暂无产品");
            }
            response.setContentType("application/json;Charset=utf-8");
            response.getWriter().write(RestBean.success(productVO).asJsonString());
            return null;
    }
    /**
     *
     * @param text select
     * @param response
     * @return
     * @throws IOException
     */
    @GetMapping("/search/text")
    public RestBean<Void> getProductInfoByName(@RequestParam String text,
                                                  HttpServletResponse response) throws IOException {

        List<ProductVO> productVO = paService.getProductInfoAccountByName(text);
        if (productVO.isEmpty()) {
            return RestBean.failure(404, "该农户暂无产品");
        }
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(productVO).asJsonString());
        return null;
    }

    /**
     *  select
     * @param id
     * @param fid
     * @param name
     * @param category
     * @param location
     * @param response
     * @return
     * @throws IOException
     */

    @GetMapping("/search/all")
    public RestBean<Void> getProductInfoAll(@RequestParam @Valid  String id, @RequestParam @Valid  String fid,
                                            @RequestParam String name, @RequestParam String category,
                                            @RequestParam String location, HttpServletResponse response) throws IOException {

        List<ProductVO> productVO = paService.selectProductAccByText(
                this.convertToInteger(id)
                ,this.convertToInteger(fid)
                ,name,category,location);
        if (productVO.isEmpty()) {
            return RestBean.failure(404, "该农户暂无产品");
        }
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(productVO).asJsonString());
        return null;
    }

    private Integer convertToInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null; // 或者记录日志
        }
    }


}
