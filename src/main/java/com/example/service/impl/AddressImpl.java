package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.AddressDto;
import com.example.mapper.AddressMapper;
import com.example.service.AddressService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.bouncycastle.pqc.crypto.newhope.NHOtherInfoGenerator;
import org.checkerframework.checker.nullness.qual.Raw;
import org.fisco.bcos.sdk.v3.codec.datatypes.Int;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressImpl extends ServiceImpl<AddressMapper, AddressDto> implements AddressService {

    @Resource
    private AddressMapper addressMapper;

    @Resource
    JwtUtils jwtUtils;

    @Override
    public boolean setDefaultAddress(Integer id, Integer ans){
        this.defaultSetting(id);
        return this.update().eq("id",id)
                .set("default_address",ans).update();
    }


    @Override
    public List<AddressDto> findByUserId(Integer id){
        return this.query().eq("user_id", id).list();
    }

    @Override
    public AddressDto findById(Integer id){
        return this.query().eq("id", id).one();
    }

    @Override
    public  boolean addAddress(AddressDto dto){
        if(dto.getDefaultAddress()==1)this.defaultSetting(dto.getUserId());
        return this.save(dto);
    }

    @Override
    public  boolean updateAddress(AddressDto dto){
        return this.update().eq("id",dto.getId())
                .eq("user_id",dto.getUserId())
                .set("user_address",dto.getUserAddress())
                .set("receive_name",dto.getReceiveName())
                .update();
    }

    @Override
    public  boolean deleteAddress(AddressDto dto){
        QueryWrapper<AddressDto> wrapper = new QueryWrapper<>();
        wrapper.eq("id",dto.getId()).eq("user_id", dto.getUserId());
        addressMapper.delete(wrapper);
        return true;
    }
    @Override
    public  boolean updatePhone(AddressDto dto){
        return this.update().eq("id",dto.getId()).eq("user_id",dto.getUserId())
                .set("phone_number",dto.getPhoneNumber()).update();
    }

    public boolean userIdEqRequestId(HttpServletRequest request,AddressDto dto){
        Integer rid = jwtUtils.getRequesetId(request);
        if(!rid.equals(dto.getUserId())) return false;
        return true;
    }

    private void defaultSetting(Integer id){
        List<AddressDto> dtoList = this.findByUserId(id);
        for (AddressDto dto : dtoList) {
            if (dto.getDefaultAddress() == 1){
                this.update().eq("id",dto.getId())
                        .set("default_address",0).update();
            }
        }
    }

}
