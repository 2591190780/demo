package com.example.service.impl.transaction;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.TransactionAccountDto;
import com.example.mapper.transaction.TransactionProcessMapper;
import com.example.service.transaction.TransactionProcessService;
import com.example.utils.BlockchainHashUtil;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionProcessImpl extends ServiceImpl<TransactionProcessMapper, TransactionAccountDto>
        implements TransactionProcessService {

    @Resource
    JwtUtils jwtUtils;

    @Resource
    BlockchainHashUtil blockchainHashUtil;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public  Boolean TransactionInfoAdd(TransactionAccountDto dto, HttpServletRequest request){

        //前端传入 卖家id，产品id，数量，总价，后端生成交易hash，订单号，以及下单时间。
        Integer requesetId = jwtUtils.getRequesetId(request);
        dto.setBuyerId(requesetId);
        String order_id = generateOrderId();
        dto.setOrderId(order_id);
        dto.setCompleteTime(LocalDateTime.now());

        String hash = blockchainHashUtil.generateTransactionHash(dto);
        dto.setCertificationHash(hash);
        if(TransactionMessageIntoRedis(dto)){
            dto.setStatus("1");
            return this.save(dto);
        }
        return false;
    }

    @Override
    public  Boolean TransactionInfoAddMulti(List<TransactionAccountDto> dtoList){

        for(TransactionAccountDto dto : dtoList){
            //前端传入 买家id，卖家id，产品id，数量，总价，后端生成交易hash，订单号，以及下单时间。
            String order_id = generateOrderId();
            dto.setOrderId(order_id);
            String hash = blockchainHashUtil.generateTransactionHash(dto);
            boolean flag =false;
            if(TransactionMessageIntoRedis(dto)){
                dto.setCertificationHash(hash);
                dto.setStatus("1");
                dto.setOrderTime(LocalDateTime.now());
                flag =  this.save(dto);
            }
            if(!flag){
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean transactionStatusUpdate(TransactionAccountDto dto,String status){
        // 更新订单状态
        UpdateWrapper<TransactionAccountDto> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_id", dto.getOrderId());

        // 设置更新字段
        updateWrapper
                .set("status", dto.getStatus())
                .set("alipay_order", dto.getAlipayOrder())
                .set("actual_payment", dto.getActualPayment())
                .set("complete_time", LocalDateTime.now());

        // 执行更新
        boolean updateResult = this.update(updateWrapper);

        // 如果更新成功且状态为已支付，清理Redis
        if (updateResult && "2".equals(dto.getStatus())) {
            this.cleanTransactionFromRedis(dto.getBuyerId(), dto.getCertificationHash());
        }

        return updateResult;
    }





    private void cleanTransactionFromRedis(Integer buyerId, String hash) {
        // 清理Redis中的订单信息
        String buyerKey = buyerId.toString();
        stringRedisTemplate.opsForSet().remove(buyerKey, hash);
        stringRedisTemplate.delete(hash);

    }



    @Override
    public  TransactionAccountDto getOrderByHash(String hash){
        if (hash != null) {
            return this.query().eq("certification_hash", hash).one();
        }
        return null;
    }


    private  Boolean TransactionMessageIntoRedis(TransactionAccountDto transactionAccountDto){
        //将订单信息存入redis缓存中,等待支付。(15分钟过期)
        String buyer_id = transactionAccountDto.getBuyerId().toString();
        String hash  = transactionAccountDto.getCertificationHash();

        //通过buyerID存储redis信息
        if(Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(buyer_id,hash))) return false;
        stringRedisTemplate.opsForSet().add(buyer_id,hash);
        stringRedisTemplate.expire(buyer_id,15,TimeUnit.MINUTES);

        return true;
    }

    private String generateOrderId(){
        return Const.ORDER_ID_INFO + UUID.randomUUID().toString().replace("-", "");
    }




}

