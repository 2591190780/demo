package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.RestBean;
import com.example.entity.dto.ProductInfoAccountDto;
import com.example.entity.vo.request.ProductAddVO;
import jakarta.servlet.http.HttpServletRequest;


public interface ProductInfoAddAccountService extends IService<ProductInfoAccountDto> {

    <T>RestBean<T> addUserProductSingle(HttpServletRequest request, ProductAddVO vo);

}
