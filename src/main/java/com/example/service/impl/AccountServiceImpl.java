package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.controller.exception.CustomerException;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    FlowUtils flowUtils;

    @Resource
    AmqpTemplate  amqpTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    PasswordEncoder Encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account =this.findAccountByNameOrEmail(username);
        if (account == null) throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    @Override
    public void registerEmailVerifyCode(String type, String email, String ip) {
        //redis查询是否已发送邮件
        String exist_email= stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_LIMIT + email);
        //发过,返回异常
        if(!ObjectUtils.isEmpty(exist_email))
        {
            throw new CustomerException("请求频繁，请稍后再试");
        }
        //生成验证码,存入reids
        Random random = new Random();
        int code = random.nextInt(899999) + 100000;
        stringRedisTemplate.opsForValue()
                .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue()
                .set(Const.VERIFY_EMAIL_LIMIT + email, String.valueOf(code), 1, TimeUnit.MINUTES);
        //发送验证码
        Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            amqpTemplate.convertAndSend("mail", data);

    }
    @Override
    public String registerEmailAccount(EmailRegisterVO emailRegisterVO){
        String email = emailRegisterVO.getEmail();
        String username = emailRegisterVO.getUsername();
        String password = emailRegisterVO.getPassword();

        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA+email);
        if (code==null) return "请先获取验证码";

        if (!code.equals(emailRegisterVO.getCode())) return "验证码错误";
        if(this.existAccountByEmail(email) )  return "邮箱已注册";
        if(this.existAccountByUsername(username)) return "用户名已存在";
        String encodePassword =  Encoder.encode(password);
        java.util.Date utilDate = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
        Account account = new Account(null,
                username,
                encodePassword,
                email,
                "2",
                sqlDate);
        if(this.save(account)){
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_LIMIT + email);
            return null;
        }else{
            return "内部错误，请联系管理员";
        }

    }
    private boolean existAccountByEmail(String email){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email",email));
    }

    private boolean existAccountByUsername(String username){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username",username));
    }



    public Account findAccountByNameOrEmail(String text){
        return this.query()
                .eq("username",text).or()
                .eq("email",text)
                .one();
    }

    private boolean verifyLimit(String ip){
        String key=Const.VERIFY_EMAIL_LIMIT+ip;
        return flowUtils.limitOnceCheck(key,60);
    }

}
