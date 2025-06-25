package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.RestBean;
import com.example.entity.dto.ProductInfoAccountDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


public interface ProductInfoUpdateAccountService extends IService<ProductInfoAccountDto> {
     <T> RestBean<T> updateSingleProductInfo(HttpServletRequest request, ProductInfoAccountDto account);
     <T> RestBean<T> updateAllProductInfo(HttpServletRequest request, List<ProductInfoAccountDto> accountList);
     <T> RestBean<T> updateSingleProductInfoAdmin(HttpServletRequest request, ProductInfoAccountDto account);
     <T> RestBean<T> updateAllProductInfoAdmin(HttpServletRequest request, List<ProductInfoAccountDto> accountList);
}