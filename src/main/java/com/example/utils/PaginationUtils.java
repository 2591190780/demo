package com.example.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.PageParam;
import org.springframework.util.StringUtils;

public class PaginationUtils {

    /**
     * 应用排序规则到QueryWrapper
     */
    public static <T> void applySorting(PageParam param, QueryWrapper<T> wrapper) {
        // 优先处理多字段排序
        if (param.getOrders() != null && !param.getOrders().isEmpty()) {
            for (PageParam.OrderItem order : param.getOrders()) {
                applyOrder(wrapper, order.getField(), order.getDirection());
            }
        }
        // 兼容单字段排序
        else if (StringUtils.hasText(param.getSortField())) {
            applyOrder(wrapper, param.getSortField(), param.getSortOrder());
        }
    }

    private static <T> void applyOrder(QueryWrapper<T> wrapper, String field, String direction) {
        if (!StringUtils.hasText(field)) return;

        // 安全过滤字段名（防止SQL注入）
        String safeField = field.replaceAll("[^a-zA-Z0-9_]", "");
        if (safeField.isEmpty()) return;

        // 确定排序方向
        boolean isAsc = !"desc".equalsIgnoreCase(direction);

        // 应用排序
        wrapper.orderBy(true, isAsc, safeField);
    }

    /**
     * 创建MyBatis Plus分页对象
     */
    public static <T> Page<T> createPage(PageParam param) {
        return new Page<>(param.getPageNum(), param.getPageSize());
    }
}
