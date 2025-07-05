package com.example.entity.vo.request;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@TableName("user_collection")
@AllArgsConstructor
public class CollectUpdateVO {
    private Integer userId;
    private Integer productId;
    private Integer productNum;
}
