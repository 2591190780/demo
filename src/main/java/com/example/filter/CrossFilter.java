package com.example.filter;

import com.example.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpClient;

@Component
@Order(Const.ORDER_CORS)
public class CrossFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest request
            , HttpServletResponse response
            , FilterChain chain) throws IOException, ServletException {

            this.addCorsHeader(request,response);  //添加跨域网址许可
        // 处理 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(request, response);
        }
    }
    public void addCorsHeader(HttpServletRequest request,HttpServletResponse response) {
        //response.addHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Max-Age", "3600"); // 预检结果缓存时间
    }
}
