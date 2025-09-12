package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dto.*;
import com.example.entity.vo.response.PendingApplicationVO;
import com.example.mapper.AdminMapper;
import com.example.service.AccountService;
import com.example.service.AdminService;
import com.example.service.NFT.NFTInfoService;
import com.example.service.NFT.NFTRuleService;
import com.example.service.NFT.NFTTransactionService;
import com.example.service.blockchain.MessageReportService;
import com.example.service.product.ProductInfoSelectAccountService;
import com.example.service.product.ProductInfoUpdateAccountService;
import com.example.service.sensor.SensorInfoUpdateService;
import com.example.utils.Const;
import com.example.utils.InfoToRedisUtils;
import com.example.utils.JwtUtils;
import com.example.utils.WeBaseUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    ProductInfoSelectAccountService productInfoSelectAccountService;
    @Resource
    SensorInfoUpdateService sensorInfoUpdateService;
    @Resource
    NFTInfoService nftInfoService;
    @Resource
    NFTRuleService nftRuleService;
    @Resource
    MessageReportService messageReportService;
    @Resource
    WeBaseUtils weBaseUtils;
    @Resource
    NFTTransactionService nftTransactionService;

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
                long expireMillis  =  template.opsForValue().getOperations().getExpire(compositeKey);
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
    public List<PendingApplicationVO> getPendingApplyCategory(HttpServletRequest request, String Type){
        if (!utils.userRoleVerifyAdmin(request)) {
            return null;
        }
        // 1. 获取所有操作类型
        String[] operationTypes = {Const.FARMER_ADD_APPLY_LIST, Const.FARMER_UPDATE_APPLY_LIST
                , Const.FARMER_DELETE_APPLY_LIST};
        List <PendingApplicationVO> pendingApplicationVOS = new ArrayList<>();
        for (String operation : operationTypes) {
            String applyListKey = "apply:" + operation ;
            // 2. 获取该操作类型下的所有复合键
            Set<String> compositeKeys = template.opsForSet().members(applyListKey);
            if (compositeKeys == null ||  compositeKeys.isEmpty()) continue;
            for (String compositeKey : compositeKeys) {
                // 检查键是否有效（未过期）
                if (Boolean.FALSE.equals(template.hasKey(compositeKey))) {
                    // 从索引中移除过期键
                    template.opsForSet().remove(applyListKey, compositeKey);
                    continue;
                }
                // 4. 解析复合键
                String[] parts = compositeKey.split(":");
                String operationType = parts[1];
                String targetType = this.constConvertType(parts[2]);
                String targetId = parts[3]; //如果是userinfo 这里是 role
                String farmerId = parts[4];  // 如果操作目标是userinfo 这里是 id
                if(targetType.equals(Type)){
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
                    long expireMillis  =  template.opsForValue().getOperations().getExpire(compositeKey);
                    long livedMillis = EXPIRE_DURATION.toSeconds() - expireMillis ;
                    LocalDateTime createTime = LocalDateTime.now().minusSeconds(livedMillis);
                    LocalDateTime deadTime = createTime.plusSeconds(EXPIRE_DURATION.toSeconds());
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
        }
        return  pendingApplicationVOS;
    }

    @Override
    public boolean handleApplication(HttpServletRequest request,List<PendingApplicationVO> voList,byte answer)
            throws Exception {
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

    private Object objectConfirm (PendingApplicationVO vo,byte ans) throws Exception {
        if (vo == null) return null;
        String operation = vo.getOperation();
        String targetType = vo.getTargetType();
        String targetId = vo.getTargetId();
        String farmerId = vo.getId();
        //这里要获取到目标用户的钱包地址信息，然后进行上链操作。（已完成）
        String userAddress = this.accountService.findAccountById(convertToInteger(farmerId)).getWalletAddress();
        //构建上传参数
        List<Object> params = new ArrayList<>();
        Object result = switch (targetType) {
            case "product" -> {
                //只有在添加产品的时候才会触发上链，否则不会上链。
                if(ans == (byte) 1 && Objects.equals(operation, Const.FARMER_ADD_APPLY_LIST)){
                    //产品上链操作  获取上链产品的hash值
                String hash = this.productInfoSelectAccountService
                        .getProductInfoAccountByProductId(this.convertToInteger(targetId)).getCertificationHash();
                    params.add(0,1);
                    params.add(1,convertToInteger(targetId));
                    params.add(2,hash);
                    //执行上链操作
                messageReportService.blockChainEvidenceReport(Const.CONTRACT_FOR_MESSAGE_REPORT_METHOD_ADD_EVIDENCE
                        ,params
                        ,userAddress,Const.CONTRACT_FOR_MESSAGE_REPORT);


                }
                yield this.productInfoUpdateAccountService.productUpdateAdmin(
                        this.convertToInteger(targetId),
                        this.convertToInteger(farmerId),  ans);
            }
            case "userInfo" -> this.accountService.updateRoleAdmin(convertToInteger(farmerId),targetId);
            case "nft_info" ->{
                if(ans == (byte) 1 && Objects.equals(operation, Const.FARMER_ADD_APPLY_LIST)){
                    //检查图片信息是否已经存在CID值了
                    NFTInfoDto nftInfoDto = this.nftInfoService.NFTInfoSelectByTemplateId(convertToInteger(targetId));
                    Integer publicID = nftInfoDto.getPublicBy();
                    Integer nftID = nftInfoDto.getTemplateId();
                    String cid = nftInfoDto.getImageUrl();
                    if (cid ==null) yield null;
                    String nftContractAddress = Const.CONTRACT_FOR_NFT_INFO;
                    String ipfs = "ipfs/"+cid;
                    String metadata = nftInfoDto.getMetadataUrl();
                    BigDecimal price = this.theLatestNFTPrice(nftID);

                    List<Object> param = new ArrayList<>();
                    param.add(0,nftID);
                    param.add(1,nftContractAddress);
                    param.add(2,ipfs);
                    param.add(3,metadata);
                    param.add(4,price);
                    String ownerAddress = this.accountService.findAccountById(publicID).getWalletAddress();
                    String methodName = Const.CONTRACT_FOR_NFT_INFO_METHOD_STORENFTINFO;
                    //service的update操作已经做了权限验证
                    /**
                     * 这里要对NFT信息进行上链。
                     */
                    weBaseUtils.callContractMethod(ownerAddress,nftContractAddress,methodName,param);
                    //this.nftInfoService.nftUpdateContractAdmin(nftContractAddress,nftID);
                }
                yield this.nftInfoService.NFTInfoUpdateAdmin(this.convertToInteger(targetId), ans);
            }

            case "nft_rule" ->//管理员同意了此规则，需要是上传规则至区块链合约上方法在ConditionNFTRule.XXXreport.
                    this.nftRuleService.nftRuleUpdateAdmin(convertToInteger(targetId),ans);


            case "sensor" -> this.sensorInfoUpdateService.updateSensorInfoDtoadmin(
                    new SensorInfoDto(
                                    this.convertToInteger(targetId),
                                    this.convertToInteger(farmerId),null,null
                                    ,ans,null,null)
            );

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
    private BigDecimal theLatestNFTPrice(Integer nftID){
        NFTTransactionDto dto = this.nftTransactionService.selectOrderByTime(nftID);
        if (dto == null) return BigDecimal.ZERO;
        return  dto.getPrice();
    }
}
