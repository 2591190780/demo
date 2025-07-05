package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.AddressDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AddressService extends IService<AddressDto> {
    List<AddressDto> findByUserId(Integer userId);
    AddressDto findById(Integer id);
    boolean addAddress(AddressDto dto);
    boolean updateAddress(AddressDto dto);
    boolean deleteAddress(AddressDto dto);
    boolean userIdEqRequestId(HttpServletRequest request, AddressDto dto);
    boolean setDefaultAddress(Integer id, Integer ans);
}
