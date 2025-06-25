package com.example.service.impl;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.ProductUpdateVO;
import com.example.mapper.ProductInfoUpdateAccountMapper;
import com.example.service.ProductInfoUpdateAccountService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductInfoUpdateAccountImpl extends ServiceImpl<ProductInfoUpdateAccountMapper, ProductUpdateVO>
        implements ProductInfoUpdateAccountService {

    @Resource
    JwtUtils utils;

    @Override
    public <T> RestBean<T> updateSingleProductInfo(HttpServletRequest request, ProductUpdateVO account){
        boolean verify = this.userRoleVerify(request);
        if(!verify)return RestBean.forbidden("权限不足");
        if(update(account)) return RestBean.success();
        return  RestBean.failure(500,"内部错误，请联系管理员");
    }

    @Override
    public  <T> RestBean<T> addProductInfo(HttpServletRequest request, ProductUpdateVO account){

        return null;
    }

    /**
     * 权限验证
     * @param request
     * @return
     */
    private boolean userRoleVerify( HttpServletRequest request){
        // 1.获取前端传入的token信息 解析角色信息
        String authorization = request.getHeader("Authorization");
        DecodedJWT jwt = utils.resolveJWT(authorization);
        String role = utils.toRole(jwt);
        return !role.equals("2");
    }
    private boolean update(ProductUpdateVO account){

        Integer productId = account.getProductId();
        Integer farmerId= account.getFarmerId();
        String name = account.getName();
        String category = account.getCategory();
        BigDecimal price = account.getPrice();
        BigDecimal stock = account.getStock();
        String originLocation = account.getOriginLocation();

        return  this.update()
                .eq("product_id",productId)
                .eq("farmer_id",farmerId)
                .set("name",name)
                .set("category",category)
                .set("price",price)
                .set("stock",stock)
                .set("origin_location",originLocation)
                .update();
    }

}
