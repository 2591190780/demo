package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.PendingApplication;
import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.response.PendingApplicationVO;

import com.example.mapper.AdminMapper;
import com.example.service.AccountService;
import com.example.service.AdminService;
import com.example.service.ProductInfoUpdateAccountService;
import com.example.utils.Const;
import com.example.utils.InfoToRedisUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, PendingApplicationVO> implements AdminService {


    @Resource
    AccountService accountService;

    @Resource
    StringRedisTemplate template;

    @Resource
    InfoToRedisUtils redisUtils;

    private static final Duration EXPIRE_DURATION = Duration.ofHours(24);

    public List<PendingApplicationVO> getPendingApplications()  {
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
                String targetType = switch (parts[2]){
                    case Const.PRODUCT_ID_LIST -> "product"  ;
                    case Const.SENSOR_ID_LIST -> "sensor";
                    case Const.NFT_ID_LIST -> "nft";
                    default -> throw new IllegalStateException("Unexpected value: " + parts[2]);
                };
                String targetId = parts[3];
                String farmerId = parts[4];
                // 5. 获取农户信息
                Account farmer = accountService.findAccountById(Integer.parseInt(farmerId));
                // 6. 获取目标对象详情（根据类型查询不同表）
                Object targetInfo = redisUtils.getTargetInfo(targetType, targetId);

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

    public boolean handleApplicationSingle(PendingApplicationVO vo) {
        // 1. 验证复合键格式
        // 1. 获取所有操作类型
        String applyListKey = "apply:"+vo.getOperation();
        String compositeKey = applyListKey +":"+vo.getTargetType()+":"+vo.getTargetId()+":"+vo.getFarmerId();
        if (!compositeKey.startsWith("apply:") ) {
            return false;
        }
        // 2. 解析复合键
        String operation = vo.getOperation();
        String targetType = vo.getTargetType();
        String targetId = vo.getTargetId();
        String farmerId = vo.getFarmerId();
        // 3. 执行实际业务逻辑


        // 4. 从Redis中删除记录和索引
        redisUtils.deleteApplication(compositeKey, operation, targetType, farmerId);

//        // 5. 发送通知给农户
//        notifyFarmer(farmerId, operation, targetType, targetId, approved);

        return true;
    }

}
