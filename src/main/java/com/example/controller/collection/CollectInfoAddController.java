package com.example.controller.collection;

import com.example.entity.RestBean;
import com.example.entity.dto.CollectionInfoDto;
import com.example.service.collection.CollectInfoAddService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/Collect")
@Tag(name="购物车添加产品",description = "相关操作")
public class CollectInfoAddController {

    @Resource
    CollectInfoAddService Service;


    @PutMapping("/addCollect")
    public RestBean<Void> addAllProducts(HttpServletRequest request, @RequestBody CollectionInfoDto vo){
        return Service.addCollectInfoSingle(request,vo);

    }
}
