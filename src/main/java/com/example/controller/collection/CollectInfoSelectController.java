package com.example.controller.collection;

import com.example.entity.dto.CollectionInfoDto;

import com.example.service.collection.CollectInfoSelectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/selectCollect")
@Tag(name="展示购物车或收藏夹",description = "相关操作")
public class CollectInfoSelectController {
    @Resource
    CollectInfoSelectService Service;

    @GetMapping("/select")
    public List <CollectionInfoDto> SelectProduct(HttpServletRequest request, @RequestParam  @Valid String type){
        return Service.selectAllProduct(request,type);

    }
}
