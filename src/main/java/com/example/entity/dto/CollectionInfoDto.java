package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.vo.response.ProductVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Data
@TableName("user_collection")
@AllArgsConstructor
public class CollectionInfoDto {

    //购物车数据Id
    @TableId(type = IdType.AUTO)
    private Integer collectionId;
    //消费者Id
    private Integer userId;
    //产品Id
    private Integer productId;
    //产品数量
    private Integer productNum;
    //购物车产品添加时间
    private LocalDateTime create_time;
    //产品哈希值
    @Length(min = 66, max = 66)
    private String product_hash;
    //添加类型 1购物车/2收藏夹
    private String operation_type;


}
