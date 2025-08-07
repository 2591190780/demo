package com.example.service.impl.transaction;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.DeliveryInfoDto;
import com.example.entity.dto.TransactionAccountDto;
import com.example.mapper.transaction.DeliveryInfoMapper;
import com.example.service.transaction.DeliveryInfoService;
import com.example.service.transaction.TransactionProcessService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class DeliveryInfoImpl extends ServiceImpl<DeliveryInfoMapper, DeliveryInfoDto>
        implements DeliveryInfoService {


    @Resource
    JwtUtils jwtUtils;

    @Resource
    TransactionProcessService transactionProcessService;

    @Override
    public boolean addDeliveryInfo(HttpServletRequest request , DeliveryInfoDto deliveryInfoDto){
        /**
         * 前端传入 orderID startAddress endAddress startTime sellerPhone buyerphone
         *  先检查orderID的交易信息是否正确。
         */
        if (!this.verifyByAlipayOrder(request, deliveryInfoDto.getOrderId())){
            return false;
        }
        if(this.selectDeliveryInfoByAlipayOrder(deliveryInfoDto.getOrderId()) != null) return false;
        String string = "time:"+ LocalDateTime.now()+ "|"
                + "address:" + deliveryInfoDto.getStartAddress();
        deliveryInfoDto.setDeliveryProcess(string);

        this.transactionProcessService.transactionUpdateDeliveryTime(deliveryInfoDto.getOrderId());
        return this.save(deliveryInfoDto) &&
                this.transactionProcessService.upDateStatusOrHash(deliveryInfoDto.getOrderId()
                , "3");
    }

    @Override
    public  DeliveryInfoDto selectDeliveryInfoByAlipayOrder(String alipayOrderId){
        return  this.query().eq("order_id", alipayOrderId).one();
    }

    @Override
    public  boolean updateDeliveryInfo(HttpServletRequest request, String alipayOrderId
            , String newAddress,String time ){
        if (!this.verifyByAlipayOrder(request,alipayOrderId)){
            return false;
        }
        //通过alipay的订单号获取交易记录
        DeliveryInfoDto dto = this.selectDeliveryInfoByAlipayOrder(alipayOrderId);
        String message = dto.getDeliveryProcess();
        //将后续的地址添加到原来的message上
        message = message + "|" +
                "time:"+ time + "|" +
                "address:" + newAddress ;

        return this.update().eq("order_id",alipayOrderId).set("delivery_process",message).update();
    }

    private boolean verifyByAlipayOrder(HttpServletRequest request,String alipayOrderId){
        TransactionAccountDto dto = this.transactionProcessService.getOrderByAlipayOrder(
                alipayOrderId); //通过alipay的订单号获取交易记录
        Integer uid = jwtUtils.getRequesetId(request); //当前登录状态下的ID
        if(dto==null){return false;}  //查询不到交易信息 返回错误
        if(!Objects.equals(uid,dto.getSellerId())){ return false;} //卖家与当前登陆账号不符合，不能添加运送信息。
        return true;
    }


}
