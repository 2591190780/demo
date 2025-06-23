package com.example.filter;


import com.example.entity.RestBean;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Order(Const.ORDER_LIMIT)
public class FlowLimitFilter extends HttpFilter {

    @Resource(name = "stringRedisTemplate")
    StringRedisTemplate redisTemplate;

//    @Resource
//    StringRedisTemplate redisTemplate;

    @Override
    protected void doFilter(HttpServletRequest servletRequest
            , HttpServletResponse servletResponse
            , FilterChain filterChain) throws IOException, ServletException {

        String  addr = servletRequest.getRemoteAddr();
        if(this.tryCount(addr)){
            filterChain.doFilter(servletRequest, servletResponse);
        }
        else{
            this.writeBlockMessage(servletResponse);
        }

    }

    private  void writeBlockMessage(HttpServletResponse servletResponse) throws IOException {
        servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        servletResponse.setContentType("application/json;charset=utf-8");
        servletResponse.getWriter().write(RestBean.forbidden("操作频繁，请稍后再试").asJsonString());
    }

    private  boolean tryCount(String ip){
        if(Boolean.TRUE.equals(redisTemplate.hasKey(Const.FLOW_LIMIT_BLOCK + ip))){
            return false;
        }
        return this.limitPeriodCheck(ip);
    }
    private  boolean limitPeriodCheck(String ip){
        if(Boolean.TRUE.equals(redisTemplate.hasKey(Const.FLOW_LIMIT_COUNT + ip))){
            long increment= Optional.ofNullable(
                    redisTemplate.opsForValue().increment(Const.FLOW_LIMIT_COUNT+ip)
            ).orElse(0L);
            if (increment>10){
                redisTemplate.opsForValue().set(Const.FLOW_LIMIT_BLOCK+ip,""
                        ,30,TimeUnit.SECONDS);
                return false;
            }
        }else {
            redisTemplate.opsForValue().set(Const.FLOW_LIMIT_COUNT+ip,"1"
                    ,3, TimeUnit.SECONDS);
        }
        return true;
    }



}
