package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.catalina.UserDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    public DecodedJWT resolveJWT(String headertoken) {  //token解析并验证token的有效性
        String token = this.converToken(headertoken);
        if(token==null) return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT verify = jwtVerifier.verify(token);
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ? null : verify;
        }catch (JWTVerificationException e){
            return null;
        }

    }
    public String createJwt(UserDetails details,int id,String Username){  //创建jwt令牌，封装用户信息id username 有效日期 颁发日期
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expire = this.expireTime();
    return JWT.create()
            .withClaim("id",id)
            .withClaim("name",Username)
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
                .withUsername(claims.get("name").asString())
                .password("***")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }
    public Integer toId(DecodedJWT decodedJWT) {  //解析token提取id
        Map<String , Claim>claims = decodedJWT.getClaims();
        return claims.get("id").asInt();
    }
    private String converToken(String headertoken){  //验证前端发送的token，并返回
        if (headertoken ==null || !headertoken.startsWith("Bearer")){
            return null;
        }
        return headertoken.substring(7);
    }
}
