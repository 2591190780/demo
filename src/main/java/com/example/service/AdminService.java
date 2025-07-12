package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.PendingApplication;
import com.example.entity.RestBean;
import com.example.entity.vo.response.PendingApplicationVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AdminService extends IService<PendingApplicationVO> {
    List<PendingApplicationVO> getPendingApplications(HttpServletRequest request);
    boolean handleApplication(HttpServletRequest request,List<PendingApplicationVO> voList,byte answer) throws Exception;
    List<PendingApplicationVO> getPendingApplyCategory(HttpServletRequest request, String Type);
}
