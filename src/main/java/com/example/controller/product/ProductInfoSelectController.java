package com.example.controller.product;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.annotation.Auditable;
import com.example.controller.ControllerPageHelper;
import com.example.entity.PageParam;
import com.example.entity.PageResult;
import com.example.entity.RestBean;
import com.example.entity.dto.ProductInfoAccountDto;
import com.example.entity.vo.response.ProductVO;
import com.example.mapper.product.ProductInfoSelectAccountMapper;
import com.example.service.product.ProductInfoSelectAccountService;
import com.example.utils.JwtUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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

    @Resource
    ProductInfoSelectAccountMapper  paMapper;

    @Resource
    JwtUtils jwtUtils;

    /**
     *
     * @param id productid select
     * @param response
     * @return
     * @throws IOException
     */

    @Auditable(
            operationType = "SEARCH_ID_PRODUCTS",
            captureBefore = true,
            captureAfter = true
    )
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

    @Auditable(
            operationType = "SEARCH_FARMER_ID_PRODUCT",
            captureBefore = true,
            captureAfter = true
    )
        @GetMapping("/search/farmerId")
        public RestBean<Void> getProductInfoByFarmerID(@RequestParam @Valid  String id,
                                                       HttpServletResponse response,
                                                       @ModelAttribute PageParam pageParam) throws IOException {

           // List<ProductVO> productVO = paService.getProductInfoAccountByFarmerID(this.convertToInteger(id));
            Page<ProductVO> page = paMapper.selectBySellerIdPage(pageParam.toPage(),this.convertToInteger(id));
            if (page.getTotal()==0) {
                return RestBean.failure(404, "该农户暂无产品");
            }
            List<ProductVO> voList= page.getRecords();
            PageResult<ProductVO> pageResult = new PageResult<>(
                    page.getTotal(),
                    voList,
                    (int) page.getCurrent(),
                    (int) page.getPages(),
                    (int) page.getSize()
            );
            response.setContentType("application/json;Charset=utf-8");
            response.getWriter().write(RestBean.success(pageResult).asJsonString());
            return null;
    }

    @Auditable(
            operationType = "SEARCH_CATEGORY",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/search/category")
    public RestBean<Void> getCate(@RequestParam @Valid  String id,
                                                   HttpServletResponse response) throws IOException {

        List<ProductVO> productVO = paService.getProductInfoAccountByFarmerID(this.convertToInteger(id));

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

    @Auditable(
            operationType = "SEARCH_TEXT_PRODUCT",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/search/text")
    public RestBean<Void> getProductInfoByName(HttpServletRequest request,
                                               @RequestParam("text") String text, @ModelAttribute PageParam pageParam,
                                               HttpServletResponse response) throws IOException {
        Integer fid = jwtUtils.getRequesetId(request);
        List<ProductVO> productVO = paService.getProductInfoAccountByName(text);
        if (productVO.isEmpty()) {
            return RestBean.failure(404, "该农户暂无产品");
        }
        PageResult<ProductVO> pageResult = ControllerPageHelper.paginateList(
                productVO, pageParam
        );

        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(pageResult).asJsonString());
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
                                            @RequestParam String location,@ModelAttribute PageParam pageParam, HttpServletResponse response) throws IOException {
        //这里只返回已经激活的农产品
        List<ProductVO> productVO = paService.selectProductAccByText(
                this.convertToInteger(id)
                ,this.convertToInteger(fid)
                ,name,category,location);
        if (productVO.isEmpty()) {
            return RestBean.failure(404, "该农户暂无产品");
        }
        PageResult<ProductVO> pageResult = ControllerPageHelper.paginateList(
                productVO, pageParam
        );
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(pageResult).asJsonString());
        return null;
    }

    @GetMapping("/search/latest/five")
    public RestBean<Void> getProductInfoRandomFive(HttpServletRequest request,HttpServletResponse response) throws IOException {
        List<ProductInfoAccountDto> productInfoAccountDto = paService.selectProductAccountForRecommendation();
        response.setContentType("application/json;Charset=utf-8");
        response.getWriter().write(RestBean.success(productInfoAccountDto).asJsonString());
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
