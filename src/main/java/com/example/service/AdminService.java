package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.PendingApplication;
import com.example.entity.RestBean;
import com.example.entity.vo.response.PendingApplicationVO;

import java.util.List;

public interface AdminService extends IService<PendingApplicationVO> {
    List<PendingApplicationVO> getPendingApplications();
    boolean handleApplicationSingle(PendingApplicationVO vo);

}
