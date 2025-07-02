package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.dto.NFTInfoDto;
import com.example.entity.vo.response.PendingApplicationVO;
import com.example.mapper.AdminMapper;
import com.example.service.AccountService;
import com.example.service.AdminService;
import com.example.service.NFT.NFTInfoService;
import com.example.service.product.ProductInfoUpdateAccountService;
import com.example.service.sensor.SensorInfoUpdateService;
import com.example.utils.Const;
import com.example.utils.InfoToRedisUtils;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.time.Duration;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, PendingApplicationVO> implements AdminService {

    @Resource
    AccountService accountService;

    @Resource
    StringRedisTemplate template;

    @Resource
    InfoToRedisUtils redisUtils;

    @Resource
    JwtUtils utils;

    @Resource
    ProductInfoUpdateAccountService productInfoUpdateAccountService;

    @Resource
    SensorInfoUpdateService sensorInfoUpdateService;

    @Resource
    NFTInfoService nftInfoService;


    private static final Duration EXPIRE_DURATION = Duration.ofHours(24);

    @Override
    public List<PendingApplicationVO> getPendingApplications(HttpServletRequest request)  {
        if (!utils.userRoleVerifyAdmin(request)) {
            return null;
        }
        // 1. 获取所有操作类型
        String[] operationTypes = {Const.FARMER_ADD_APPLY_LIST, Const.FARMER_UPDATE_APPLY_LIST
                , Const.FARMER_DELETE_APPLY_LIST};

        List <PendingApplicationVO> pendingApplicationVOS = new ArrayList<>();
        for (String operation : operationTypes) {
            String applyListKey = "apply:" + operation ;
            int maxlength = applyListKey.length();
            // 2. 获取该操作类型下的所有复合键
            Set<String> compositeKeys = template.opsForSet().members(applyListKey);
            if (compositeKeys == null ||  compositeKeys.isEmpty()) continue;
            // 3. 处理每个申请
            for (String compositeKey : compositeKeys) {
                // 检查键是否有效（未过期）
                if (Boolean.FALSE.equals(template.hasKey(compositeKey))) {
                    // 从索引中移除过期键
                    template.opsForSet().remove(applyListKey, compositeKey);
                    continue;
                }
                // 4. 解析复合键
                String[] parts = compositeKey.split(":");
                if (!(parts.length <= maxlength))
                {
                    RestBean.failure(401,"内部错误，请联系管理员");
                    return null;
                }  // 确保格式正确
                String operationType = parts[1];
                String targetType = this.constConvertType(parts[2]);
                String targetId = parts[3]; //如果是userinfo 这里是 role
                String farmerId = parts[4];  // 如果操作目标是userinfo 这里是 id

                // 5. 获取农户信息
                Account farmer = accountService.findAccountById(Integer.parseInt(farmerId));
                Object targetInfo ;

                if(targetType.equals("userInfo")){
                    // 6. 获取目标对象详情（根据类型查询不同表）
                    farmer.setPassword(null);
                    targetInfo = farmer;

                }else {
                    targetInfo = redisUtils.getTargetInfo(targetType, targetId);
                }
                //redis缓存中 过期时间以ttl存储。需要反推。
                long expireMillis  =  template.opsForValue().getOperations().getExpire(applyListKey);
                long livedMillis = EXPIRE_DURATION.toSeconds() - expireMillis ;
                LocalDateTime createTime = LocalDateTime.now().minusSeconds(livedMillis);
                LocalDateTime deadTime = createTime.plusSeconds(EXPIRE_DURATION.toSeconds());

                // 7. 添加到结果列表
                pendingApplicationVOS.add(new PendingApplicationVO(
                        operationType,
                        targetType,
                        targetId,
                        farmerId,
                        farmer != null ? farmer.getUsername() : "未知农户",
                        createTime,
                        deadTime,
                        targetInfo
                ));

            }
        }
        return  pendingApplicationVOS;
    }

    @Override
    public boolean handleApplication(HttpServletRequest request,List<PendingApplicationVO> voList,byte answer) {
        // 1. 验证复合键格式
        // 1. 获取所有操作类型
        if (!utils.userRoleVerifyAdmin(request)) {
            RestBean.forbidden("权限不足");
            return false;
        }
        for (PendingApplicationVO vo : voList) {
            String applyListKey = "apply:" + vo.getOperation();

            String tarType = this.typeConvertConst(vo.getTargetType());
            /**
             * 如果传入入的是 申请修改用户角色信息 -->  "update" + Const.USER_ID_LIST + role + id
             */
            String compositeKey = applyListKey + ":" + tarType+ ":" + vo.getTargetId() + ":" + vo.getId();
            //检查 键 是否在redis缓存中
            if (!Boolean.TRUE.equals(template.hasKey(compositeKey))) {
                return false;
            }

            // 2. 解析复合键
            String operation = vo.getOperation();
            String targetId = vo.getTargetId();
            String farmerId = vo.getId();

            // 3. 执行实际业务逻辑
            this.objectConfirm(vo,answer);
            // 4. 从Redis中删除记录和索引
            redisUtils.deleteApplication(compositeKey, operation, tarType, farmerId);
//        // 5. 发送通知给农户
//        notifyFarmer(farmerId, operation, targetType, targetId, approved);
        }
        return true;

    }

    private String typeConvertConst(String type){
        return switch (type) {
        case "product" -> Const.PRODUCT_ID_LIST;
        case "sensor" -> Const.SENSOR_ID_LIST;
        case "nft_info" -> Const.NFT_ID_LIST;
        case "userInfo" -> Const.USER_ID_LIST;
        case "nft_rule" -> Const.NFT_RULE_ID_LIST;
        default -> null;
             };
        }

    private String constConvertType(String type){
        return switch (type){
            case Const.PRODUCT_ID_LIST -> "product"  ;
            case Const.SENSOR_ID_LIST -> "sensor";
            case Const.NFT_ID_LIST -> "nft_info";
            case Const.USER_ID_LIST -> "userInfo";
            case Const.NFT_RULE_ID_LIST -> "nft_rule";
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }


    private Object objectConfirm (PendingApplicationVO vo,byte ans){
        if (vo == null) return null;
        String operation = vo.getOperation();
        String targetType = vo.getTargetType();
        String targetId = vo.getTargetId();
        String farmerId = vo.getId();

        Object result = switch (targetType) {
            case "product" -> productInfoUpdateAccountService.productUpdateAdmin(
                    this.convertToInteger(targetId),
                    this.convertToInteger(farmerId),  ans);
            //case "sensor":
            case "userInfo" -> accountService.updateRoleAdmin(Integer.valueOf(farmerId),targetId);
            case "nft_info" -> nftInfoService.NFTInfoUpdateAdmin(
                    this.convertToInteger(targetId),ans);
            default -> throw new IllegalStateException("未知的数据类型" + targetType);
        };
        return result;

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
