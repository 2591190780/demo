package com.example.service.impl.collection;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.vo.request.CollectUpdateVO;

import com.example.mapper.collection.CollectInfoUpdateMapper;
import com.example.service.CollectInfoUpdateService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class CollectInfoUpdateImpl extends ServiceImpl<CollectInfoUpdateMapper,CollectUpdateVO>
        implements CollectInfoUpdateService {

    @Resource
    JwtUtils jwtUtils;

    @Override
    public  <T> RestBean<T> updateNum(HttpServletRequest request, CollectUpdateVO vo){
        Integer id = jwtUtils.getRequesetId(request);
        Integer cid = vo.getUserId();
        if(!jwtUtils.getUserIdVerify(request,cid)) return RestBean.forbidden("请不要修改他人产品数量");
        Integer pid = vo.getProductId();
                update()
               .eq("user_id", id)
               .eq("product_id",pid).set("product_num",vo.getProductNum())
        .update();
        return RestBean.success();
    }

}