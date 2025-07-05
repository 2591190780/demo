package com.example.controller.collection;

import com.example.entity.RestBean;
import com.example.entity.vo.request.CollectUpdateVO;
import com.example.service.CollectInfoUpdateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/updateCollect")
@Tag(name="修改产品数量",description = "相关操作")
public class CollectInfoUpdateController {
    @Resource
    CollectInfoUpdateService Service;

    @PutMapping("/update")
    public <T> RestBean<T> UpdateProduct(HttpServletRequest request, @RequestBody CollectUpdateVO vo){
        return Service.updateNum(request,vo);

    }

}
