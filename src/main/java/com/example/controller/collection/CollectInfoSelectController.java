package com.example.controller.collection;

import com.alipay.api.domain.AccountVO;
import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.dto.CollectionInfoDto;

import com.example.entity.dto.ProductInfoAccountDto;
import com.example.entity.vo.response.CollectionInfoVO;
import com.example.entity.vo.response.ProductVO;
import com.example.service.AccountService;
import com.example.service.collection.CollectInfoSelectService;
import com.example.service.product.ProductInfoSelectAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/selectCollect")
@Tag(name="展示购物车或收藏夹",description = "相关操作")
public class CollectInfoSelectController {
    @Resource
    CollectInfoSelectService Service;

    @Resource
    ProductInfoSelectAccountService productInfoSelectAccountService;
    @Resource
    AccountService accountService;

    @GetMapping("/select")
    public void SelectProduct(HttpServletRequest request, @RequestParam  @Valid String type
            , HttpServletResponse response) throws IOException {
        List<CollectionInfoDto> dtoList = Service.selectAllProduct(request,type);
        List<CollectionInfoVO> voList = new ArrayList<>();
        for (CollectionInfoDto dto : dtoList) {
            ProductVO vo =  this.productInfoSelectAccountService.getProductInfoAccountByProductId(dto.getProductId());
            Account account = this.accountService.findAccountById(vo.getFarmerId());
            account.setPassword(null);
            voList.add(
                    new CollectionInfoVO(dto,vo,account)
            );
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(RestBean.success(voList).asJsonString());
    }
}
