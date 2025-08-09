package com.example.entity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    // 总记录数
    private Long total;

    // 当前页数据列表
    private List<T> list;

    // 当前页码
    private Integer currentPage;

    // 总页数
    private Integer totalPages;

    // 每页数量
    private Integer pageSize;

    // 从MyBatis Plus分页对象转换
    public static <T> PageResult<T> build(IPage<T> page) {
        return new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                (int) page.getCurrent(),
                (int) page.getPages(),
                (int) page.getSize()
        );
    }
}