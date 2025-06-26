package com.example.service.impl;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.ProductInfoAccountDto;

import com.example.entity.vo.request.ProductAddVO;
import com.example.mapper.ProductInfoAddAccountMapper;
import com.example.service.ProductInfoAddAccountService;
import com.example.utils.BlockchainHashUtil;
import com.example.utils.Const;
import com.example.utils.InfoToRedisUtils;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class ProductInfoAddAccountImpl extends ServiceImpl<ProductInfoAddAccountMapper, ProductInfoAccountDto>
        implements ProductInfoAddAccountService{

    @Resource
    JwtUtils utils;

    @Resource
    BlockchainHashUtil hashUtil;

    @Resource
    InfoToRedisUtils redisUtils;
    /**
     * 生成产品存证哈希
     *
     * @param farmerId 农户ID
     * @param name 产品名称
     * @param category 产品类别
     * @param originLocation 原产地
     * @param createTime 创建时间
     * @return 66字符的十六进制哈希值 (0x开头)
     */
    /**
     *
     * @param request
     * @param vo
     * @return
     * @param <T>
     */
    @Override
    public <T> RestBean<T> addUserProductSingle(HttpServletRequest request, ProductAddVO vo){
        //验证角色是否正确（农户或者管理员）
        boolean verifyRole = utils.userRoleVerify(request);
        if(!verifyRole)return RestBean.forbidden("只有农户可以添加新产品");

        //验证增加的产品 为当前用户下的 产品 （管理员不受限）
        Integer fid = vo.getFarmerId();
        boolean verifyId = this.getUserIdVerify(request,fid);
        if(!verifyId)return RestBean.forbidden("权限不足");

        //农户提交新产品的信息    此时需要等待管理员确认后才激活产品售卖(功能注释了)。
        if(this.generateProductAccount(vo)){
            redisUtils.InfoToRedis(vo.getProductId(), vo.getFarmerId()
                    , "add","product");
            return RestBean.success();
        }
        return RestBean.failure(401,"请检查传入的参数");
    }


    @Override
    public  RestBean<Void> addUserProductAll(HttpServletRequest request, List<ProductAddVO> voList){
        // 1. 验证角色（农户或管理员）
        if (!utils.userRoleVerify(request)) {
            return RestBean.forbidden("只有农户可以增加产品");
        }
        // 2. 获取当前用户ID（用于后续验证）
        Integer currentUserId = utils.toId(
                utils.resolveJWT(request.getHeader("Authorization"))
        );
        // 3. 批量验证产品归属权（非管理员需验证每个产品）
            for (ProductAddVO vo : voList) {
                if (!vo.getFarmerId().equals(currentUserId)) {
                    // 发现权限问题立即回滚事务

                    return RestBean.forbidden("请检查产品所属的农户");
                }
            }
        // 4. 批量创建产品
        for (ProductAddVO vo : voList) {
            if (!this.generateProductAccount(vo)) {// 任意产品添加失败时回滚整个事务
                throw new IllegalArgumentException("添加产品失败，请检查参数");}
            else{
            redisUtils.InfoToRedis(vo.getProductId(), vo.getFarmerId()
                    , "add","product");
            }
        }
        // 5. 全部成功时返回
        return RestBean.success();
    }

    private Boolean getUserIdVerify(HttpServletRequest request,Integer fid){
        return userIdVerify(request, fid, utils);
    }


    public static Boolean userIdVerify(HttpServletRequest request, Integer fid, JwtUtils utils) {
        String authorization = request.getHeader("Authorization");
        DecodedJWT jwt = utils.resolveJWT(authorization);
        String role = utils.toRole(jwt);
        Integer id = utils.toId(jwt);
        if (Objects.equals(role, "3")) return true;
        return id.equals(fid);
    }

    private Boolean generateProductAccount(ProductAddVO vo){
        if (vo == null) { RestBean.failure(401,"错误的参数类型"); return false;}
        //生成hash凭证
        Integer productId =  vo.getProductId();
        Integer farmerId = vo.getFarmerId();
        String name =  vo.getName();
        String category = vo.getCategory();
        String origin =  vo.getOriginLocation();
        LocalDateTime now = LocalDateTime.now();
        byte active = (byte) 0;
        String certificationHash = hashUtil.generateProductHash(
                farmerId, name, category , origin,now
        );
        ProductInfoAccountDto dto;
        dto = new ProductInfoAccountDto(
                productId,
                farmerId,
                name,
                category,
                vo.getPrice(),
                vo.getStock(),
                origin,
                certificationHash,
                now,
                now,
                active
        );

        if(this.save(dto)){
            RestBean.success();
            vo.setProductId(dto.getProductId());
            return true;}
        RestBean.failure(500,"未知错误请联系管理员");
        return false ;
    }


}
