package com.example.service.impl.collection;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.CollectionInfoDto;
import com.example.mapper.collection.CollectionInfoMapper;
import com.example.service.collection.CollectInfoDeleteService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CollectInfoDeleteImpl extends ServiceImpl<CollectionInfoMapper, CollectionInfoDto>
        implements CollectInfoDeleteService {

    @Resource
    JwtUtils jwtUtils;

    @Resource
    CollectionInfoMapper collectInfoMapper;


    @Override
    public <T>RestBean<T> CollectInfoDelete(HttpServletRequest request, CollectionInfoDto vo) {
        Integer id = jwtUtils.getRequesetId(request);
        Integer cid = vo.getUserId();
        if(!Objects.equals(id, cid)) return RestBean.forbidden("请不要删除他人产品信息");
        QueryWrapper<CollectionInfoDto> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", vo.getUserId())
                .eq("product_id", vo.getProductId())
                        .eq("operation_type",vo.getOperation_type());
        collectInfoMapper.delete(wrapper);
        return RestBean.success();
    }


    @Override
    public boolean CollectInfoDeleteAll(Integer userId) {
        QueryWrapper<CollectionInfoDto> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("operation_type",1);
        collectInfoMapper.delete(wrapper);
        return true;
    }
}
