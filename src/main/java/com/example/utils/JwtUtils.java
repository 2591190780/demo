package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {

    private final StringHttpMessageConverter stringHttpMessageConverter;

    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;

    public JwtUtils(StringHttpMessageConverter stringHttpMessageConverter) {
        this.stringHttpMessageConverter = stringHttpMessageConverter;
    }

    public boolean invalidDateJWT(String headerToken) {
        String token = this.converToken(headerToken);
        if (token == null) {
            return false;
        }
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            DecodedJWT  jwt = verifier.verify(token);
            String id = jwt.getId();
            return deleteToken(id,jwt.getExpiresAt());
        }catch (JWTVerificationException exception){
            return false;
        }
    }

    private boolean deleteToken(String uuid,Date time) {
        if(this.isInvalidToken(uuid))return false;
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(), 0);
        template.opsForValue().set(Const.JWT_BLACK_LIST+uuid,"",expire, TimeUnit.MILLISECONDS);
        return true;
    }

    private  boolean isInvalidToken(String uuid) {
            return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST+uuid));
    }

    public DecodedJWT resolveJWT(String headerToken) {  //token解析并验证token的有效性
        String token = this.converToken(headerToken);
        if(token==null) return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            if(this.isInvalidToken(verify.getId())) return null; //失效返回空
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ? null : verify;  //过期返回空，通过返回token
        }catch (JWTVerificationException e){
            return null;
        }

    }
    public String createJwt(UserDetails details,int id,String role,String Username){  //创建jwt令牌，封装用户信息id username 有效日期 颁发日期
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expire = this.expireTime();
    return JWT.create()
            .withJWTId(UUID.randomUUID().toString())
            .withClaim("id",id)
            .withClaim("username",Username)
            .withClaim("role",role)
            .withClaim("authorities",details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
            .withExpiresAt(expire)
            .withIssuedAt(new Date())
//            .withExpiresAt(new Date())
            .sign(algorithm);
    }
    public Date expireTime(){   //有效期天数计算
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire * 24);
        return calendar.getTime();
    }

    public UserDetails toUser(DecodedJWT decodedJWT) { //解析token并封装给user类
        Map<String , Claim>claims = decodedJWT.getClaims();
        return User
                .withUsername(claims.get("username").asString())
                .password("***")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }
    public Integer toId(DecodedJWT decodedJWT) {  //解析token提取id
        Map<String , Claim>claims = decodedJWT.getClaims();
        return claims.get("id").asInt();
    }

    public String toRole(DecodedJWT decodedJWT) {
        Map<String , Claim>claims = decodedJWT.getClaims();
        return claims.get("role").asString();
    }

    private String converToken(String headertoken){  //验证前端发送的token，并返回
        if (headertoken ==null || !headertoken.startsWith("Bearer")){
            return null;
        }
        return headertoken.substring(7);
    }
}
