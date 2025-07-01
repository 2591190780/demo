package com.example.service.collection;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.CollectionInfoDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface CollectInfoSelectService extends IService<CollectionInfoDto> {
     List<CollectionInfoDto> selectAllProduct(HttpServletRequest request, String type);
}
