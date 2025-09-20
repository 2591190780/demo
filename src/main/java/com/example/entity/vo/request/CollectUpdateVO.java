package com.example.entity.vo.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@TableName("user_collection")
@AllArgsConstructor
public class CollectUpdateVO {
     @TableId(type = IdType.AUTO)
     private Integer collectionId;
    private Integer userId;
    private Integer productId;
    private Integer productNum;
}
