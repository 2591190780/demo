package com.example.entity.vo.response;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Getter
@Setter
public class ProductVO {
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
    //认证hash
    private String certificationHash;
}
