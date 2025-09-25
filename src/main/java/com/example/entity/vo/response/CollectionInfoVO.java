package com.example.entity.vo.response;

import com.alipay.api.domain.AccountVO;
import com.example.entity.dto.Account;
import com.example.entity.dto.CollectionInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CollectionInfoVO {
    CollectionInfoDto collectionInfo;
    ProductVO productVO;
    Account account;
}
