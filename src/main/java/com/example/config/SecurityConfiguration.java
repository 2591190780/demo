package com.example.config;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.response.AuthorizeVO;
//import com.example.filter.CrossFilter;
import com.example.filter.JwtAuthorizeFilter;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import com.fasterxml.jackson.databind.util.BeanUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.View;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@Configuration
public class SecurityConfiguration {
    @Resource
    JwtUtils utils;
    @Resource
    JwtAuthorizeFilter jwtAuthorizeFilter;

    @Resource
    AccountService accountService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, View error) throws Exception {
        return http
                .authorizeHttpRequests(conf -> conf
                        .requestMatchers(
                                "/doc.html",
                                "/webjars/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll() //knife4j相关接口
                        .requestMatchers("api/auth/**","/error").permitAll()
                        .anyRequest().authenticated()
                )  //login请求放行
                .formLogin(conf -> conf
                        .loginProcessingUrl("/api/auth/login")
                        .failureHandler(this::onAuthenticationFailure)
                        .successHandler(this::onAuthenticationSuccess)
                )
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                .exceptionHandling(conf->conf
                        .authenticationEntryPoint(this::onUnauthorized)
                        .accessDeniedHandler(this::onAccessDeny)
                )

                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(conf->conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)  //token验证过滤器
                .build();

    }


    public void onAuthenticationSuccess(HttpServletRequest request,   //登陆成功返回的json消息
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        User user = (User) authentication.getPrincipal();
        Account account =accountService.findAccountByNameOrEmail(user.getUsername());
        String token  = utils.createJwt(user,account.getId(),account.getUsername());  //封装用户token信息
        AuthorizeVO vo = new AuthorizeVO();
        //BeanUtils.copyProperties();
        vo.setExpire(utils.expireTime());
        System.out.println(utils.expireTime());
        vo.setToken(token);
        vo.setUsername(account.getUsername());
        vo.setEmail(account.getEmail());
        vo.setDate(String.valueOf(new Date(System.currentTimeMillis())));
        String role = account.getRole();
        switch (role) {
            case "1":
                vo.setRole(Const.ROLE_OF_USER_MERCHANT);
                break;
            case "2":
                vo.setRole(Const.ROLE_OF_USER_ORDINARY);
                break;
            case "3":
                vo.setRole(Const.ROLE_OF_USER_ADMINISTRATOR);
                break;
            default:
                vo.setRole("WARRING:NO_ROLE");
                break;
        }
        response.getWriter().write(RestBean.success(vo).asJsonString()); //返回前端json消息

    }

    public void onLogoutSuccess(HttpServletRequest request,  //登出返回的json消息
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        String authenticationHeader = request.getHeader("Authorization");

        if(utils.invaliddateJWT(authenticationHeader)){
            writer.write(RestBean.success().asJsonString());
        }else {
            writer.write(RestBean.failure(400,"退出登录失败").asJsonString());
        }
    }



    public void onAccessDeny(HttpServletRequest request //权限验证的消息提示
            , HttpServletResponse response
            , AccessDeniedException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.forbidden(exception.getMessage()).asJsonString());
    }
    public  void  onUnauthorized(HttpServletRequest request, //未验证时的消息提示
                                 HttpServletResponse response,
                                 AuthenticationException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString());
    }

    public void onAuthenticationFailure(HttpServletRequest request, //登陆失败返回的json消息
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RestBean.failure(401,exception.getMessage()).asJsonString());
        //System.out.println(exception.getMessage());
    }

}
