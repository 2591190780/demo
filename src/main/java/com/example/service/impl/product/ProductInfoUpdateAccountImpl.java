package com.example.service.impl.product;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.ProductInfoAccountDto;
import com.example.mapper.product.ProductInfoUpdateAccountMapper;
import com.example.service.product.ProductInfoUpdateAccountService;
import com.example.utils.InfoToRedisUtils;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
public class ProductInfoUpdateAccountImpl extends ServiceImpl<ProductInfoUpdateAccountMapper, ProductInfoAccountDto>
        implements ProductInfoUpdateAccountService {

    @Resource
    JwtUtils utils;

    @Resource
    InfoToRedisUtils redisUtils;

    /**
     * 修改单个
     * @param request
     * @param account
     * @return
     * @param <T>
     */
    @Override
    public <T> RestBean<T> updateSingleProductInfo(HttpServletRequest request, ProductInfoAccountDto account){
        //验证角色是否正确（农户或者管理员）
        boolean verifyRole = utils.userRoleVerify(request);
        if(!verifyRole) return RestBean.forbidden("只有农户才能修改");
        //验证修改的产品 为当前用户下的 产品 （管理员不受限） -->农户只能修改自己的农产品
        Integer fid = account.getFarmerId();
        boolean verifyId = utils.getUserIdVerify(request,fid);
        if(!verifyId)return RestBean.forbidden("请检查农产品所属农户");

        if(update(account)) {
            redisUtils.InfoToRedis(account.getProductId(), account.getFarmerId()
                    , "update","product");
            return RestBean.success();
        }
        return  RestBean.failure(401,"参数有误");
    }

    @Override
    public <T> RestBean<T> updateSingleProductInfoAdmin(HttpServletRequest request, ProductInfoAccountDto account){
        //验证角色是否正确（农户或者管理员）
        boolean verifyRole = utils.userRoleVerifyAdmin(request);
        if(!verifyRole)return RestBean.forbidden("权限不足");
        if(productUpdateAdmin(account.getProductId(),account.getFarmerId(), account.getIsActive())) return RestBean.success();
        return  RestBean.failure(500,"参数有误");
    }

    /**
     * 管理员和农户本人可批量修改产品信息
     * @param request
     * @param accountList
     * @return
     * @param <T>
     */

    @Override
    public <T> RestBean<T> updateAllProductInfo(HttpServletRequest request
            , List<ProductInfoAccountDto> accountList){
        // 权限验证
        if (!utils.userRoleVerify(request)) {
            return RestBean.forbidden("只有农户才能修改价格");
        }
        // 批量更新
        try {
            boolean allSuccess = true;
            for (ProductInfoAccountDto account : accountList) {
                Integer fid = account.getFarmerId();
                if(!utils.getUserIdVerify(request,fid))
                    return  RestBean.failure(500,"无权限的操作，请检查农产品编号");
                // 对每个产品执行更新
                if (!update(account)) {
                    allSuccess = false;
                    // 记录失败日志（实际生产环境应更详细）
                    log.error(String.format("更新产品失败，产品ID:%s", account.getProductId()));
                }
                redisUtils.InfoToRedis(account.getProductId(), account.getFarmerId()
                        , "update","product");
            }
            return allSuccess ?
                    RestBean.success() :
                    RestBean.failure(400, "部分产品更新失败，请检查数据");
        } catch (Exception e) {
            log.error("批量更新产品异常", e);
            return RestBean.failure(500, "批量更新失败: " + e.getMessage());
        }
    }

    @Override
    public <T> RestBean<T> updateAllProductInfoAdmin(HttpServletRequest request
            , List<ProductInfoAccountDto> accountList){
        // 权限验证
        if (!utils.userRoleVerifyAdmin(request)) {
            return RestBean.forbidden("权限不足");
        }
        // 批量更新
        try {
            boolean allSuccess = true;
            for (ProductInfoAccountDto account : accountList) {
                // 对每个产品执行更新
                if (!productUpdateAdmin(account.getProductId(),account.getFarmerId(),account.getIsActive())) {
                    allSuccess = false;
                    // 记录失败日志（实际生产环境应更详细）
                    log.error(String.format("更新产品失败，产品ID:%s", account.getProductId()));
                }
            }
            return allSuccess ?
                    RestBean.success() :
                    RestBean.failure(400, "部分产品更新失败，请检查数据");
        } catch (Exception e) {
            log.error("批量更新产品异常", e);
            return RestBean.failure(500, "批量更新失败: " + e.getMessage());
        }
    }
    /**
     * 权限验证
     * @param
     * @return
     */

    //用户需要将update消息提交到redis队列中，等待管理员用户确认后生效
    public boolean update(ProductInfoAccountDto account){
        Integer productId = account.getProductId();
        Integer farmerId= account.getFarmerId();
        BigDecimal price = account.getPrice();
        BigDecimal stock = account.getStock();
        String imgURL = account.getProductImgurl();
        return  this.update()
                .eq("product_id",productId)
                .eq("farmer_id",farmerId)
                .set("price",price)
                .set("stock",stock)
                .set("is_active",0)
                .set("product_imgurl",imgURL)
                .update();
    }

    public boolean productUpdateAdmin(Integer productId,Integer farmerId,byte active){
        return  this.update()
                .eq("product_id",productId)
                .eq("farmer_id",farmerId)
                .set("is_active",active)
                .update();
    }

}
