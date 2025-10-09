package com.example.controller.collection;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.dto.CollectionInfoDto;
import com.example.service.collection.CollectInfoDeleteService;
import com.example.utils.JwtUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deleteCollect")
@Tag(name="删除购物车信息",description = "相关操作")
public class CollectInfoDeleteController {
    @Resource
    CollectInfoDeleteService Service;
    @Resource
    JwtUtils jwtUtils;

    @Auditable(
            operationType = "DELETE_COLLECTION",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/delete")
    public RestBean<Void> addAllProducts(HttpServletRequest request, @RequestBody CollectionInfoDto vo){
        return Service.CollectInfoDelete(request,vo);
    }
    @GetMapping("/delete/all")
    public RestBean<Void> deleteAllProducts(HttpServletRequest request){
        Integer userId = this.jwtUtils.getRequesetId(request);
        this.Service.CollectInfoDeleteAll(userId);
        return RestBean.success();
    }

}
