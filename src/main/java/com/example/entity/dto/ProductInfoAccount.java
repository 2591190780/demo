package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;

@Data
@TableName("product_info")
@AllArgsConstructor
public class ProductInfoAccount {
    // * 产品ID（主键）
    @TableId(type = IdType.AUTO)  // 自增主键
    private Integer productId;
    // * 农户ID（外键）
    private Integer farmerId;
    // * 产品名称
    private String name;
    // * 产品类别
    private String category;
    // * 产品价格（10位整数，2位小数）
    private BigDecimal price;
    // * 库存数量
    private Integer stock;
    // * 原产地
    private String originLocation;
    // 认证哈希值（固定长度66字符）
    @TableField(fill = FieldFill.INSERT)
    private String certificationHash;
    // * 更新时间
    private Date updateTime;

}
