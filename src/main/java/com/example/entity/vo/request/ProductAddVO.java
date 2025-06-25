package com.example.entity.vo.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class ProductAddVO {
    // * 产品ID（主键）

    @TableId(type = IdType.AUTO)  // 自增主键
    private Integer productId;

    private Integer farmerId;
    // * 产品名称
    private String name;
    // * 产品类别
    private String category;
    // * 产品价格（10位整数，2位小数）
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;
    // * 库存数量
    private BigDecimal stock;
    // * 原产地
    private String originLocation;
    // * 更新时间
    private LocalDateTime updateTime;
    private LocalDateTime createTime;

    private byte isActive;
}
