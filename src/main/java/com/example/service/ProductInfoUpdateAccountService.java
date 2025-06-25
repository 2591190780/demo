package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.RestBean;
import com.example.entity.dto.ProductUpdateVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


public interface ProductInfoUpdateAccountService extends IService<ProductUpdateVO> {
     <T> RestBean<T> updateSingleProductInfo(HttpServletRequest request, ProductUpdateVO account);
     <T> RestBean<T> updateAllProductInfo(HttpServletRequest request, List<ProductUpdateVO> accountList);
     <T> RestBean<T> addProductInfo(HttpServletRequest request, ProductUpdateVO account);
}