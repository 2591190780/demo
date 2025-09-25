package com.example.service.impl.transaction;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.TransactionAccountDto;
import com.example.entity.vo.response.ProductVO;
import com.example.mapper.transaction.TransactionProcessMapper;
import com.example.service.product.ProductInfoSelectAccountService;
import com.example.service.transaction.TransactionProcessService;
import com.example.utils.BlockchainHashUtil;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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
    @Resource
    ProductInfoSelectAccountService productInfoSelectAccountService;

    @Override
    public boolean updateStatusByAlipayOrder(String alipayOrder,Integer transactionId,Integer sellerId,String status){
        TransactionAccountDto dto = this.getOrderByAlipayOrderAndTrasactionId(alipayOrder,transactionId);
        if(!Objects.equals(dto.getSellerId(), sellerId)) {return false;}
            dto.setStatus(status);
            this.updateById(dto);
        return true;
    }

    @Override
    public  List<TransactionAccountDto> paySelectForSeller(Integer FarmerID){
        //检查订单状态。
        List<TransactionAccountDto> dtoList = this.query()
                .eq("seller_id",FarmerID).eq("status",1).list();
        this.updateStatusListener(dtoList);
        return this.query().eq("seller_id",FarmerID)
                .orderByAsc("status")
                .orderByDesc("order_time")
                .ne("status",6)
                .list();
    }

    private void updateStatusListener(List<TransactionAccountDto> dtoList){
        for (TransactionAccountDto dto : dtoList) {
            if (LocalDateTime.now().isAfter(dto.getOrderTime().plusMinutes(15))){
                this.update().eq("id",dto.getId())
                        .set("status",6).update();
            }
        }
    }

    @Override
    public  List<TransactionAccountDto> paySelectForBuyer(Integer BuyerID){
        //检查订单状态。
        List<TransactionAccountDto> dtoList = this.query()
                .eq("buyer_id",BuyerID).eq("status",1).list();
        this.updateStatusListener(dtoList);
        return this.query().eq("buyer_id",BuyerID)
                .orderByAsc("status")
                .orderByDesc("order_time")
                .list();
    }

    //添加单个交易信息
    @Override
    //服了，这里为毛线返回整个类，无语，写昏头了。
    //哦，没昏头，这里用户提交的交易申请要给用户确认。所以得返回交易的具体信息。草
    public TransactionAccountDto TransactionInfoAdd(TransactionAccountDto dto, HttpServletRequest request){
        //前端传入 卖家id，产品id，数量，总价，后端生成交易hash，订单号，以及下单时间。并返回给前端
        Integer requesetId = jwtUtils.getRequesetId(request); //获取当前登录状态的ID
        dto.setBuyerId(requesetId);
        String order_id = generateOrderId();  //生成订单号
        dto.setOrderId(order_id);
        dto.setOrderTime(LocalDateTime.now());  //生成下单时间
        if (this.requestProductVerify(dto)){  //认证通过
            return  null;
        }
        String hash = blockchainHashUtil.generateTransactionHash(dto);
        dto.setCertificationHash(hash);
        if(TransactionMessageIntoRedis(dto)){
            dto.setStatus("1");
            return this.save(dto)? dto :null;
            /**
             * 这里要执行上链操作 ----->  blockChainEvidenceService
             * 如果信息存储成功--->生成区块链凭证初始信息--->调用合约进行上链操作
             *      --->区块链返回上链成功的区块号--->更新数据库的上链信息。
             */
        }
        return null;
    }
    //添加多个交易信息
    @Override
    public  List<TransactionAccountDto> TransactionInfoAddMulti(List<TransactionAccountDto> dtoList){
        boolean flag =true;
        for(TransactionAccountDto dto : dtoList){
            //前端传入 买家id，卖家id，产品id，数量，总价，后端生成交易hash，订单号，以及下单时间。
            String order_id = generateOrderId();
            dto.setOrderId(order_id);
            dto.setOrderTime(LocalDateTime.now());
            String hash = blockchainHashUtil.generateTransactionHash(dto);
            dto.setCertificationHash(hash);
            if (this.requestProductVerify(dto)){  //认证通过
                return  null;
            }
            if(TransactionMessageIntoRedis(dto)){
                dto.setStatus("1");
                flag = this.save(dto);
                /**
                 * 这里要执行上链操作 ----->  blockChainEvidenceService
                 * 如果信息存储成功--->生成区块链凭证初始信息--->调用合约进行上链操作
                 *      --->区块链返回上链成功的区块号--->更新数据库的上链信息。
                 */
            }
        }
        if(!flag){
            return null;
        }
        return  dtoList;
    }

    @Override
    public  boolean transactionUpdateDeliveryTime(String alipayOrder){
        return this.update().eq("alipay_order",alipayOrder)
                .set("delivery_time",LocalDateTime.now()).update();
    }
    @Override
    public TransactionAccountDto getOrderByTransactionID(Integer transactionID){
        return query().eq("id", transactionID).one();
    }

    //更新订单状态
    @Override
    public boolean transactionStatusUpdate(TransactionAccountDto dto,String status){
        // 更新订单状态
        UpdateWrapper<TransactionAccountDto> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("certification_hash", dto.getCertificationHash());
        // 设置更新字段
        updateWrapper
                .set("status", status)
                .set("alipay_order", dto.getAlipayOrder())
                .set("actual_payment", dto.getActualPayment());
        // 执行更新
        boolean updateResult = this.update(updateWrapper);
        // 如果更新成功且状态为已支付，清理Redis
        if (updateResult && "2".equals(dto.getStatus())) {
            this.cleanTransactionFromRedis(dto.getBuyerId(), dto.getCertificationHash());
        }
        return updateResult;
    }

    @Override
    public List<TransactionAccountDto> getOrderByAlipayOrder(String alipayOrder){
        List<TransactionAccountDto> dtoList = this.query().eq("alipay_order", alipayOrder).list();
        if (!dtoList.isEmpty()) {
            return dtoList;
        }
        return null;
    }

    @Override
    public TransactionAccountDto getOrderByAlipayOrderAndTrasactionId(String alipayOrder,Integer transactionId){
        TransactionAccountDto dtoList = this.query().eq("alipay_order", alipayOrder)
                .eq("id",transactionId).one();
        if (dtoList!=null) {
            return dtoList;
        }
        return null;
    }

    @Override
    public  boolean upDateStatusOrHash(String text,String status){
        return this.update().eq("alipay_order", text)
                .or()
                .eq("certification_hash", text)
                .set("status",status).update();
    }


    @Override
    public  TransactionAccountDto getOrderByHash(String hash){
        TransactionAccountDto dto = this.query().eq("certification_hash", hash).one();
        if (dto != null) {
            return dto;
        }
        return null;
    }

    @Override
    public  boolean cancelTransaction(HttpServletRequest request,String hash){
        String userId = jwtUtils.getRequesetId(request).toString();
        if(Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(userId, hash))){
            stringRedisTemplate.opsForSet().remove(userId,hash);
            this.upDateStatusOrHash(hash,"6");
            return true;
        }
        return false;
    }

    @Override
    public boolean cancelTransactionMulti(HttpServletRequest request,List<String> hashList){
        String userId = jwtUtils.getRequesetId(request).toString();
        boolean flag = Boolean.TRUE;
        for(String hash : hashList){
            if(Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(userId, hash))){
                stringRedisTemplate.opsForSet().remove(userId,hash);
                this.upDateStatusOrHash(hash,"6");
            }else{
                 flag = Boolean.FALSE;
            }
        }
        return flag == Boolean.TRUE;
    }

    @Override
    public TransactionAccountDto transactionSelect(HttpServletRequest request,Integer id){
        Integer userId = jwtUtils.getRequesetId(request);
        if(!Objects.equals(userId, id)) return null;
        Set<String> hashes = stringRedisTemplate.opsForSet().members(userId.toString());
        if(hashes == null || hashes.isEmpty()) return null;
        for(String hash : hashes){
            this.clearLateDateTemplate(id,hash);
        }
        hashes = stringRedisTemplate.opsForSet().members(userId.toString());
        if(hashes == null || hashes.isEmpty()) return null;
        List<TransactionAccountDto> dtoList = this.query().eq("buyer_id", id).eq("status", "1")
                .orderByDesc("order_time").list();
        this.updateStatusListener(dtoList);
        for (TransactionAccountDto dto : dtoList) {
            if (hashes.contains(dto.getCertificationHash())) {
                return dto;
            }
        }
    return null;
    }

    @Override
    public List<TransactionAccountDto> transactionSelectMulti(HttpServletRequest request,Integer id){
        Integer userId = jwtUtils.getRequesetId(request);
        if(!Objects.equals(userId, id)) return null;
        Set<String> hashes = stringRedisTemplate.opsForSet().members(userId.toString());
        if(hashes == null || hashes.isEmpty()) return null;
        for(String hash : hashes){
            this.clearLateDateTemplate(id,hash);
        }
        hashes = stringRedisTemplate.opsForSet().members(userId.toString());
        if(hashes == null || hashes.isEmpty()) return null;
        List<TransactionAccountDto> dtoList = new ArrayList<>();
        for (String hash : hashes){
            TransactionAccountDto dto = this.getOrderByHash(hash);
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    public  boolean completeTransaction(String OrderID){

        return this.upDateStatusOrHash(OrderID,"5") &&
                this.update().eq("alipay_order",OrderID).update();
    }


    public  boolean requestProductVerify(TransactionAccountDto dto){
       ProductVO productVO = productInfoSelectAccountService.getProductInfoAccountByProductId(dto.getProductId());
       if(productVO == null) return true;
       BigDecimal dtoPayDiscount = dto.getPayDiscount();
       if (!Objects.equals(productVO.getDiscount(),dtoPayDiscount)){return true;}
       return false;
    }

    private void cleanTransactionFromRedis(Integer buyerId, String hash) {
        // 清理Redis中的订单信息
        /**
         * 用户在提交支付请求时，已经在逻辑里执行了删除redis原有缓存的逻辑，这里可能已经为空了，添加判断语句。
         */
        String buyerKey = buyerId.toString();
        if (Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(buyerKey, hash))) {
            stringRedisTemplate.opsForSet().remove(buyerKey, hash);
            stringRedisTemplate.delete(hash);
            this.upDateStatusOrHash(hash,"6");
        }
    }


    private void clearLateDateTemplate(Integer id ,String hash){
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(hash))) {
            stringRedisTemplate.opsForSet().remove(String.valueOf(id),hash);
        }
    }


    private  Boolean TransactionMessageIntoRedis(TransactionAccountDto transactionAccountDto){
        //将订单信息存入redis缓存中,等待支付。(15分钟过期)
        String buyer_id = transactionAccountDto.getBuyerId().toString();
        String hash  = transactionAccountDto.getCertificationHash();
        //通过buyerID 存储redis信息  id:hash(订单hash)
        if(Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(buyer_id,hash))) return false;
        stringRedisTemplate.opsForSet().add(buyer_id,hash);
        stringRedisTemplate.expire(buyer_id,15,TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set(hash,"",15,TimeUnit.MINUTES);

        return true;
    }

    private String generateOrderId(){
        return Const.ORDER_ID_INFO + UUID.randomUUID().toString().replace("-", "");
    }

}

