package com.example.entity.vo.response;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class ProductVO {
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
    private BigDecimal stock;
    // * 原产地
    private String originLocation;
    //认证hash
    private String certificationHash;
    // * 更新时间
    private LocalDateTime  updateTime;
    // * 创建时间
    private LocalDateTime createTime;

    private byte isActive;

    private String productImgurl;
}
