package com.example.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.LogInfoDto;
import com.example.mapper.LogInfoMapper;
import com.example.service.LogInfoService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LogInfoImpl extends ServiceImpl<LogInfoMapper, LogInfoDto> implements LogInfoService {

    @Override
    public boolean addLogInfo(LogInfoDto dto){
        try {
            // 1. 设置默认值
            if (dto.getCreateTime() == null) {
                dto.setCreateTime(LocalDateTime.now());
            }
            if (dto.getStatus() == null) {
                dto.setStatus("1"); // 默认成功
            }
            if (dto.getUserId() == null) {
                dto.setUserId(-1); // 未知用户
            }

            // 2. 记录调试信息
            System.out.println("新增日志："+dto);
            // 3. 执行保存
            return this.save(dto);
        } catch (Exception e) {
            log.error("保存日志失败");
            // 记录关键字段以便调试
            return false;
        }
    }
}
