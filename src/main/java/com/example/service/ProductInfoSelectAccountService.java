package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.ProductInfoAccountDto;
import com.example.entity.vo.response.ProductVO;

import java.util.List;

public interface ProductInfoSelectAccountService extends IService<ProductInfoAccountDto> {
    ProductVO getProductInfoAccountByProductId(Integer Id) ; //查询商品信息(ID查询)
    List<ProductVO> getProductInfoAccountByFarmerID(Integer Id) ; //查询商品信息(FarmerID查询)
    List<ProductVO> getProductInfoAccountByName(String name); //根据字符的模糊查询
    List<ProductVO> selectProductAccByText(Integer id,Integer fid,String name,String category,String location);
}
