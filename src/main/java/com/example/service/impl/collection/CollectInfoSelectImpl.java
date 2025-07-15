package com.example.service.impl.collection;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.entity.dto.CollectionInfoDto;
import com.example.mapper.collection.CollectionInfoMapper;
import com.example.service.collection.CollectInfoSelectService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectInfoSelectImpl extends ServiceImpl<CollectionInfoMapper, CollectionInfoDto>
        implements CollectInfoSelectService {
        @Resource
        JwtUtils jwtUtils;


        @Override
        public List<CollectionInfoDto> selectAllProduct(HttpServletRequest request, String type){
            Integer id = jwtUtils.getRequesetId(request);
            return this.query()
                    .eq("user_id", id)
                    .eq("operation_type",type)
                    .list();

        }

        @Override
        public  CollectionInfoDto selectByHash(Integer uid ,String hash,String type){
            return this.query()
                    .eq("user_id", uid)
                    .eq("product_hash",hash)
                    .eq("operation_type",type)
                    .one();
        }

}




























