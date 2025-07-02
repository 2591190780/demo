package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.controller.exception.CustomerException;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.entity.vo.request.ResetPasswordByPasswordVO;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
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

    @Resource
    JwtUtils jwtUtils;

    @Resource
    WalletBlockChainUtil walletBlockChainUtil;

    @Resource
    InfoToRedisUtils infoToRedisUtils;

    @Override
    public boolean updateRoleByApply(HttpServletRequest request,Account account){
        if (!Objects.equals(account.getId(), jwtUtils.getRequesetId(request))) return false;
        infoToRedisUtils.InfoToRedis(Integer.valueOf(account.getRole())
                ,account.getId(),"update","userInfo");
        return true;

    }

    @Override
    public boolean updateRoleAdmin(Integer id,String role){
        return this.update().eq("id",id)
                .set("role",role).update();

    }


    @Override
    public boolean updateImg(HttpServletRequest request ,Account account){
        if (ObjectUtils.isEmpty(account)){
            return false;
        }
        if(!account.getId().equals(jwtUtils.getRequesetId(request))) return false;

        return  this.update().eq("id",account.getId())
                .set("user_imgurl",account.getUserImgurl()).update();
    }

    @Override
    public Account findAccountById(Integer id){
        if (id==null)return null;
        return this.query()
                .eq("id",id)
                .one();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account =this.findAccountByNameOrEmail(username);
        if (account == null) throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    } //使用security的接口，校验传入的密码是否正确

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

        LocalDateTime sqlDate = LocalDateTime.now();
        /**
         * 后续需要由前端生成私钥和地址，传回地址
         * String address = emailRegisterVO.getWalletAddress();
         */
        Map<String,String> mapList =  walletBlockChainUtil.generateUserWallet();
        String key = mapList.get("privateKey");
        String address = mapList.get("address");

        Account account = new Account(null,
                username,
                encodePassword,
                email,
                "2",
                sqlDate,
                address,
                null
                );
        if(this.save(account)){
            this.RedisClearCode(email);
            return address + ":"+ key;
        }else{
            return "内部错误，请联系管理员";
        }

    }

    @Override
    public  String resetEmailAccountPassword(EmailResetVO emailResetVO) {
        String email = emailResetVO.getEmail();
        //判断传入验证码的状态
        String verify = this.resetConfirm(new ConfirmResetVO(email,emailResetVO.getCode()));
        if(verify != null) return verify;
        String password = Encoder.encode(emailResetVO.getPassword());
        boolean update = this.update().eq("email", email).set("password", password).update();
        if (update){
            this.RedisClearCode(email);
        }
        return null;

    }

    @Override
    public String resetConfirm(ConfirmResetVO confirmVO){
        //获取传入的email
        String email = confirmVO.getEmail();
        //获取redis中的验证码
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
        //未查询到或者验证码错误返回异常
        if(code==null)return "请先获取密码";
        if(!code.equals(confirmVO.getCode()))return "验证码错误";
        return  null;
    }

    @Override
    public String resetPasswordByPassword(ResetPasswordByPasswordVO vo, HttpServletRequest request){

        String username = vo.getUsername();
        String oldPassword = vo.getOldPassword();
        String newPassword = vo.getNewPassword();
        if(Objects.equals(username, "") || Objects.equals(oldPassword, "") || newPassword ==null) return "用户名密码不能为空";
        // 1. 查询用户但不验证密码（只获取存储的加密密码）
        Account account = this.query().eq("username", username).one();
        if (account == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        // 2. 验证旧密码（使用matches方法）
        if (!Encoder.matches(oldPassword, account.getPassword())) {
            throw new BadCredentialsException("旧密码错误");
        }
        //使当前令牌失效
        String authorization = request.getHeader("Authorization");
            // 同时验证旧密码并更新新密码
        boolean updated = this.update()
                    .eq("id", account.getId())  // 使用主键更安全
                    .eq("password", account.getPassword()) // 确保密码未变化
                    .set("password", Encoder.encode(newPassword))
                    .update();
        if (Boolean.TRUE.equals(updated) & authorization != null){
            jwtUtils.invalidDateJWT(authorization);
            return  null ;
        }else if(Boolean.TRUE.equals(updated)){
            return null;
        }else{
            return "修改失败";
        }
    }



    public void RedisClearCode(String email){
        stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
        stringRedisTemplate.delete(Const.VERIFY_EMAIL_LIMIT + email);
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
