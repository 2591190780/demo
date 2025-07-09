package com.example.utils;

import com.example.entity.RestBean;
import com.example.service.AccountService;
import com.example.service.NFT.NFTInfoService;
import com.example.service.NFT.NFTRuleService;
import com.example.service.product.ProductInfoSelectAccountService;
import com.example.service.sensor.SensorInfoSelectService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class InfoToRedisUtils {

    @Resource
    StringRedisTemplate template;

    @Resource
    ProductInfoSelectAccountService productSelectAccountService;

    @Resource
    SensorInfoSelectService sensorInfoSelectService;

    @Resource
    NFTInfoService nftInfoService;

    @Resource
    NFTRuleService nftRuleService;

    public <T> RestBean<T> InfoToRedis(Integer id, Integer farmerId,
                                       String operationType, String targetType) {
        // 确定操作类型和目标类型常量
        String applyListKey = switch (operationType) {
            case "add" -> "apply:"+Const.FARMER_ADD_APPLY_LIST;
            case "update" -> "apply:"+Const.FARMER_UPDATE_APPLY_LIST;
            case "delete" -> "apply:"+Const.FARMER_DELETE_APPLY_LIST;
            default -> throw new IllegalStateException("无效的操作类型: " + operationType);
        };
        String targetListKey = switch (targetType) {
            case "product" -> Const.PRODUCT_ID_LIST;
            case "sensor" -> Const.SENSOR_ID_LIST;
            case "nft_info" -> Const.NFT_ID_LIST;
            case "userInfo"-> Const.USER_ID_LIST;
            case "nft_rule"-> Const.NFT_RULE_ID_LIST;
            default -> throw new IllegalStateException("无效的目标类型: " + targetType);
        };
        // 创建复合键防止冲突
        /**
         * 如果传入入的是 申请修改用户角色信息 -->  "update" + Const.USER_ID_LIST + role + id
         */
        String compositeKey = applyListKey +":" +targetListKey + ":" + id + ":" + farmerId;

        // 检查是否已存在相同的申请 (使用复合键检查)
        if (Boolean.TRUE.equals(template.hasKey(compositeKey))) {
            return RestBean.failure(401, "请等待管理员处理您的申请，请勿重复提交");
        }
        // 存储申请信息（使用复合键）
        template.opsForValue().set(
                compositeKey,
                farmerId.toString(),
                1, TimeUnit.DAYS
        );
        // 存储到操作类型集合（用于快速查找某种操作的所有申请）
        template.opsForSet().add(applyListKey, compositeKey);
        template.expire(applyListKey, 1, TimeUnit.DAYS);

        // 存储到目标类型集合（用于快速查找某类目标的所有申请）
        template.opsForSet().add(targetListKey, compositeKey);
        template.expire(targetListKey, 1, TimeUnit.DAYS);

        // 创建农户索引（关键改进）
        String farmerIndexKey = Const.FARMER_INDEX + farmerId;
        template.opsForSet().add(farmerIndexKey, compositeKey);

        template.expire(farmerIndexKey, 1, TimeUnit.DAYS);

        return RestBean.success();
    }

    /**
     * 从Redis中删除申请记录
     */

    public void deleteApplication(String compositeKey, String operation,
                                  String targetType, String farmerId) {
        // 删除主记录
        template.delete(compositeKey);

        // 从操作类型索引中删除
        String applyListKey = "apply:" + operation;
        template.opsForSet().remove(applyListKey,compositeKey);

        template.opsForSet().remove(targetType,compositeKey);

        // 从农户索引中删除
        String farmerIndexKey = Const.FARMER_INDEX + this.convertToInteger(farmerId);
        template.opsForSet().remove(farmerIndexKey, compositeKey);
    }


    /**
     * 根据目标类型查询详细信息
     */
    public Object getTargetInfo(String targetType, String targetId) {
        try {
            int id = Integer.parseInt(targetId);
            //查询信息
            switch (targetType) {
                case "product":
                    return productSelectAccountService.getProductInfoAccountByProductId(id);
                case "sensor":
                    return sensorInfoSelectService.getSensorInfoBySensorId(id);
                case "nft_info":
                    return nftInfoService.NFTInfoSelectByTemplateId(id);
                case "nft_rule": return  nftRuleService.nftRuleSelectByID(id);

                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private Integer convertToInteger (String value){
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null; // 或者记录日志
        }
    }



}
