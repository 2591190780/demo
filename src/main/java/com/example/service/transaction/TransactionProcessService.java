package com.example.service.transaction;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.TransactionAccountDto;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface TransactionProcessService extends IService<TransactionAccountDto> {
    //添加交易记录 生成hash存储到数据库中
    Boolean TransactionInfoAdd(TransactionAccountDto transactionAccountDto, HttpServletRequest request);
    //更新订单状态
    boolean transactionStatusUpdate(TransactionAccountDto dto,String status);
    TransactionAccountDto getOrderByHash(String hash);

    Boolean TransactionInfoAddMulti(List<TransactionAccountDto> dtoList);


}
