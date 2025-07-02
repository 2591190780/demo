package com.example.utils;



import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 *    暂时没什么用的工具类
 */
@Component
public class MessageIntoIPFSUtil {

    @Resource
    StringRedisTemplate template;

    @Resource
    JwtUtils jwtUtils;
    /**
     * 前端点击上传图片-->执行IPFS工具类中的Reids将信息存储到redis中等待处理-->
     * 若新产品创建成功-->则将图片提交IPFS然后得到URL存储到数据库中
     *
     * @param operationType
     * @return
     */

    public String sendMessageToRedis(HttpServletRequest request, String operationType) {
        /**
         * 此处需要判断传入的类型是 product 还是  nft 前端传入 operationType “product” “nft”
         *  将信息存储redis中  name + uuid : id
         */
        String uuid = String.valueOf(UUID.randomUUID());
        String  id = jwtUtils.getRequesetId(request).toString();
        String opName = this.typeChoose(operationType,id);
        if (opName == null) { return null; }
        template.opsForValue().set(opName,uuid,10, TimeUnit.MINUTES);
        return opName;
    }

    public Boolean clearMessageToRedis(HttpServletRequest request , String operationType,String uuid) {
            String  id = jwtUtils.getRequesetId(request).toString();
            String opName = this.typeChoose(operationType,uuid);
            if (opName == null) { return false; }
            template.delete(id);
            return true;
    }

    public   String typeChoose(String operationType,String id){
        return switch (operationType){
            case "product" -> Const.IMG_FOR_PRODUCT+":"+ id;
            case  "nft" ->  Const.IMG_FOR_NFT+":"+ id;
            case "user" -> Const.IMG_FOR_USER+":"+ id;
            default -> null;
        };
    }

}
