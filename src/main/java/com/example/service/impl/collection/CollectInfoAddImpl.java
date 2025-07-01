package com.example.service.impl.collection;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.CollectionInfoDto;
import com.example.mapper.collection.CollectionInfoMapper;
import com.example.service.collection.CollectInfoAddService;
import com.example.service.product.ProductInfoSelectAccountService;
import com.example.utils.BlockchainHashUtil;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.LocalDateTime;

@Service
public class CollectInfoAddImpl extends ServiceImpl<CollectionInfoMapper, CollectionInfoDto>
        implements CollectInfoAddService {
    @Resource
    JwtUtils jwtUtils;

    @Resource
    BlockchainHashUtil blockchainHashUtil;

    @Resource
    ProductInfoSelectAccountService productInfoSelectAccountService;

    @Override
    public <T>RestBean<T> addCollectInfoSingle(HttpServletRequest request, CollectionInfoDto vo) {
        Integer id = jwtUtils.getRequesetId(request);
        Integer cid = vo.getUserId();
        if(!jwtUtils.getUserIdVerify(request,cid)) return RestBean.forbidden("请不要给别人的购物车添加产品");
        vo.setUserId(id);
        vo.setCreate_time(LocalDateTime.now());
        String hash = productInfoSelectAccountService.getProductInfoAccountByProductId(vo.getProductId()).getCertificationHash();
        vo.setProduct_hash(hash);
        this.save(vo);
    return RestBean.success();
    }

}
