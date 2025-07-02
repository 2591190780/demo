package com.example.entity.vo.response;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PendingApplicationVO {

    private String operation;      // 操作类型: add/update/delete
    private String targetType;     // 目标类型: product/sensor/nft
    private String targetId;       // 目标ID
    private String id;       // 农户ID
    private String farmerName;     // 农户名称
    private LocalDateTime createTime;     //创建时间
    private LocalDateTime deadLineTime;   //到期时间
    private Object operateData;

}
