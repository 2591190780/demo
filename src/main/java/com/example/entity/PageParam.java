package com.example.entity;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageParam {
    // 页码，默认1
    private Integer pageNum = 1;

    // 每页数量，默认10
    private Integer pageSize = 10;

    // 排序字段（可选）
    private String sortField;

    // 排序方向：asc/desc（可选）
    private String sortOrder;

    // 多字段排序支持
    private List<OrderItem> orders = new ArrayList<>();

    public <T> Page<T> toPage() {
        return new Page<>(pageNum, pageSize);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String field;
        private String direction = "asc";
    }
}