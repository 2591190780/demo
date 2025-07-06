package com.example.controller.collection;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.dto.CollectionInfoDto;
import com.example.service.collection.CollectInfoDeleteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deleteCollect")
@Tag(name="删除购物车信息",description = "相关操作")
public class CollectInfoDeleteController {
    @Resource
    CollectInfoDeleteService Service;

    @Auditable(
            operationType = "DELETE_COLLECTION",
            captureBefore = true,
            captureAfter = true
    )
    @PutMapping("/delete")
    public RestBean<Void> addAllProducts(HttpServletRequest request, @RequestBody CollectionInfoDto vo){
        return Service.CollectInfoDelete(request,vo);
    }
}
