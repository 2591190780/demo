package com.example.service.impl.transaction;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.TransactionAccountDto;
import com.example.mapper.transaction.TransactionInfoAddMapper;
import com.example.service.transaction.TransactionInfoAddService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class TransactionInfoAddImpl extends ServiceImpl<TransactionInfoAddMapper, TransactionAccountDto>
        implements TransactionInfoAddService {

    @Resource
    JwtUtils jwtUtils;
    @Resource


    @Override
    public  Boolean TransactionInfoAdd(TransactionAccountDto transactionAccountDto){



        return false;
    }


}

