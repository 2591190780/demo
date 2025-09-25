package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@TableName("product_info")
@AllArgsConstructor
@Getter
@Setter
public class ProductInfoAccountDto {
    // * 产品ID（主键）
    @Id
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
    private BigDecimal stock;

    private BigDecimal stockRemain;
    // * 原产地
    private String originLocation;
    // 认证哈希值（固定长度66字符）
    @TableField(fill = FieldFill.INSERT)
    @Length(min = 66,max = 66)
    private String certificationHash;
    // * 更新时间
    private LocalDateTime  updateTime;
    // * 创建时间
    private LocalDateTime createTime;

    private byte isActive;

    private String productImgurl;

    private String description;
    private BigDecimal discount;
    private String unit;
    public ProductInfoAccountDto() {

    }

}
