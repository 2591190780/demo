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
    List<TransactionAccountDto> TransactionInfoAddMulti(List<TransactionAccountDto> dtoList);
    boolean cancelTransaction(HttpServletRequest request,String hash);
    boolean cancelTransactionMulti(HttpServletRequest request,List<String> hashList);
    List<TransactionAccountDto> transactionSelectMulti(HttpServletRequest request,Integer id);
    TransactionAccountDto transactionSelect(HttpServletRequest request,Integer id);

}
