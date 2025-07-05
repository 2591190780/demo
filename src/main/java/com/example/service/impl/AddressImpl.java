package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.AddressDto;
import com.example.mapper.AddressMapper;
import com.example.service.AddressService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.checkerframework.checker.nullness.qual.Raw;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressImpl extends ServiceImpl<AddressMapper, AddressDto> implements AddressService {

    @Resource
    private AddressMapper addressMapper;

    @Resource
    JwtUtils jwtUtils;

    @Override
    public List<AddressDto> findByuserId(Integer id){
        return this.query().eq("user_id", id).list();
    }

    @Override
    public AddressDto findById(Integer id){
        return this.query().eq("id", id).one();
    }

    @Override
    public  boolean addAddress(AddressDto dto){
        return this.save(dto);
    }

    @Override
    public  boolean updateAddress(AddressDto dto){
        return this.update().eq("id",dto.getId()).eq("user_id",dto.getAddress())
                .set("user_address",dto.getAddress()).update();
    }
    @Override
    public  boolean deleteAddress(AddressDto dto){
        QueryWrapper<AddressDto> wrapper = new QueryWrapper<>();
        wrapper.eq("id",dto.getId()).eq("user_id", dto.getUserId());
        addressMapper.delete(wrapper);
        return true;
    }

    public boolean userIdEqRequestId(HttpServletRequest request,AddressDto dto){
        Integer rid = jwtUtils.getRequesetId(request);
        if(!rid.equals(dto.getUserId())) return false;
        return true;
    }

}
