package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.NFTInfoDto;
import com.example.entity.dto.NFTRuleDto;
import com.example.service.NFT.NFTAddInfoService;
import com.example.service.NFT.NFTAddRuleService;
import com.example.service.NFT.NFTInfoService;
import com.example.service.NFT.NFTRuleService;
import com.example.utils.JwtUtils;
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
    @Resource
    NFTRuleService nftRuleService;
    @Resource
    NFTAddInfoService nftAddInfoService;
    @Resource
    NFTAddRuleService nftAddRuleService;

    @Resource
    JwtUtils  jwtUtils;

    @PutMapping("/rule/addSingle")
    public <T> RestBean<T> addSingleRule(HttpServletRequest request, @RequestBody NFTRuleDto dto) {
        return this.nftAddRuleService.NFTRuleAddSingle(request,dto)?
                RestBean.success():RestBean.failure(401,"添加失败，信息有误");
    }
    @GetMapping("/rule/select/id")
    public <T> RestBean<T> selectRule(HttpServletRequest request,HttpServletResponse response
            ,@RequestParam String id) throws IOException {
          Integer tid = jwtUtils.convertToInteger(id);
            NFTRuleDto dto = this.nftRuleService.nftRuleSelectByID(tid);
          if(dto!=null){
              response.getWriter().write(RestBean.success(dto).asJsonString());
              return null;
          }
          return RestBean.failure(401,"暂无该NFT规则");
    }
    @GetMapping("/rule/select/name")
    public <T>RestBean<T> selectRuleByName(HttpServletRequest request,HttpServletResponse response
    ,@RequestParam String name) throws IOException {
        List<NFTRuleDto> list = this.nftRuleService.nftRuleSelectByName(name);
        if(!list.isEmpty()){
            response.getWriter().write(RestBean.success(list).asJsonString());
            return null;
        }
        return RestBean.failure(401,"暂无该NFT规则");
    }
    @GetMapping("/rule/select/templateid")
    public <T>RestBean<T> selectRuleByTemplateId(HttpServletRequest request,HttpServletResponse response
    ,@RequestParam String templateId) throws IOException {
             Integer tid = jwtUtils.convertToInteger(templateId);
             List<NFTRuleDto> dto = this.nftRuleService.nftRuleSelectByTemplateID(tid);
             if(dto!=null){
                 response.getWriter().write(RestBean.success(dto).asJsonString());
                 return null;
             }
        return RestBean.failure(401,"暂无该NFT规则");
    }
    @GetMapping("/rule/select/hash")
    public <T>RestBean<T> selectRuleByHash(HttpServletRequest request,HttpServletResponse response
            ,@RequestParam String hash) throws IOException {
            NFTRuleDto ruleDto = this.nftRuleService.nftRuleSelectByHash(hash);
            if(ruleDto!=null){
                response.getWriter().write(RestBean.success(ruleDto).asJsonString());
                return null;
            }
        return RestBean.failure(401,"暂无该NFT规则");
    }
    @GetMapping("/rule/select/condition")
    public <T>RestBean<T> selectRuleByCondition(HttpServletRequest request,HttpServletResponse response
    ,@RequestBody NFTRuleDto dto) throws IOException {
            List<NFTRuleDto> dtoList = this.nftRuleService.nftRuleSelectCondition(dto);
            if(dtoList!=null){
                response.getWriter().write(RestBean.success(dtoList).asJsonString());
                return null;
            }
            return RestBean.failure(401,"暂无该NFT规则");
    }



    @PutMapping("/info/addSingle")
    public <T> RestBean<T> addSingleInfo(HttpServletRequest request
                                      , @RequestBody NFTInfoDto dto){
        return this.nftAddInfoService.NFTInfoAddSingle(request,dto)?
                RestBean.success():RestBean.failure(401,"添加失败，信息有误");
    }
    @GetMapping("/info/select")
    public <T> RestBean<T> selectNFT (HttpServletResponse response , NFTInfoDto dto) throws IOException {
        List<NFTInfoDto> dtoList = this.nftInfoService.infoSelectByCondition(dto);
        if (!dtoList.isEmpty()){
            response.getWriter().write(RestBean.success(dtoList).asJsonString());
            return null;
        }
        return RestBean.failure(401,"暂无该NFT");
    }
    @GetMapping("/info/select/url")
    public  <T> RestBean<T> selectNFTUrl (@RequestParam String url,HttpServletResponse response) throws IOException {
        NFTInfoDto dto =  this.nftInfoService.NFTInfoSelectByURL(url);
        if (dto!=null){
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return null;
        }
        return RestBean.failure(401,"暂无该NFT");
    }
    @GetMapping("/info/select/templateid")
    public  <T> RestBean<T> selectNFTID (@RequestParam String id,HttpServletResponse response) throws IOException {
        NFTInfoDto dto =  this.nftInfoService.NFTInfoSelectByTemplateId(jwtUtils.convertToInteger(id));
        if (dto!=null){
            response.getWriter().write(RestBean.success(dto).asJsonString());
            return null;
        }
        return RestBean.failure(401,"暂无该NFT");
    }
    @GetMapping("/info/select/publicid")
    public <T> RestBean<T> selectPublicID(@RequestParam String id, HttpServletResponse response) throws IOException {
        List<NFTInfoDto> dtoList = this.nftInfoService.NFTInfoSelectByPublic(jwtUtils.convertToInteger(id));
        if (!dtoList.isEmpty()){
            response.getWriter().write(RestBean.success(dtoList).asJsonString());
            return null;
        }
        return RestBean.failure(401,"暂无该NFT");
    }






}
