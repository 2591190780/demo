package com.example.service.transaction;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.TransactionAccountDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface TransactionProcessService extends IService<TransactionAccountDto> {
    //添加交易记录 生成hash存储到数据库中
    TransactionAccountDto TransactionInfoAdd(TransactionAccountDto transactionAccountDto, HttpServletRequest request);
    //更新订单状态
    boolean transactionStatusUpdate(TransactionAccountDto dto,String status);
    TransactionAccountDto getOrderByHash(String hash);
    String TransactionInfoAddMulti(List<TransactionAccountDto> dtoList);
    boolean cancelTransaction(HttpServletRequest request,String hash);
    boolean cancelTransactionMulti(HttpServletRequest request,List<String> hashList);
    List<TransactionAccountDto> transactionSelectMulti(HttpServletRequest request,Integer id);
    TransactionAccountDto transactionSelect(HttpServletRequest request,Integer id);
    List<TransactionAccountDto> getOrderByAlipayOrder(String alipayOrder);
    boolean transactionUpdateDeliveryTime(String alipayOrder);
    boolean upDateStatusOrHash(String text,String status);
    boolean completeTransaction(String OrderID);
    List<TransactionAccountDto> paySelectForSeller(Integer FarmerID);
    List<TransactionAccountDto> paySelectForBuyer(Integer BuyerID);
    TransactionAccountDto getOrderByTransactionID(Integer transactionID);
    TransactionAccountDto getOrderByAlipayOrderAndTrasactionId(String alipayOrder,Integer transactionId);
    boolean updateStatusByAlipayOrder(String alipayOrder,Integer trasactionId,Integer sellerId,String status);
}
