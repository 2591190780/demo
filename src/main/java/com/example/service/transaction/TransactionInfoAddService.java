package com.example.service.transaction;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.TransactionAccountDto;

public interface TransactionInfoAddService extends IService<TransactionAccountDto> {
    Boolean TransactionInfoAdd(TransactionAccountDto transactionAccountDto);

}
