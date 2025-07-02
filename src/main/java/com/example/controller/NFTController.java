package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.NFTInfoDto;
import com.example.service.NFT.NFTInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/nft")
@Tag(name = "NFT",description = "NFT操作")
public class NFTController {

    @Resource
    NFTInfoService nftInfoService;

    @PutMapping("/addSingle")
    public <T> RestBean<T> addSingle (HttpServletRequest request
                                      , @RequestBody NFTInfoDto dto){
        return this.nftInfoService.NFTInfoAddSingle(request,dto)?
                RestBean.success():RestBean.failure(401,"添加失败，信息有误");
    }

    @GetMapping("/select")
    public <T> RestBean<T> selectNFT (HttpServletResponse response , NFTInfoDto dto) throws IOException {
        List<NFTInfoDto> dtoList = this.nftInfoService.infoSelectByCondition(dto);
        if (!dtoList.isEmpty()){
            response.getWriter().write(RestBean.success(dtoList).asJsonString());
            return null;
        }
        return RestBean.failure(401,"暂无该NFT");
    }



}
