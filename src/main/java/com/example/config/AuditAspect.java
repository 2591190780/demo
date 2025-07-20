package com.example.config;

import com.example.annotation.Auditable;
import com.example.entity.dto.LogInfoDto;
import com.example.service.LogInfoService;
import com.example.utils.JwtUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
@Aspect
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    // 使用单线程池处理日志保存
    private static final Executor LOG_EXECUTOR = Executors.newSingleThreadExecutor();

    private final LogInfoService logInfoService;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        logInfo.setStatus("1");

        // 2. 获取HTTP请求
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // 3. 使用JwtUtils获取用户ID
                Integer userId = jwtUtils.getRequesetId(request);
                if (userId != null) {
                    logInfo.setUserId(userId);
                } else {
                    log.warn("无法从请求中获取用户ID");
                    logInfo.setUserId(-1); // 设置为未知用户
                }
                // 4. 设置客户端IP
                logInfo.setClientIp(
                        getClientIp(request)
                );
            }
        } catch (Exception e) {
            log.warn("获取请求上下文失败: {}", e.getMessage());
            logInfo.setClientIp("获取IP失败");
        }
        // 5. 执行目标方法并记录响应
        Object result = null;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            // 记录成功响应
            logInfo.setStatus("0");
            logInfo.setResponseContent(convertResponseToString(result)); // 记录响应内容
            return result;
        } catch (Throwable ex) {
            // 记录错误响应
            logInfo.setStatus("1");
            // 获取异常信息
            String errorMessage = truncateErrorMessage(ex.getMessage());
            logInfo.setErrorMessage(errorMessage);
            logInfo.setResponseContent(getExceptionResponse(ex)); // 记录异常响应内容
            throw ex;
        } finally {
            // 6. 安全保存日志（包含响应内容）- 使用线程池异步执行
            saveLogAsync(logInfo);
        }
    }

    // 将响应对象转换为字符串
    private String convertResponseToString(Object response) {
        if (response == null) {
            return "null";
        }
        try {
            // 如果是字符串直接返回
            if (response instanceof String) {
                return truncateString((String) response, 2000);
            }

            // 如果是ResponseEntity类型
            if (response instanceof ResponseEntity) {
                ResponseEntity<?> res = (ResponseEntity<?>) response;
                Object body = res.getBody();
                if (body instanceof String) {
                    return truncateString((String) body, 2000);
                }
                return convertObjectToJson(body);
            }
            // 其他类型转换为JSON
            return convertObjectToJson(response);
        } catch (Exception e) {
            log.warn("响应内容转换失败: {}", e.getMessage());
            return "响应内容解析失败: " + e.getMessage();
        }
    }

    // 获取异常响应内容
    private String getExceptionResponse(Throwable ex) {
        // 如果是自定义业务异常，可能有响应内容
        // 例如: if (ex instanceof BusinessException) ...

        // 默认返回异常消息
        return truncateErrorMessage(ex.getMessage());
    }

    // 将对象转换为JSON字符串
    private String convertObjectToJson(Object obj) {
        if (obj == null) return "null";

        try {
            return truncateString(objectMapper.writeValueAsString(obj), 2000);
        } catch (JsonProcessingException e) {
            return "JSON转换失败: " + e.getMessage();
        }
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

    // 截断长文本
    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }

    // 截断错误信息
    private String truncateErrorMessage(String message) {
        if (message == null) return null;
        return truncateString(message, 2000);
    }

    // 异步安全保存日志
    private void saveLogAsync(LogInfoDto logInfo) {
        LOG_EXECUTOR.execute(() -> {
            try {
                log.debug("保存审计日志: {}", logInfo);
                boolean success = logInfoService.addLogInfo(logInfo);
                if (!success) {
                    log.error("日志服务返回保存失败");
                    // 本地备份
                    writeLogToLocalFile(logInfo);
                }
            } catch (Exception e) {
                log.error("审计日志保存失败: {}", e.getMessage());
                log.error("失败日志内容: 操作类型={}, 用户ID={}",
                        logInfo.getOperationType(), logInfo.getUserId());

                // 本地备份
                writeLogToLocalFile(logInfo);
            }
        });
    }

    // 写入本地文件作为备份
    private void writeLogToLocalFile(LogInfoDto logInfo) {
        try {
            // 这里应该是实际的文件路径
            String logEntry = String.format("[%s] [%s] User:%d - %s | %s%n",
                    logInfo.getCreateTime(),
                    logInfo.getOperationType(),
                    logInfo.getUserId(),
                    logInfo.getStatus().equals("0") ? "SUCCESS" : "FAILURE",
                    logInfo.getResponseContent());
            // 实际项目中应使用正式日志系统或文件写入
            log.warn("本地备份日志: {}", logEntry);
        } catch (Exception e) {
            log.error("日志本地备份失败: {}", e.getMessage());
        }
    }
}