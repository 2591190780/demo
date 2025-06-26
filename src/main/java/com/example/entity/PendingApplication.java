package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 待处理申请信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingApplication {
    private String compositeKey;   // Redis中的复合键
    private String operation;      // 操作类型: add/update/delete
    private String targetType;     // 目标类型: product/sensor/nft
    private String targetId;       // 目标ID
    private Object targetInfo;     // 目标对象详细信息
    private String farmerId;       // 农户ID
    private String farmerName;     // 农户名称
    private String storedValue;    // Redis中存储的原始值
}