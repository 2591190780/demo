package com.example.entity.vo.request;

import lombok.Data;
import java.util.List;

@Data
public class ContractCallRequest {
    private String contractAddress;  // 合约地址（必需）
    private String methodName;       // 要调用的方法名（必需）
    private List<Object> params;     // 方法参数列表
    private String userAddress;      // 调用者地址（必需）
}