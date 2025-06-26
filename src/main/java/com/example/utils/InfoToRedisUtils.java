package com.example.utils;

import com.example.entity.RestBean;
import com.example.entity.vo.request.ProductAddVO;
import com.example.service.ProductInfoSelectAccountService;
import com.example.service.SensorInfoSelectService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.events.Event;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class InfoToRedisUtils {

    @Resource
    StringRedisTemplate template;

    @Resource
    ProductInfoSelectAccountService selectAccountService;

    @Resource
    SensorInfoSelectService selectInfoService;

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
            case "nft" -> Const.NFT_ID_LIST;
            default -> throw new IllegalStateException("无效的目标类型: " + targetType);
        };
        // 创建复合键防止冲突
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

    public  <T> RestBean<T> InfoConfirm () {

        return null;
    }

    public Set<String> getKeysByFarmerId(Integer farmerId) {
        // 1. 获取农户索引键
        String farmerIndexKey = Const.FARMER_INDEX + farmerId;
        // 2. 获取该农户的所有申请键
        Set<String> compositeKeys = template.opsForSet().members(farmerIndexKey);
        if (compositeKeys == null || compositeKeys.isEmpty()) {
            return Collections.emptySet();
        }
        // 3. 过滤已过期的键
        Set<String> validKeys = new HashSet<>();
        for (String key : compositeKeys) {
            if (Boolean.TRUE.equals(template.hasKey(key))) {
                validKeys.add(key);
            } else {
                // 清理过期键
                template.opsForSet().remove(farmerIndexKey, key);
                // 从操作类型集合中移除
                String operationType = key.split(":")[0];
                template.opsForSet().remove(operationType, key);
                // 从目标类型集合中移除
                String targetType = key.split(":")[1];
                template.opsForSet().remove(targetType, key);
            }
        }
        return validKeys;
    }

    /**
     * 根据目标类型查询详细信息
     */
    public Object getTargetInfo(String targetType, String targetId) {
        try {
            int id = Integer.parseInt(targetId);

            switch (targetType) {
                case "product":
                    return selectAccountService.getProductInfoAccountByProductId(id);
//                case "sensor":
//                    return selectInfoService  (id);
//                case "nft":
//                    return nftTemplateService.getNftTemplateById(id);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
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
        template.opsForSet().remove(applyListKey, compositeKey);

        // 从目标类型索引中删除
        String targetListKey = "target:" + targetType;
        template.opsForSet().remove(targetListKey, compositeKey);

        // 从农户索引中删除
        String farmerIndexKey = Const.FARMER_INDEX + farmerId;
        template.opsForSet().remove(farmerIndexKey, compositeKey);
    }

    /**
     * 通知农户处理结果
     */
//    private void notifyFarmer(String farmerId, String operation,
//                              String targetType, String targetId, boolean approved) {
//        String message = String.format(
//                "您的%s%s申请(ID:%s)已被管理员%s",
//                getChineseTargetType(targetType),
//                getChineseOperation(operation),
//                targetId,
//                approved ? "批准" : "拒绝"
//        );
//
//        notificationService.sendNotification(
//                Integer.parseInt(farmerId),
//                approved ? "申请批准通知" : "申请拒绝通知",
//                message
//        );
//    }

    // 辅助方法：获取中文操作类型
    private String getChineseOperation(String operation) {
        return switch (operation) {
            case "add" -> "添加";
            case "update" -> "更新";
            case "delete" -> "删除";
            default -> operation;
        };
    }

    // 辅助方法：获取中文目标类型
    private String getChineseTargetType(String targetType) {
        return switch (targetType) {
            case "product" -> "商品";
            case "sensor" -> "传感器";
            case "nft" -> "NFT";
            default -> targetType;
        };
    }

}
