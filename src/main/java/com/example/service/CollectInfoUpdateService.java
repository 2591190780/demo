package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.RestBean;
import com.example.entity.vo.request.CollectUpdateVO;
import jakarta.servlet.http.HttpServletRequest;

public interface CollectInfoUpdateService extends IService<CollectUpdateVO> {
    <T> RestBean<T> updateNum(HttpServletRequest request, CollectUpdateVO vo);
}
