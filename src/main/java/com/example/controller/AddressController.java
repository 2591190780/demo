package com.example.controller;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.dto.AddressDto;
import com.example.service.AddressService;
import com.example.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/address")
@Tag(name="用户地址",description = "用户地址操作")
public class AddressController {

    @Resource
    private AddressService addressService;

    @Resource
    JwtUtils jwtUtils;

    @Auditable(
            operationType = "USER_SET_DEFAULT_ADDRESS",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/user/set/default")
    public  <T> RestBean<T> setDefault(HttpServletRequest request,
                                       @Parameter String id,
                                       @Parameter String flag){

        AddressDto dto = this.addressService.findById(jwtUtils.convertToInteger(id));
        if(!Objects.equals(jwtUtils.getRequesetId(request),dto.getUserId()))
        {return RestBean.failure(401,"未授权的操作");}
            return this.addressService.setDefaultAddress(jwtUtils.convertToInteger(id),
                    jwtUtils.convertToInteger(flag))? RestBean.success():RestBean.failure(401,"设置失败。");

    }

    @Auditable(
            operationType = "USERID_SELECT_ADDRESS",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/userid/select")
    public <T>RestBean<T> selectByUserId(HttpServletRequest request, HttpServletResponse response,
                                         @Parameter String userid ) throws IOException {
        List<AddressDto> dtoList = this.addressService.findByUserId(jwtUtils.convertToInteger(userid));
        AddressDto addressDto = dtoList.get(0);
        if(this.addressService.userIdEqRequestId(request,addressDto)){
            response.setContentType("application/json;Charset=utf-8");
            response.getWriter().write(RestBean.success(dtoList).asJsonString());
            return null;
        }
        return RestBean.failure(401,"您还没有添加地址。");
    }

    @Auditable(
            operationType = "ID_SELECT_ADDRESS",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/id/select")
    public <T>RestBean<T> selectById(HttpServletRequest request, HttpServletResponse response,
                                     @Parameter String id ) throws IOException {
        AddressDto dto = this.addressService.findById(jwtUtils.convertToInteger(id));
        if(this.addressService.userIdEqRequestId(request,dto)){
            response.setContentType("application/json;Charset=utf-8");
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return null;
        }
        return RestBean.failure(401,"未查询到该地址。");
    }

    @Auditable(
            operationType = "ADD_ADDRESS",
            captureBefore = true,
            captureAfter = true
    )

    @PutMapping("/add")
    public <T>RestBean<T> addAddress(@RequestBody @Valid AddressDto dto, HttpServletRequest request) throws IOException {
        if(dto==null) return RestBean.failure(401,"请不要传入空的参数。");
        if(dto.getUserId() == null) dto.setUserId(jwtUtils.getRequesetId(request));
        if(this.addressService.userIdEqRequestId(request,dto)){
            this.addressService.addAddress(dto);
            return RestBean.success();
        }
        return RestBean.failure(401,"添加失败,请检查参数。");
    }

    @Auditable(
            operationType = "UPDATE_ADDRESS",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/update")
    public <T>RestBean<T> updateAddress(@RequestBody AddressDto dto, HttpServletRequest request) throws IOException {
        if(this.addressService.userIdEqRequestId(request,dto)){
            this.addressService.updateAddress(dto);
            return RestBean.success();
        }
        return RestBean.failure(401,"添加失败,请检查参数。");
    }

    @Auditable(
            operationType = "UPDATE_ADDRESS_PHONE",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/update/phone")
    public <T>RestBean<T> updatePhone(@RequestBody AddressDto dto, HttpServletRequest request) throws IOException {
        if(this.addressService.userIdEqRequestId(request,dto)){
            this.addressService.updatePhone(dto);
            return RestBean.success();
        }
        return RestBean.failure(401,"添加失败,请检查参数。");
    }



    @Auditable(
            operationType = "DELETE_ADDRESS",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/delete")
    public <T>RestBean<T> deleteAddress(@RequestBody AddressDto dto, HttpServletRequest request) throws IOException {
        if(this.addressService.userIdEqRequestId(request,dto)){
            this.addressService.deleteAddress(dto);
            return RestBean.success();
        }
        return RestBean.failure(401,"添加失败,请检查参数。");
    }

}
