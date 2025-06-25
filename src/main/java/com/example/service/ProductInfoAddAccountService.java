package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.RestBean;
import com.example.entity.dto.ProductInfoAccountDto;
import com.example.entity.vo.request.ProductAddVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


public interface ProductInfoAddAccountService extends IService<ProductInfoAccountDto> {

    <T>RestBean<T> addUserProductSingle(HttpServletRequest request, ProductAddVO vo);
    RestBean<Void>  addUserProductAll(HttpServletRequest request, List<ProductAddVO> voList);


}
