package com.example.config;


import com.example.annotation.Auditable;
import com.example.entity.dto.LogInfoDto;
import com.example.service.LogInfoService;
import com.example.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.LocalDateTime;


@Component
@Aspect
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final LogInfoService logInfoService;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuditAspect(@Lazy LogInfoService logInfoService, JwtUtils jwtUtils) {
        this.logInfoService = logInfoService;
        this.jwtUtils = jwtUtils;
    }

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        // 1. 创建日志DTO
        LogInfoDto logInfo = new LogInfoDto();
        logInfo.setOperationType(auditable.operationType());
        logInfo.setCreateTime(LocalDateTime.now());
        logInfo.setStatus("0"); // 默认成功

        // 2. 获取HTTP请求
        HttpServletRequest request = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                request = attributes.getRequest();

                // 3. 使用JwtUtils获取用户ID
                Integer userId = jwtUtils.getRequesetId(request);
                if (userId != null) {
                    logInfo.setUserId(userId);
                } else {
                    log.warn("无法从请求中获取用户ID");
                    logInfo.setUserId(-1); // 设置为未知用户
                }

                // 4. 设置客户端IP
                logInfo.setClientIp(getClientIp(request));
            }
        } catch (Exception e) {
            log.warn("获取请求上下文失败: {}", e.getMessage());
            logInfo.setClientIp("获取IP失败");
        }

        // 5. 执行目标方法
        Object result = null;
        try {
            result = joinPoint.proceed();
            logInfo.setStatus("0"); // 操作成功
        } catch (Throwable ex) {
            logInfo.setStatus("1"); // 操作失败
            logInfo.setErrorMessage(truncateErrorMessage(ex.getMessage()));
            throw ex;
        } finally {
            // 6. 安全保存日志
            saveLogSafely(logInfo);
        }

        return result;
    }

    // 获取客户端IP方法
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "无请求信息";
        }

        try {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            // 处理多级代理的情况
            return ip.split(",")[0].trim();
        } catch (Exception e) {
            log.warn("获取客户端IP失败: {}", e.getMessage());
            return "获取IP失败";
        }
    }

    // 截断错误信息
    private String truncateErrorMessage(String message) {
        if (message == null) return null;
        return message.length() > 2000 ? message.substring(0, 2000) : message;
    }

    // 安全保存日志
    private void saveLogSafely(LogInfoDto logInfo) {
        try {
            log.debug("保存审计日志: {}", logInfo);
            boolean success = logInfoService.addLogInfo(logInfo);
            if (!success) {
                log.error("日志服务返回保存失败");
            }
        } catch (Exception e) {
            log.error("审计日志保存失败: {}", e.getMessage());
            log.error("失败日志内容: 操作类型={}, 用户ID={}",
                    logInfo.getOperationType(), logInfo.getUserId());
        }
    }
}

