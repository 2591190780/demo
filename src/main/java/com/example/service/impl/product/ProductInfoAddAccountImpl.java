package com.example.service.impl.product;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.ProductInfoAccountDto;

import com.example.entity.vo.request.ProductAddVO;
import com.example.mapper.product.ProductInfoAddAccountMapper;
import com.example.service.BlockChainEvidenceService;
import com.example.utils.IPFSUtils;
import com.example.service.product.ProductInfoAddAccountService;
import com.example.utils.BlockchainHashUtil;
import com.example.utils.InfoToRedisUtils;
import com.example.utils.JwtUtils;
import com.example.utils.MessageIntoIPFSUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductInfoAddAccountImpl extends ServiceImpl<ProductInfoAddAccountMapper, ProductInfoAccountDto>
        implements ProductInfoAddAccountService{

    @Resource
    JwtUtils utils;

    @Resource
    BlockchainHashUtil hashUtil;

    @Resource
    InfoToRedisUtils redisUtils;

    @Resource
    MessageIntoIPFSUtil messageIntoIPFSUtil;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    IPFSUtils ipfsUtils;

    @Resource
    BlockChainEvidenceService blockChainEvidenceService;

    @Override
    public <T> RestBean<T> addUserProductSingle(HttpServletRequest request, ProductAddVO vo){
        //验证角色是否正确（农户或者管理员）
        boolean verifyRole = utils.userRoleVerify(request);
        if(!verifyRole)return RestBean.forbidden("只有农户可以添加新产品");

        //验证增加的产品 为当前用户下的 产品 （管理员不受限）
        Integer fid = vo.getFarmerId();
        boolean verifyId = utils.getUserIdVerify(request,fid);
        if(!verifyId)return RestBean.forbidden("权限不足");
        //农户提交新产品的信息    此时需要等待管理员确认后才激活产品售卖
        if(this.generateProductAccount(vo)){
            redisUtils.InfoToRedis(vo.getProductId(), vo.getFarmerId()
                    , "add","product");
            return RestBean.success();
        }
        return RestBean.failure(401,"请检查传入的参数");
    }



    @Override
    public  RestBean<Void> addUserProductAll(HttpServletRequest request, List<ProductAddVO> voList){
        // 1. 验证角色（农户）
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
                vo.getStock(),vo.getStock(),//农户在刚添加产品的时候，库存剩余量肯定等于库存的。
                origin,
                certificationHash,
                now,
                now,
                active,
                null
        );
        if(this.save(dto)){
            /**
             * 这里要执行上链操作 ----->  blockChainEvidenceService
             * 如果信息存储成功--->生成区块链凭证初始信息--->调用合约进行上链操作
             *      --->区块链返回上链成功的区块号--->更新数据库的上链信息。
             */
            RestBean.success();
            vo.setProductId(dto.getProductId());
            return true;}
        RestBean.failure(500,"未知错误请联系管理员");
        return false ;
    }


}
