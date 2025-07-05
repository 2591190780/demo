package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.AddressDto;
import com.example.service.AddressService;
import com.example.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/address")
@Tag(name="用户地址",description = "用户地址操作")
public class AddressController {

    @Resource
    private AddressService addressService;

    @Resource
    JwtUtils jwtUtils;

    @GetMapping("/userid/select")
    public <T>RestBean<T> selectByUserId(HttpServletRequest request, HttpServletResponse response, @Parameter String userid ) throws IOException {
        List<AddressDto> dtoList = this.addressService.findByuserId(jwtUtils.convertToInteger(userid));
        AddressDto addressDto = dtoList.get(0);
        if(this.addressService.userIdEqRequestId(request,addressDto)){
            response.getWriter().write(RestBean.success(dtoList).asJsonString());
            return null;
        }
        return RestBean.failure(401,"您还没有添加地址。");
    }

    @GetMapping("/id/select")
    public <T>RestBean<T> selectById(HttpServletRequest request, HttpServletResponse response, @Parameter String id ) throws IOException {
        AddressDto dto = this.addressService.findById(jwtUtils.convertToInteger(id));
        if(this.addressService.userIdEqRequestId(request,dto)){
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return null;
        }
        return RestBean.failure(401,"未查询到该地址。");
    }

    @PutMapping("/add")
    public <T>RestBean<T> addAddress(@RequestBody AddressDto dto, HttpServletRequest request) throws IOException {
        if(this.addressService.userIdEqRequestId(request,dto)){
            this.addressService.addAddress(dto);
            return RestBean.success();
        }
        return RestBean.failure(401,"添加失败,请检查参数。");
    }

    @PutMapping("/update")
    public <T>RestBean<T> updateAddress(@RequestBody AddressDto dto, HttpServletRequest request) throws IOException {
        if(this.addressService.userIdEqRequestId(request,dto)){
            this.addressService.updateAddress(dto);
            return RestBean.success();
        }
        return RestBean.failure(401,"添加失败,请检查参数。");
    }

    @PutMapping("/delete")
    public <T>RestBean<T> deleteAddress(@RequestBody AddressDto dto, HttpServletRequest request) throws IOException {
        if(this.addressService.userIdEqRequestId(request,dto)){
            this.addressService.deleteAddress(dto);
            return RestBean.success();
        }
        return RestBean.failure(401,"添加失败,请检查参数。");
    }

}
