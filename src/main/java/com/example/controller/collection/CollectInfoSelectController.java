package com.example.controller.collection;

import com.example.annotation.Auditable;
import com.example.entity.RestBean;
import com.example.entity.dto.CollectionInfoDto;

import com.example.service.collection.CollectInfoSelectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/selectCollect")
@Tag(name="展示购物车或收藏夹",description = "相关操作")
public class CollectInfoSelectController {
    @Resource
    CollectInfoSelectService Service;

    @Auditable(
            operationType = "SELECT_COLLECTION",
            captureBefore = true,
            captureAfter = true
    )
    @GetMapping("/select")
    public void SelectProduct(HttpServletRequest request, @RequestParam  @Valid String type
            , HttpServletResponse response) throws IOException {
        List<CollectionInfoDto> dtoList = Service.selectAllProduct(request,type);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(RestBean.success(dtoList).asJsonString());
    }
}
