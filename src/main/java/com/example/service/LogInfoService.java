package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.LogInfoDto;

public interface LogInfoService extends IService<LogInfoDto> {
    boolean addLogInfo(LogInfoDto dto);
}
