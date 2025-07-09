package com.example.service.product;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.dto.ProductInfoAccountDto;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.List;


public interface ProductInfoUpdateAccountService extends IService<ProductInfoAccountDto> {
     <T> RestBean<T> updateSingleProductInfo(HttpServletRequest request, ProductInfoAccountDto account);
     <T> RestBean<T> updateAllProductInfo(HttpServletRequest request, List<ProductInfoAccountDto> accountList);
     <T> RestBean<T> updateSingleProductInfoAdmin(HttpServletRequest request, ProductInfoAccountDto account);
     <T> RestBean<T> updateAllProductInfoAdmin(HttpServletRequest request, List<ProductInfoAccountDto> accountList);
     boolean productUpdateAdmin(Integer productId,Integer farmerId,byte active);
     <T> RestBean<T> updateProductInfoDown(HttpServletRequest request, ProductInfoAccountDto account);
     boolean updateStock(Integer productId,Integer farmerId, BigDecimal count);
}