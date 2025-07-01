package com.example.service.collection;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.RestBean;
import com.example.entity.dto.CollectionInfoDto;
import jakarta.servlet.http.HttpServletRequest;

public interface CollectInfoAddService extends IService<CollectionInfoDto> {
    <T>RestBean<T> addCollectInfoSingle(HttpServletRequest request, CollectionInfoDto dto);
}
