package com.example.entity.vo.response;

import com.alipay.api.domain.AddressInfoVO;
import com.example.entity.dto.AddressDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderInfoVO {
    TransactionInfoVO transactionInfoVO;
    AddressDto addressDto;
}
